package com.pelmenstar.projktSens.serverProtocol

import com.pelmenstar.projktSens.shared.io.Input
import com.pelmenstar.projktSens.shared.io.Output
import com.pelmenstar.projktSens.shared.serialization.ObjectSerializer

/**
 * A contract between client and server, that describes in what way interpret byte data.
 */
interface Contract {
    suspend fun writeRequests(requests: Array<Request>, output: Output)
    suspend fun readRequests(input: Input): Array<Request>
    suspend fun writeResponses(responses: Array<Response>, output: Output)
    suspend fun readResponses(input: Input, valueClasses: Array<Class<*>>): Array<Response>
}