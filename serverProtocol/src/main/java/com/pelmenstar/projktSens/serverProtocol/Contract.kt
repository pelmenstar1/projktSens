package com.pelmenstar.projktSens.serverProtocol

import com.pelmenstar.projktSens.shared.io.SmartInputStream
import com.pelmenstar.projktSens.shared.io.SmartOutputStream

/**
 * A contract between client and server, that describes in what way interpret byte data.
 */
interface Contract {
    /**
     * Writes [request] to [output].
     * If run [readRequest] on written data, it will return the same request.
     */
    suspend fun writeRequest(request: Request, output: SmartOutputStream)

    /**
     * Reads [Request] from [input].
     */
    suspend fun readRequest(input: SmartInputStream): Request

    /**
     * Writes [response] to [output].
     * If [readResponse] is invoked on written data, it must return the same response
     */
    suspend fun writeResponse(response: Response, output: SmartOutputStream)

    /**
     * Reads [Response] from [input].
     *
     * @param valueClass expected class of data stored in [Request]
     */
    suspend fun <T : Any> readResponse(input: SmartInputStream, valueClass: Class<T>): Response
}