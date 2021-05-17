package com.pelmenstar.projktSens.serverProtocol.repo

import java.io.InputStream
import java.io.OutputStream

/**
 * A contract between client and server, that describes in what way interpret byte data.
 */
interface RepoContract {
    /**
     * Writes [request] to [output].
     * If run [readRequest] on written data, it will return the same request.
     */
    suspend fun writeRequest(request: RepoRequest, output: OutputStream)

    /**
     * Reads [RepoRequest] from [input].
     */
    suspend fun readRequest(input: InputStream): RepoRequest

    /**
     * Writes [response] to [output].
     * If [readResponse] is invoked on written data, it must return the same response
     */
    suspend fun writeResponse(response: RepoResponse, output: OutputStream)

    /**
     * Reads [RepoResponse] from [input].
     *
     * @param valueClass expected class of data stored in [RepoRequest]
     */
    suspend fun<T:Any> readResponse(input: InputStream, valueClass: Class<T>): RepoResponse
}

suspend inline fun<reified T:Any> RepoContract.readResponse(input: InputStream): RepoResponse {
    return readResponse(input, T::class.java)
}