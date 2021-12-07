package com.pelmenstar.projktSens.serverProtocol

import com.pelmenstar.projktSens.shared.io.SmartInputStream
import com.pelmenstar.projktSens.shared.io.SmartOutputStream

/**
 * A contract between client and server, that describes in what way interpret byte data.
 */
interface Contract {
    suspend fun writeRequests(requests: Array<Request>, output: SmartOutputStream)

    suspend fun readRequests(
        input: SmartInputStream,
    ): Array<Request>

    suspend fun writeResponses(responses: Array<Response>, output: SmartOutputStream)

    suspend fun readResponses(
        input: SmartInputStream,
        valueClasses: Array<Class<*>>
    ): Array<Response>
}

suspend fun Contract.writeRequest(request: Request, output: SmartOutputStream) {
    writeRequests(arrayOf(request), output)
}

suspend fun Contract.writeResponse(response: Response, output: SmartOutputStream) {
    writeResponses(arrayOf(response), output)
}