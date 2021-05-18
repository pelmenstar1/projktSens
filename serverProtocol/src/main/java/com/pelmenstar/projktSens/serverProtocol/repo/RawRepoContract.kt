package com.pelmenstar.projktSens.serverProtocol.repo

import com.pelmenstar.projktSens.shared.*
import com.pelmenstar.projktSens.shared.serialization.ValueWriter
import com.pelmenstar.projktSens.shared.serialization.ObjectSerializer
import com.pelmenstar.projktSens.shared.serialization.Serializable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import kotlin.math.min

/**
 * Writes and reads [RepoRequest], [RepoResponse] in raw non-human readable compact binary form.
 * ## RepoRequest
 * Binary format:
 * - 1 byte | [RepoRequest.command]
 * - 2 bytes | size of [RepoRequest.args], if args is null, contains 0
 * - size of args bytes | [RepoRequest.args], if args is not null
 *
 * ## RepoResponse
 * Binary format:
 * if response is [RepoResponse.Empty]
 * - 0 (1 byte)
 *
 * if response is [RepoResponse.Error]
 * - 1 (1 byte)
 * - 4 bytes | [RepoResponse.Error.error]
 *
 * if response is [RepoResponse.Ok]
 * - 2 (1 byte)
 * - 2 bytes | size of serialized value of response
 * - various byte | serialized representation of response
 */
object RawRepoContract: RepoContract {
    internal const val RESPONSE_BUFFER_SIZE = 1024
    private const val STATUS_EMPTY: Byte = 0
    private const val STATUS_ERROR: Byte = 1
    private const val STATUS_OK: Byte = 2

    private val RESPONSE_STATE_EMPTY_BUFFER = byteArrayOf(STATUS_EMPTY)

    override suspend fun writeRequest(request: RepoRequest, output: OutputStream) {
        withContext(Dispatchers.IO) {
            val command = request.command.toByte()
            val args = request.args

            if (args == null) {
                val header = ByteArray(3)
                header[0] = command

                output.writeSuspend(header)
            } else {
                val buffer = buildByteArray(args.size + 3) {
                    this[0] = command
                    writeShort(1, args.size.toShort())
                    System.arraycopy(args, 0, this, 3, args.size)
                }

                output.writeSuspend(buffer)
            }
        }
    }

    override suspend fun readRequest(input: InputStream): RepoRequest {
        return withContext(Dispatchers.IO) {
            val header = input.readNSuspend(3)

            val command = header[0].toInt() and 0xff
            val argsSize = header.getShort(1).toInt()

            when {
                argsSize == 0 -> {
                    RepoRequest(command)
                }
                argsSize > 0 -> {
                    val args = input.readNSuspend(argsSize)

                    RepoRequest(command, args)
                }
                else -> throw IOException()
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun writeResponse(response: RepoResponse, output: OutputStream) {
        when(response) {
            RepoResponse.Empty -> {
                output.writeSuspend(RESPONSE_STATE_EMPTY_BUFFER)
            }

            is RepoResponse.Error -> {
                output.writeSuspend(buildByteArray(5) {
                    this[0] = STATUS_ERROR
                    writeInt(1, response.error)
                })
            }

            is RepoResponse.Ok<*> -> {
                val serializer = Serializable.getSerializer(response.valueClass) as ObjectSerializer<Any>
                val objectSize = serializer.getSerializedObjectSize(response.value)

                output.writeSuspend(buildByteArray(objectSize + 3) {
                    this[0] = STATUS_OK
                    writeShort(1, objectSize.toShort())
                    serializer.writeObject(response.value,
                        ValueWriter(
                            this,
                            3
                        )
                    )
                })
            }
        }
    }

    override suspend fun <T:Any> readResponse(input: InputStream, valueClass: Class<T>): RepoResponse {
        val stateBuffer = input.readNSuspend(1)

        return when(val state = stateBuffer[0]) {
            STATUS_EMPTY -> RepoResponse.Empty
            STATUS_ERROR -> {
                val errorBuffer = input.readNSuspend(4)
                val error = errorBuffer.getInt(0)

                RepoResponse.error(error)
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

                RepoResponse.ok(value, valueClass)
            }
            else -> throw IOException("Illegal state of response. $state")
        }
    }
}