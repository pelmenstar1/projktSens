package com.pelmenstar.projktSens.serverProtocol

import com.pelmenstar.projktSens.shared.*
import com.pelmenstar.projktSens.shared.io.SmartInputStream
import com.pelmenstar.projktSens.shared.io.SmartOutputStream
import com.pelmenstar.projktSens.shared.serialization.Serializable
import com.pelmenstar.projktSens.shared.serialization.ValueWriter

/**
 * Writes and reads [Request], [Response] in raw non-human readable compact binary form.
 */
object RawContract : Contract {
    private const val RESPONSE_BUFFER_SIZE = 1024
    private const val TYPE_EMPTY: Byte = 0
    private const val TYPE_ERROR: Byte = 1
    private const val TYPE_OK: Byte = 2

    private const val REQUEST_HEADER_SEC_LENGTH = 2

    private val RESPONSE_TYPE_EMPTY_ARRAY = ByteArray(5).apply {
        this[0] = TYPE_EMPTY
    }

    private fun ByteArray.writeRequestHeader(command: Int, type: Int) {
        this[0] = command.toByte()
        this[1] = type.toByte()
    }

    override suspend fun openSession(output: SmartOutputStream, reqCount: Int) {
        if(reqCount <= 0) {
            throw RuntimeException("Request count <= 0")
        }

        if(reqCount > 127) {
            throw RuntimeException("Request count limit exceeded (limit=128, current=${reqCount})")
        }

        output.write(buildByteArray(1) { this[0] = reqCount.toByte() })
    }

    override suspend fun writeRequest(request: Request, output: SmartOutputStream) {
        val command = request.command
        val arg = request.argument

        val buffer: ByteArray
        if (arg == null) {
            buffer = ByteArray(REQUEST_HEADER_SEC_LENGTH)
            buffer.writeRequestHeader(command, Request.Argument.TYPE_NULL)
        } else {
            buffer = when(arg) {
                is Request.Argument.Integer -> {
                    buildByteArray(REQUEST_HEADER_SEC_LENGTH + 4) {
                        writeRequestHeader(command, Request.Argument.TYPE_INTEGER)
                        writeInt(REQUEST_HEADER_SEC_LENGTH, arg.value)
                    }
                }
                is Request.Argument.DateRange -> {
                    buildByteArray(REQUEST_HEADER_SEC_LENGTH + 8) {
                        writeRequestHeader(command, Request.Argument.TYPE_DATE_RANGE)
                        writeInt(REQUEST_HEADER_SEC_LENGTH, arg.start)
                        writeInt(REQUEST_HEADER_SEC_LENGTH + 4, arg.endInclusive)
                    }
                }
            }
        }

        output.write(buffer)
    }

    override suspend fun readRequest(input: SmartInputStream): Request {
        val header = input.readN(REQUEST_HEADER_SEC_LENGTH)

        val command = header[0].toInt()
        val argType = header[1].toInt()
        val arg: Request.Argument?

        when(argType) {
            Request.Argument.TYPE_NULL -> {
                arg = null
            }
            Request.Argument.TYPE_INTEGER -> {
                val buffer = input.readN(4)

                arg = Request.Argument.Integer(buffer.getInt(0))
            }
            Request.Argument.TYPE_DATE_RANGE -> {
                val buffer = input.readN(8)

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

    @Suppress("UNCHECKED_CAST")
    override suspend fun writeResponse(response: Response, output: SmartOutputStream) {
        val buffer = when(response) {
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
                    Serializable.getSerializer(value.javaClass)
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

        output.write(buffer)
    }

    override suspend fun <T : Any> readResponse(
        input: SmartInputStream,
        valueClass: Class<T>
    ): Response {
        val header = input.readN(5)
        val type = header[0]

        return if(type == TYPE_EMPTY) {
            Response.Empty
        } else {
            val dataSize = header.getInt(1)
            if(dataSize <= 0) {
                throw RuntimeException("dataSize < 0 (dataSize=$dataSize)")
            }

            val data = input.readNBuffered(dataSize, RESPONSE_BUFFER_SIZE)

            when(type) {
                TYPE_ERROR -> {
                    val error = data.getInt(0)

                    Response.Error(error)
                }
                TYPE_OK -> {
                    val value = Serializable.ofByteArray(data, valueClass)

                    Response.ok(value)
                }
                else -> throw RuntimeException("Invalid type of response ($type)")
            }
        }
    }
}