package com.pelmenstar.projktSens.serverProtocol

import androidx.annotation.RequiresApi
import java.io.InputStream
import java.io.OutputStream
import java.nio.channels.AsynchronousByteChannel

/**
 * A contract between client and server, that describes in what way interpret byte data.
 */
interface Contract {
    /**
     * Writes [request] to [output].
     * If run [readRequest] on written data, it will return the same request.
     */
    suspend fun writeRequest(request: Request, output: OutputStream)

    @RequiresApi(26)
    suspend fun writeRequest(request: Request, channel: AsynchronousByteChannel)

    /**
     * Reads [Request] from [input].
     */
    suspend fun readRequest(input: InputStream): Request

    @RequiresApi(26)
    suspend fun readRequest(channel: AsynchronousByteChannel): Request

    /**
     * Writes [response] to [output].
     * If [readResponse] is invoked on written data, it must return the same response
     */
    suspend fun writeResponse(response: Response, output: OutputStream)

    @RequiresApi(26)
    suspend fun writeResponse(response: Response, channel: AsynchronousByteChannel)

    /**
     * Reads [Response] from [input].
     *
     * @param valueClass expected class of data stored in [Request]
     */
    suspend fun <T : Any> readResponse(input: InputStream, valueClass: Class<T>): Response

    @RequiresApi(26)
    suspend fun <T : Any> readResponse(channel: AsynchronousByteChannel, valueClass: Class<T>): Response
}

/**
 * Reads [Response] from [input]. Note that generic parameter [T] is marked as reified, so
 * actual class of [T] should be known in compile-time
 */
suspend inline fun <reified T : Any> Contract.readResponse(input: InputStream): Response {
    return readResponse(input, T::class.java)
}