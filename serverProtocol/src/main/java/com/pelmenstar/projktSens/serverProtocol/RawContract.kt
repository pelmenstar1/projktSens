package com.pelmenstar.projktSens.serverProtocol

import com.pelmenstar.projktSens.shared.*
import com.pelmenstar.projktSens.shared.serialization.ObjectSerializer
import com.pelmenstar.projktSens.shared.serialization.Serializable
import com.pelmenstar.projktSens.shared.serialization.ValueReader
import com.pelmenstar.projktSens.shared.serialization.ValueWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import kotlin.math.min

/**
 * Writes and reads [Request], [Response] in raw non-human readable compact binary form.
 * ## RepoRequest
 * Binary format:
 * - 1 byte | [Request.command]
 * - 2 bytes | size of [Request.args], if args is null, contains 0
 * - size of args bytes | [Request.args], if args is not null
 *
 * ## RepoResponse
 * Binary format:
 * if response is [Response.Empty]
 * - 0 (1 byte)
 *
 * if response is [Response.Error]
 * - 1 (1 byte)
 * - 4 bytes | [Response.Error.error]
 *
 * if response is [Response.Ok]
 * - 2 (1 byte)
 * - 2 bytes | size of serialized value of response
 * - various byte | serialized representation of response
 */
object RawContract: Contract {
    internal const val RESPONSE_BUFFER_SIZE = 1024
    private const val STATUS_EMPTY: Byte = 0
    private const val STATUS_ERROR: Byte = 1
    private const val STATUS_OK: Byte = 2

    private val RESPONSE_STATE_EMPTY_BUFFER = byteArrayOf(STATUS_EMPTY)

    override suspend fun writeRequest(request: Request, output: OutputStream) {
        withContext(Dispatchers.IO) {
            val command = request.command.toByte()
            val arg = request.argument

            val buffer: ByteArray
            if (arg == null) {
                buffer = ByteArray(3)
                buffer[0] = command
            } else {
                val argClass = arg.javaClass
                val serializer = Serializable.getSerializer(argClass)
                val objectSize = serializer.getSerializedObjectSize(arg)
                val argClassNameBytes = argClass.name.toByteArray(Charsets.UTF_8)

                buffer = buildByteArray(objectSize + argClassNameBytes.size + 5) {
                    this[0] = command
                    writeShort(1, objectSize.toShort())
                    serializer.writeObject(arg, ValueWriter(this, 3))
                    writeShort(objectSize + 3, argClassNameBytes.size.toShort())
                    System.arraycopy(argClassNameBytes, 0, this, objectSize + 5, argClassNameBytes.size)
                }
            }
            output.writeSuspend(buffer)
        }
    }

    override suspend fun readRequest(input: InputStream): Request {
        return withContext(Dispatchers.IO) {
            val header = input.readNSuspend(3)

            val command = header[0].toInt() and 0xff
            val argBytesLength = header.getShort(1).toInt()
            val argBytes: ByteArray

            when {
                argBytesLength == 0 -> {
                    return@withContext Request(command)
                }
                argBytesLength > 0 -> {
                    argBytes = input.readNSuspend(argBytesLength)
                }
                else -> throw IOException()
            }

            val argClassNameLengthBuffer = input.readNSuspend(2)
            val argClassNameLength = argClassNameLengthBuffer.getShort(0).toInt()
            val argClassNameBytes = input.readNSuspend(argClassNameLength)
            val argClassName = String(argClassNameBytes, Charsets.UTF_8)
            val serializer = Serializable.getSerializer(Class.forName(argClassName))
            val arg = serializer.readObject(ValueReader(argBytes))

            Request(command, arg)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun writeResponse(response: Response, output: OutputStream) {
        when(response) {
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
                val serializer = Serializable.getSerializer(value.javaClass) as ObjectSerializer<Any>
                val objectSize = serializer.getSerializedObjectSize(response.value)

                output.writeSuspend(buildByteArray(objectSize + 3) {
                    this[0] = STATUS_OK
                    writeShort(1, objectSize.toShort())
                    serializer.writeObject(value,
                        ValueWriter(
                            this,
                            3
                        )
                    )
                })
            }
        }
    }

    override suspend fun <T:Any> readResponse(input: InputStream, valueClass: Class<T>): Response {
        val stateBuffer = input.readNSuspend(1)

        return when(val state = stateBuffer[0]) {
            STATUS_EMPTY -> Response.Empty
            STATUS_ERROR -> {
                val errorBuffer = input.readNSuspend(4)
                val error = errorBuffer.getInt(0)

                Response.error(error)
            }
            STATUS_OK -> {
                val dataSizeBuffer = input.readNSuspend(2)
                val dataSize = dataSizeBuffer.getShort(0).toInt()

                val data = ByteArray(dataSize)
                if (dataSize < RESPONSE_BUFFER_SIZE) {
                    input.readSuspendAndThrowIfNotEnough(data)
                } else {
                    var offset = 0
                    while (offset < dataSize) {
                        val expectedBytesToRead = min(dataSize - offset, RESPONSE_BUFFER_SIZE)

                        input.readSuspendAndThrowIfNotEnough(data, offset, expectedBytesToRead)

                        offset += expectedBytesToRead
                    }
                }

                val serializer = Serializable.getSerializer(valueClass)
                val value = Serializable.ofByteArray(data, serializer)

                Response.ok(value)
            }
            else -> throw IOException("Illegal state of response. $state")
        }
    }
}