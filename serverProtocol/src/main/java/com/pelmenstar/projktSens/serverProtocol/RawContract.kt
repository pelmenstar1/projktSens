package com.pelmenstar.projktSens.serverProtocol

import com.pelmenstar.projktSens.shared.*
import com.pelmenstar.projktSens.shared.io.Input
import com.pelmenstar.projktSens.shared.io.Output
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

    override suspend fun writeRequests(requests: Array<Request>, output: Output) {
        if(requests.isEmpty()) {
            throw RuntimeException("Requests are empty")
        }
        if(requests.size > 127) {
            throw RuntimeException("Limit of requests exceeded")
        }

        var bufferSize = requests.size * 2
        for(request in requests) {
            val arg = request.argument
            if(arg != null) {
                bufferSize += when(arg.type) {
                    Request.Argument.TYPE_INTEGER -> 4
                    Request.Argument.TYPE_DATE_RANGE -> 8
                    else -> throw RuntimeException("Invalid arg type")
                }
            }
        }
        val buffer = ByteArray(bufferSize + 3)
        buffer.writeShort(0, bufferSize.toShort())
        buffer[2] = requests.size.toByte()
        var index = 3

        for(request in requests) {
            buffer[index] = request.command.toByte()

            val arg = request.argument
            if(arg != null) {
                buffer[index + 1] = arg.type.toByte()

                when(arg) {
                    is Request.Argument.Integer -> {
                        buffer.writeInt(index + 2, arg.value)

                        index += 4
                    }
                    is Request.Argument.DateRange -> {
                        buffer.writeInt(index + 2, arg.start)
                        buffer.writeInt(index + 6, arg.endInclusive)

                        index += 8
                    }
                }
            } else {
                buffer[index + 1] = Request.Argument.TYPE_NULL.toByte()
            }

            index += 2
        }

        output.write(buffer)
    }

    override suspend fun readRequests(input: Input): Array<Request> {
        val header = input.readN(3)
        val bufferSize = header.getShort(0).toInt()
        val reqCount = header[2].toInt()

        if(bufferSize <= 0) {
            throw RuntimeException("bufferSize <= 0")
        }
        if(reqCount <= 0) {
            throw RuntimeException("reqCount <= 0")
        }

        val buffer = input.readN(bufferSize)
        var index = 0

        return Array(reqCount) {
            val command = buffer[index].toInt()
            val argType = buffer[index + 1].toInt()
            index += 2

            var arg: Request.Argument? = null

            if(argType != Request.Argument.TYPE_NULL) {
                arg = when(argType) {
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

            Request(command, arg)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun writeResponses(responses: Array<Response>, output: Output) {
        if(responses.isEmpty()) {
            throw RuntimeException("Responses are empty")
        }
        if(responses.size > 127) {
            throw RuntimeException("Limit of responses exceeded")
        }

        var bufferSize = responses.size
        for(response in responses) {
            when(response) {
                is Response.Error -> {
                    bufferSize += 4
                }
                is Response.Ok<*> -> {
                    val value = response.value
                    val serializer = Serializable.getSerializer(value.javaClass)

                    bufferSize += serializer.getSerializedObjectSize(value)
                }
            }
        }

        val buffer = ByteArray(bufferSize + 5)
        buffer.writeInt(0, bufferSize)
        buffer[4] = responses.size.toByte()

        var index = 5
        for(response in responses) {
            when(response) {
                Response.Empty -> {
                    buffer[index++] = TYPE_EMPTY
                }
                is Response.Error -> {
                    buffer[index++] = TYPE_ERROR
                    buffer.writeInt(index, response.error)

                    index += 4
                }
                is Response.Ok<*> -> {
                    val value = response.value
                    val serializer = Serializable.getSerializer(value.javaClass)
                    val objectSize = serializer.getSerializedObjectSize(value)

                    buffer[index++] = TYPE_OK

                    serializer.writeObject(
                        value,
                        ValueWriter(
                            buffer,
                            index
                        )
                    )

                    index += objectSize
                }
            }
        }

        output.write(buffer)
    }

    override suspend fun readResponses(
        input: Input,
        valueClasses: Array<Class<*>>
    ): Array<Response> {
        val header = input.readN(5)
        val totalSize = header.getInt(0)
        val responsesCount = header[4].toInt()

        val buffer = input.readN(totalSize)
        var index = 0

        return Array(responsesCount) { i ->
            val type = buffer[index++]
            when(type) {
                TYPE_EMPTY -> Response.Empty
                TYPE_ERROR -> {
                    val errorId = buffer.getInt(index)
                    index += 4

                    Response.error(errorId)
                }
                TYPE_OK -> {
                    val serializer = Serializable.getSerializer(valueClasses[i]) as ObjectSerializer<Any>

                    val value = serializer.readObject(ValueReader(
                        buffer,
                        index
                    ))
                    index += serializer.getSerializedObjectSize(value)

                    Response.ok(value)
                }
                else -> throw RuntimeException("Invalid response type")
            }
        }
    }
}