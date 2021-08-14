package com.pelmenstar.projktSens.serverProtocol

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.pelmenstar.projktSens.shared.*
import java.io.*
import kotlin.coroutines.suspendCoroutine

object JsonContract: Contract {
    private const val BUFFER_SIZE = 1024
    private val gson = Gson()

    override suspend fun writeRequest(request: Request, output: OutputStream) {
        val arg = request.argument

        val json = buildString {
            append("{command=\"")
            append(Commands.toString(request.command))
            append('"')

            if(arg != null) {
                append(",argClass=\"")
                append(arg.javaClass.name)
                append("\",arg=")
                gson.toJson(arg, this)
            }
            append('}')
        }

        output.writeString(json, Charsets.UTF_8)
    }

    override suspend fun readRequest(input: InputStream): Request {
        val json = input.readString(Charsets.UTF_8, BUFFER_SIZE)

        try {
            val root = JsonParser.parseString(json) as JsonObject
            val commandName = root.get("command").asString
            val command = Commands.fromString(commandName)

            return if(root.has("argClass") && root.has("arg")) {
                val argClassString = root.get("argClass").asString
                val argClass = Class.forName(argClassString)
                val argElement = root.get("arg")
                val arg = gson.fromJson(argElement, argClass)

                Request(command, arg)
            } else {
                Request(command)
            }
        } catch (e: Exception) {
            throw RuntimeException("Illegal request JSON", e)
        }
    }

    override suspend fun writeResponse(response: Response, output: OutputStream) {
        val json = when(response) {
            Response.Empty -> "{}"
            is Response.Error -> "{error=${Errors.toString(response.error)}}"
            is Response.Ok<*> -> gson.toJson(response.value)
        }

        output.writeString(json, Charsets.UTF_8)
    }

    override suspend fun <T : Any> readResponse(
        input: InputStream,
        valueClass: Class<T>
    ): Response {
        val json = input.readString(Charsets.UTF_8, BUFFER_SIZE)

        try {
            val root = JsonParser.parseString(json)
            if(root is JsonObject) {
                if (root.size() == 0) {
                    return Response.Empty
                }

                val errorElement = root.get("error")
                if (errorElement != null) {
                    val errorName = errorElement.asString

                    return Response.error(Errors.fromString(errorName))
                }
            }

            val value = gson.fromJson(root, valueClass)
            return Response.ok(value)
        } catch (e: Exception) {
            throw RuntimeException("Illegal request JSON", e)
        }
    }
}