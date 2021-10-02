package com.pelmenstar.projktSens.serverProtocol

import com.pelmenstar.projktSens.shared.*
import com.pelmenstar.projktSens.shared.readNBufferedSuspend
import com.pelmenstar.projktSens.shared.readNSuspend
import com.pelmenstar.projktSens.shared.writeSuspend
import com.pelmenstar.projktSens.shared.serialization.ObjectSerializer
import com.pelmenstar.projktSens.shared.serialization.Serializable
import com.pelmenstar.projktSens.shared.serialization.ValueWriter
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousByteChannel

/**
 * Writes and reads [Request], [Response] in raw non-human readable compact binary form.
 */
object RawContract : Contract {
    internal const val RESPONSE_BUFFER_SIZE = 1024
    private const val TYPE_EMPTY: Byte = 0
    private const val TYPE_ERROR: Byte = 1
    private const val TYPE_OK: Byte = 2

    private val RESPONSE_TYPE_EMPTY_ARRAY = ByteArray(5).apply {
        this[0] = TYPE_EMPTY
    }

    private val RESPONSE_TYPE_EMPTY_BUFFER = ByteBuffer.wrap(RESPONSE_TYPE_EMPTY_ARRAY)

    private fun createRequestData(request: Request): ByteArray {
        val command = request.command.toByte()
        val arg = request.argument

        val buffer: ByteArray
        if (arg == null) {
            buffer = ByteArray(2)
            buffer[0] = command
        } else {
            buffer = when(arg) {
                is Request.Argument.Integer -> {
                    buildByteArray(6) {
                        this[0] = command
                        this[1] = Request.Argument.TYPE_INTEGER.toByte()
                        writeInt(2, arg.value)
                    }
                }
                is Request.Argument.DateRange -> {
                    buildByteArray(10) {
                        this[0] = command
                        this[1] = Request.Argument.TYPE_DATE_RANGE.toByte()
                        writeInt(2, arg.start)
                        writeInt(6, arg.endInclusive)
                    }
                }
            }
        }

        return buffer
    }

    override suspend fun writeRequest(request: Request, output: OutputStream) {
        output.writeSuspend(createRequestData(request))
    }

    override suspend fun writeRequest(request: Request, channel: AsynchronousByteChannel) {
        val buffer = ByteBuffer.wrap(createRequestData(request))

        channel.writeSuspend(buffer)
    }

    override suspend fun readRequest(input: InputStream): Request {
        val header = input.readNSuspend(2)

        val command = header[0].toInt() and 0xff
        val argType = header[1].toInt() and 0xff
        val arg: Request.Argument?

        when(argType) {
            Request.Argument.TYPE_NULL -> {
                arg = null
            }
            Request.Argument.TYPE_INTEGER -> {
                val buffer = input.readNSuspend(4)

                arg = Request.Argument.Integer(buffer.getInt(0))
            }
            Request.Argument.TYPE_DATE_RANGE -> {
                val buffer = input.readNSuspend(8)

                val start = buffer.getInt(0)
                val end = buffer.getInt(4)

                arg = Request.Argument.DateRange(start, end)
            }
            else -> {
                throw RuntimeException("Invalid argType ($argType)")
            }
        }

        return Request(command, arg)
    }

    override suspend fun readRequest(channel: AsynchronousByteChannel): Request {
        val header = channel.readNSuspend(2)

        val command = header[0].toInt() and 0xff
        val argType = header[1].toInt() and 0xff
        val arg: Request.Argument?

        when(argType) {
            Request.Argument.TYPE_NULL -> {
                arg = null
            }
            Request.Argument.TYPE_INTEGER -> {
                val buffer = channel.readNSuspend(4)

                arg = Request.Argument.Integer(buffer.int)
            }
            Request.Argument.TYPE_DATE_RANGE -> {
                val buffer = channel.readNSuspend(8)

                val start = buffer.int
                val end = buffer.int

                arg = Request.Argument.DateRange(start, end)
            }
            else -> {
                throw RuntimeException("Invalid argType ($argType)")
            }
        }

        return Request(command, arg)
    }

    @Suppress("UNCHECKED_CAST")
    private fun createResponseDataArray(response: Response): ByteArray {
        return when(response) {
            Response.Empty -> RESPONSE_TYPE_EMPTY_ARRAY
            is Response.Error -> {
                buildByteArray(9) {
                    this[0] = TYPE_ERROR
                    writeInt(1, 4 /* length of data */)
                    writeInt(5, response.error)
                }
            }
            is Response.Ok<*> -> {
                val value = response.value
                val serializer =
                    Serializable.getSerializer(value.javaClass) as ObjectSerializer<Any>
                val objectSize = serializer.getSerializedObjectSize(response.value)

                buildByteArray(objectSize + 5) {
                    this[0] = TYPE_OK
                    writeInt(1, objectSize)
                    serializer.writeObject(
                        value,
                        ValueWriter(this, 5)
                    )
                }
            }
        }
    }

    private fun createResponseDataBuffer(response: Response): ByteBuffer {
        if(response === Response.Empty) {
            return RESPONSE_TYPE_EMPTY_BUFFER
        }

        return ByteBuffer.wrap(createResponseDataArray(response))
    }

    override suspend fun writeResponse(response: Response, output: OutputStream) {
        output.writeSuspend(createResponseDataArray(response))
    }

    override suspend fun writeResponse(response: Response, channel: AsynchronousByteChannel) {
        channel.writeSuspend(createResponseDataBuffer(response))
    }

    override suspend fun <T : Any> readResponse(
        input: InputStream,
        valueClass: Class<T>
    ): Response {
        val header = input.readNSuspend(5)
        val type = header[0]

        return if(type == TYPE_EMPTY) {
            Response.Empty
        } else {
            val dataSize = header.getInt(1)
            if(dataSize <= 0) {
                throw RuntimeException("dataSize < 0 (dataSize=$dataSize)")
            }

            val data = input.readNBufferedSuspend(dataSize, RESPONSE_BUFFER_SIZE)

            when(type) {
                TYPE_ERROR -> {
                    val error = data.getInt(0)

                    Response.Error(error)
                }
                TYPE_OK -> {
                    val serializer = Serializable.getSerializer(valueClass)
                    val value = Serializable.ofByteArray(data, serializer)

                    Response.ok(value)
                }
                else -> throw RuntimeException("Invalid type of response ($type)")
            }
        }
    }

    override suspend fun <T : Any> readResponse(
        channel: AsynchronousByteChannel,
        valueClass: Class<T>
    ): Response {
        val header = channel.readNSuspend(5)
        val type = header[0]

        return if(type == TYPE_EMPTY) {
            Response.Empty
        } else {
            val dataSize = header.getInt(1)
            if(dataSize <= 0) {
                throw RuntimeException("Invalid dataSize ($dataSize)")
            }

            val data = channel.readNBufferedSuspend(dataSize, RESPONSE_BUFFER_SIZE)

            when(type) {
                TYPE_ERROR -> {
                    val error = data.int

                    Response.Error(error)
                }
                TYPE_OK -> {
                    val serializer = Serializable.getSerializer(valueClass)
                    val value = Serializable.ofByteBuffer(data, serializer)

                    Response.ok(value)
                }
                else -> throw RuntimeException("Invalid type of response ($type)")
            }
        }
    }
}