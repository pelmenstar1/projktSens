package com.pelmenstar.projktSens.serverProtocol

import com.pelmenstar.projktSens.shared.io.Input
import com.pelmenstar.projktSens.shared.io.Output

/**
 * A contract between client and server, that describes in what way interpret byte data.
 */
interface Contract {
    suspend fun openSession(output: Output, reqCount: Int)

    /**
     * Writes [request] to [output].
     * If run [readRequest] on written data, it will return the same request.
     */
    suspend fun writeRequest(request: Request, output: Output)

    /**
     * Reads [Request] from [input].
     */
    suspend fun readRequest(input: Input): Request

    /**
     * Writes [response] to [output].
     * If [readResponse] is invoked on written data, it must return the same response
     */
    suspend fun writeResponse(response: Response, output: Output)

    /**
     * Reads [Response] from [input].
     *
     * @param valueClass expected class of data stored in [Request]
     */
    suspend fun <T : Any> readResponse(input: Input, valueClass: Class<T>): Response
}