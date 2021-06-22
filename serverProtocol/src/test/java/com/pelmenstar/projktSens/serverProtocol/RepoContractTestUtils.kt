package com.pelmenstar.projktSens.serverProtocol

import com.pelmenstar.projktSens.serverProtocol.repo.RepoContract
import com.pelmenstar.projktSens.serverProtocol.repo.RepoRequest
import com.pelmenstar.projktSens.serverProtocol.repo.RepoResponse
import kotlinx.coroutines.runBlocking
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.assertEquals

object RepoContractTestUtils {
    fun test(contract: RepoContract, request: RepoRequest) {
        runBlocking {
            val output = ByteArrayOutputStream()
            contract.writeRequest(request, output)

            val input = ByteArrayInputStream(output.toByteArray())
            val readFromInput = contract.readRequest(input)

            assertEquals(request, readFromInput)
        }
    }

    inline fun<reified T:Any> test(contract: RepoContract,response: RepoResponse) {
        runBlocking {
            val output = ByteArrayOutputStream()
            contract.writeResponse(response, output)

            val input = ByteArrayInputStream(output.toByteArray())
            val readFromInput = contract.readResponse(input, T::class.java)

            assertEquals(response, readFromInput)
        }
    }
}