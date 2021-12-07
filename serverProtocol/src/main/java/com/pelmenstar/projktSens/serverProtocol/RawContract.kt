package com.pelmenstar.projktSens.serverProtocol

import com.pelmenstar.projktSens.shared.*
import com.pelmenstar.projktSens.shared.io.SmartInputStream
import com.pelmenstar.projktSens.shared.io.SmartOutputStream
import com.pelmenstar.projktSens.shared.serialization.ObjectSerializer
import com.pelmenstar.projktSens.shared.serialization.Serializable
import com.pelmenstar.projktSens.shared.serialization.ValueReader
import com.pelmenstar.projktSens.shared.serialization.ValueWriter

/**
 * Writes and reads [Request], [Response] in raw non-human readable compact binary form.
 */
object RawContract : Contract {
    private const val TYPE_EMPTY: Byte = 0
    private const val TYPE_ERROR: Byte = 1
    private const val TYPE_OK: Byte = 2

    override suspend fun writeRequests(requests: Array<Request>, output: SmartOutputStream) {
        if(requests.isEmpty()) {
            throw RuntimeException("Cannot write empty array of requests")
        }

        val headerSize = 2 * requests.size
        var bufferSize = headerSize

        for(request in requests) {
            val arg = request.argument
            if(arg != null) {
                when(arg.type) {
                    Request.Argument.TYPE_INTEGER -> bufferSize += 4
                    Request.Argument.TYPE_DATE_RANGE -> bufferSize += 8
                }
            }
        }

        val buffer = ByteArray(bufferSize)
        buffer.writeShort(0, (bufferSize - 4).toShort())
        buffer.writeShort(2, requests.size.toShort())

        var index = 0
        for(request in requests) {
            val arg = request.argument

            buffer[index] = request.command.toByte()
            buffer[index + 1] = (arg?.type ?: Request.Argument.TYPE_NULL).toByte()

            if(arg != null) {
                index += when(arg) {
                    is Request.Argument.Integer -> {
                        buffer.writeInt(index, arg.value)

                        4
                    }
                    is Request.Argument.DateRange -> {
                        buffer.writeInt(index, arg.start)
                        buffer.writeInt(index + 4, arg.endInclusive)

                        8
                    }
                }
            }

            index += 2
        }

        output.write(buffer)
    }

    override suspend fun readRequests(
        input: SmartInputStream,
    ): Array<Request> {
        val sizeBuffer = input.readN(4)
        val totalSize = sizeBuffer.getShort(0).toInt()
        val reqCount = sizeBuffer.getShort(2).toInt()
        if(reqCount <= 0) {
            throw RuntimeException("Cannot receive requests if reqCount <= 0")
        }

        val buffer = input.readN(totalSize)

        var index = 0
        return Array(reqCount) {
            val command = buffer[index].toInt()
            val argType = buffer[index + 1].toInt()
            val arg: Request.Argument? = if(argType == Request.Argument.TYPE_NULL) {
                null
            } else {
                when(argType) {
                    Request.Argument.TYPE_INTEGER -> {
                        val value = buffer.getInt(index)
                        index += 4
                        Request.Argument.Integer(value)
                    }
                    Request.Argument.TYPE_DATE_RANGE -> {
                        val start = buffer.getInt(index)
                        val end = buffer.getInt(index + 4)

                        index += 8

                        Request.Argument.DateRange(start, end)
                    }
                    else -> throw RuntimeException("Invalid argType")
                }
            }

            index += 2

            Request(command, arg)
        }
    }

    override suspend fun writeResponses(responses: Array<Response>, output: SmartOutputStream) {
        if(responses.isEmpty()) {
            throw RuntimeException("Cannot write empty array of responses")
        }

        var bufferSize = 8
        for(response in responses) {
            bufferSize += when(response) {
                Response.Empty -> 1 /* command */
                is Response.Error -> 5 /* command & error */
                is Response.Ok<*> -> {
                    val value = response.value
                    val serializer = Serializable.getSerializer(value.javaClass)

                    serializer.getSerializedObjectSize(value) + 1
                }
            }
        }

        val buffer = ByteArray(bufferSize)
        buffer.writeInt(0, bufferSize - 4)
        buffer.writeInt(4, responses.size)
        var index = 8

        for(response in responses) {
            when(response) {
                Response.Empty -> {
                    buffer[index++] = TYPE_EMPTY
                }
                is Response.Error -> {
                    buffer[index] = TYPE_ERROR
                    buffer.writeInt(index + 1, response.error)

                    index += 5
                }
                is Response.Ok<*> -> {
                    val value = response.value
                    val serializer = Serializable.getSerializer(value.javaClass)
                    val objectSize = serializer.getSerializedObjectSize(value)

                    buffer[index] = TYPE_OK
                    serializer.writeObject(value, ValueWriter(buffer, index + 1))

                    index += 1 + objectSize
                }
            }
        }

        output.write(buffer)
    }

    override suspend fun readResponses(
        input: SmartInputStream,
        valueClasses: Array<Class<*>>
    ): Array<Response> {
        val sizeBuffer = input.readN(8)
        val totalSize = sizeBuffer.getInt(0)
        val responsesLength = sizeBuffer.getInt(4)
        if(responsesLength <= 0) {
            throw RuntimeException("responsesLength <= 0")
        }

        val buffer = input.readNBuffered(totalSize)

        var index = 0
        return Array(responsesLength) { i ->
            val type = buffer[index++]

            when(type) {
                TYPE_EMPTY -> Response.Empty
                TYPE_ERROR -> {
                    val errorId = buffer.getInt(index)

                    Response.error(errorId)
                }
                TYPE_OK -> {
                    val valueClass = valueClasses[i]
                    val serializer = Serializable.getSerializer(valueClass) as ObjectSerializer<Any>

                    val value = serializer.readObject(ValueReader(buffer, index))
                    index += serializer.getSerializedObjectSize(value)

                    Response.ok(value)
                }
                else -> throw RuntimeException("Invalid type of response")
            }
        }
    }
}