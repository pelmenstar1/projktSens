package com.pelmenstar.projktSens.serverProtocol

import com.pelmenstar.projktSens.serverProtocol.repo.RawRepoContract
import com.pelmenstar.projktSens.serverProtocol.repo.RepoCommands
import com.pelmenstar.projktSens.serverProtocol.repo.RepoRequest
import com.pelmenstar.projktSens.serverProtocol.repo.RepoResponse
import org.junit.Test
import kotlin.random.Random

class RawRepoContractTests {
    @Test
    fun repoRequest_no_args() {
        fun testCase(command: Int) {
            val request = RepoRequest(command)

            RepoContractTestUtils.test(RawRepoContract, request)
        }

        testCase(RepoCommands.GEN_DAY_REPORT)
        testCase(RepoCommands.GEN_DAY_RANGE_REPORT)
        testCase(RepoCommands.GET_AVAILABLE_DATE_RANGE)
        testCase(RepoCommands.GET_LAST_WEATHER)
    }

    @Test
    fun repoRequest_with_args() {
        fun testCase(command: Int) {
            val argsBytes = Random(0).nextBytes(64)
            val request = RepoRequest(command, TestObject(argsBytes))

            RepoContractTestUtils.test(RawRepoContract, request)
        }

        testCase(RepoCommands.GEN_DAY_REPORT)
        testCase(RepoCommands.GEN_DAY_RANGE_REPORT)
        testCase(RepoCommands.GET_AVAILABLE_DATE_RANGE)
        testCase(RepoCommands.GET_LAST_WEATHER)
    }

    @Test
    fun repoResponse_empty() {
        RepoContractTestUtils.test<Any>(RawRepoContract, RepoResponse.Empty)
    }

    @Test
    fun repoResponse_error() {
        fun testCase(errorId: Int) {
            val response = RepoResponse.error(errorId)

            RepoContractTestUtils.test<Any>(RawRepoContract, response)
        }

        testCase(Errors.NONE)
        testCase(Errors.UNKNOWN)
        testCase(Errors.INVALID_COMMAND)
        testCase(Errors.INVALID_ARGUMENTS)
        testCase(Errors.INVALID_RESPONSE)
        testCase(Errors.IO)
        testCase(Errors.INTERNAL_DB_ERROR)
    }

    @Test
    fun repoResponse_ok_lessThanBufferSize() {
        val random = Random(0)

        val obj = TestObject(random.nextBytes(RawRepoContract.RESPONSE_BUFFER_SIZE / 2))
        val response = RepoResponse.ok(obj)

        RepoContractTestUtils.test<TestObject>(RawRepoContract, response)
    }

    @Test
    fun repoResponse_ok_greaterThanBufferSize() {
        val random = Random(0)

        val obj = TestObject(random.nextBytes(RawRepoContract.RESPONSE_BUFFER_SIZE * 2))
        val response = RepoResponse.ok(obj)

        RepoContractTestUtils.test<TestObject>(RawRepoContract, response)
    }
}