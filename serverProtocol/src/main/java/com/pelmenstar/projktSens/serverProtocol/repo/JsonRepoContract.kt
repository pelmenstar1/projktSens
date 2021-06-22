package com.pelmenstar.projktSens.serverProtocol.repo

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.pelmenstar.projktSens.serverProtocol.Errors
import com.pelmenstar.projktSens.shared.readString
import com.pelmenstar.projktSens.shared.writeString
import java.io.InputStream
import java.io.OutputStream

object JsonRepoContract: RepoContract {
    private const val BUFFER_SIZE = 1024
    private val gson = Gson()

    override suspend fun writeRequest(request: RepoRequest, output: OutputStream) {
        val arg = request.argument

        val json = buildString {
            append("{command=\"")
            append(RepoCommands.toString(request.command))
            append('"')

            if(arg != null) {
                val argJson = gson.toJson(arg)

                append(",argClass=\"")
                append(arg.javaClass.name)
                append("\",arg=")
                append(argJson)
            }
            append('}')
        }

        output.writeString(json, Charsets.UTF_8)
    }

    override suspend fun readRequest(input: InputStream): RepoRequest {
        val json = input.readString(Charsets.UTF_8, BUFFER_SIZE)

        try {
            val root = JsonParser.parseString(json) as JsonObject
            val commandName = root.get("command").asString
            val command = RepoCommands.fromString(commandName)

            return if(root.has("argClass") && root.has("arg")) {
                val argClassString = root.get("argClass").asString
                val argClass = Class.forName(argClassString)
                val argElement = root.get("arg")
                val arg = gson.fromJson(argElement, argClass)

                RepoRequest(command, arg)
            } else {
                RepoRequest(command)
            }
        } catch (e: Exception) {
            throw RuntimeException("Illegal request JSON", e)
        }
    }

    override suspend fun writeResponse(response: RepoResponse, output: OutputStream) {
        val json = when(response) {
            RepoResponse.Empty -> "{}"
            is RepoResponse.Error -> {
                "{error=${Errors.toString(response.error)}}"
            }
            is RepoResponse.Ok<*> -> {
                gson.toJson(response.value)
            }
        }
        output.writeString(json, Charsets.UTF_8)
    }

    override suspend fun <T : Any> readResponse(
        input: InputStream,
        valueClass: Class<T>
    ): RepoResponse {
        val json = input.readString(Charsets.UTF_8, BUFFER_SIZE)

        try {
            val root = JsonParser.parseString(json) as JsonObject
            if(root.size() == 0) {
                return RepoResponse.Empty
            }

            if(root.has("error")) {
                val errorName = root.get("error").asString
                val errorId = Errors.fromString(errorName)

                return RepoResponse.error(errorId)
            }

            val value = gson.fromJson(json, valueClass)
            return RepoResponse.ok(value)
        } catch (e: Exception) {
            throw RuntimeException("Illegal request JSON", e)
        }
    }
}