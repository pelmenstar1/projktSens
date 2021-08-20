package com.pelmenstar.projktSens.serverProtocol

import com.pelmenstar.projktSens.shared.*
import com.pelmenstar.projktSens.shared.serialization.ObjectSerializer
import com.pelmenstar.projktSens.shared.serialization.Serializable
import com.pelmenstar.projktSens.shared.serialization.ValueReader
import com.pelmenstar.projktSens.shared.serialization.ValueWriter
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * Writes and reads [Request], [Response] in raw non-human readable compact binary form.
 */
object RawContract : Contract {
    internal const val RESPONSE_BUFFER_SIZE = 1024
    private const val STATUS_EMPTY: Byte = 0
    private const val STATUS_ERROR: Byte = 1
    private const val STATUS_OK: Byte = 2

    private val RESPONSE_STATE_EMPTY_BUFFER = byteArrayOf(STATUS_EMPTY)

    override suspend fun writeRequest(request: Request, output: OutputStream) {
        val command = request.command.toByte()
        val arg = request.argument

        val buffer: ByteArray
        if (arg == null) {
            buffer = ByteArray(5)
            buffer[0] = command
        } else {
            val argClass = arg.javaClass
            val argClassName = argClass.name
            val serializer = Serializable.getSerializer(argClass)
            val objectSize = serializer.getSerializedObjectSize(arg)

            buffer = buildByteArray(objectSize + argClassName.length + 5) {
                this[0] = command
                writeShort(1, objectSize.toShort())
                writeShort(3, argClassName.length.toShort())
                serializer.writeObject(arg, ValueWriter(this, 5))
                StringUtils.writeAsciiBytes(argClassName, this, objectSize + 5)
            }
        }

        output.writeSuspend(buffer)
    }

    override suspend fun readRequest(input: InputStream): Request {
        val header = input.readNSuspend(5)

        val command = header[0].toInt() and 0xff
        val argContentLength = header.getShort(1).toInt()
        val argClassNameLength = header.getShort(3).toInt()

        val argBytes: ByteArray

        when {
            argContentLength == 0 -> {
                return Request(command)
            }
            argContentLength > 0 -> {
                argBytes = input.readNSuspend(argContentLength + argClassNameLength)
            }
            else -> throw IOException()
        }

        val argClassName = String(argBytes, argContentLength, argClassNameLength, Charsets.US_ASCII)

        val serializer = Serializable.getSerializer(Class.forName(argClassName))
        val arg = serializer.readObject(ValueReader(argBytes))

        return Request(command, arg)
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun writeResponse(response: Response, output: OutputStream) {
        when (response) {
            Response.Empty -> {
                output.writeSuspend(RESPONSE_STATE_EMPTY_BUFFER)
            }

            is Response.Error -> {
                output.writeSuspend(buildByteArray(5) {
                    this[0] = STATUS_ERROR
                    writeInt(1, response.error)
                })
            }

            is Response.Ok<*> -> {
                val value = response.value
                val serializer =
                    Serializable.getSerializer(value.javaClass) as ObjectSerializer<Any>
                val objectSize = serializer.getSerializedObjectSize(response.value)

                output.writeSuspend(buildByteArray(objectSize + 3) {
                    this[0] = STATUS_OK
                    writeShort(1, objectSize.toShort())
                    serializer.writeObject(
                        value,
                        ValueWriter(
                            this,
                            3
                        )
                    )
                })
            }
        }
    }

    override suspend fun <T : Any> readResponse(
        input: InputStream,
        valueClass: Class<T>
    ): Response {
        val stateBuffer = input.readNSuspend(1)

        return when (val state = stateBuffer[0]) {
            STATUS_EMPTY -> Response.Empty
            STATUS_ERROR -> {
                val errorBuffer = input.readNSuspend(4)
                val error = errorBuffer.getInt(0)

                Response.error(error)
            }
            STATUS_OK -> {
                val dataSizeBuffer = input.readNSuspend(2)
                val dataSize = dataSizeBuffer.getShort(0).toInt()

                val data = input.readNBufferedSuspend(dataSize, RESPONSE_BUFFER_SIZE)

                val serializer = Serializable.getSerializer(valueClass)
                val value = Serializable.ofByteArray(data, serializer)

                Response.ok(value)
            }
            else -> throw IOException("Illegal state of response. $state")
        }
    }
}