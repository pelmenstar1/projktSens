package com.pelmenstar.projktSens.serverProtocol

import kotlinx.coroutines.runBlocking
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.assertEquals

object ContractTestUtils {
    fun test(contract: Contract, request: Request) {
        runBlocking {
            val output = ByteArrayOutputStream()
            contract.writeRequest(request, output)

            val input = ByteArrayInputStream(output.toByteArray())
            val readFromInput = contract.readRequest(input)

            assertEquals(request, readFromInput)
        }
    }

    inline fun <reified T : Any> test(contract: Contract, response: Response) {
        runBlocking {
            val output = ByteArrayOutputStream()
            contract.writeResponse(response, output)

            val input = ByteArrayInputStream(output.toByteArray())
            val readFromInput = contract.readResponse(input, T::class.java)

            assertEquals(response, readFromInput)
        }
    }
}