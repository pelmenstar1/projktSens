package com.pelmenstar.projktSens.serverProtocol

import org.junit.Test
import kotlin.random.Random

class JsonContractTests {
    @Test
    fun repoRequest_no_args() {
        fun testCase(command: Int) {
            val request = Request(command)

            ContractTestUtils.test(JsonContract, request)
        }

        testCase(Commands.GET_DAY_REPORT)
        testCase(Commands.GET_DAY_RANGE_REPORT)
        testCase(Commands.GET_AVAILABLE_DATE_RANGE)
        testCase(Commands.GET_LAST_WEATHER)
    }

    @Test
    fun repoRequest_with_args() {
        fun testCase(command: Int) {
            val argsBytes = Random(0).nextBytes(64)
            val request = Request(command, TestObject(argsBytes))

            ContractTestUtils.test(JsonContract, request)
        }

        testCase(Commands.GET_DAY_REPORT)
        testCase(Commands.GET_DAY_RANGE_REPORT)
        testCase(Commands.GET_AVAILABLE_DATE_RANGE)
        testCase(Commands.GET_LAST_WEATHER)
    }

    @Test
    fun repoResponse_empty() {
        ContractTestUtils.test<Any>(JsonContract, Response.Empty)
    }

    @Test
    fun repoResponse_error() {
        fun testCase(errorId: Int) {
            val response = Response.error(errorId)

            ContractTestUtils.test<Any>(JsonContract, response)
        }

        testCase(Errors.NONE)
        testCase(Errors.UNKNOWN)
        testCase(Errors.INVALID_COMMAND)
        testCase(Errors.INVALID_ARGUMENTS)
        testCase(Errors.IO)
        testCase(Errors.INTERNAL_DB_ERROR)
    }

    @Test
    fun repoResponse_ok_lessThanBufferSize() {
        val random = Random(0)

        val obj = TestObject(random.nextBytes(500))
        val response = Response.ok(obj)

        ContractTestUtils.test<TestObject>(JsonContract, response)
    }

    @Test
    fun repoResponse_ok_greaterThanBufferSize() {
        val random = Random(0)

        val obj = TestObject(random.nextBytes(2000))
        val response = Response.ok(obj)

        ContractTestUtils.test<TestObject>(JsonContract, response)
    }
}