package com.pelmenstar.projktSens.serverProtocol

import com.pelmenstar.projktSens.shared.time.ShortDate
import com.pelmenstar.projktSens.shared.time.ShortDateRange
import org.junit.Test
import kotlin.random.Random

class RawContractTests {
    @Test
    fun repoRequest_no_args() {
        fun testCase(command: Int) {
            val request = Request(command)

            ContractTestUtils.test(RawContract, request)
        }

        testCase(Commands.GET_DAY_REPORT)
        testCase(Commands.GET_DAY_RANGE_REPORT)
        testCase(Commands.GET_AVAILABLE_DATE_RANGE)
        testCase(Commands.GET_LAST_WEATHER)
    }

    @Test
    fun repoRequest_with_args() {
        fun testCase(command: Int, arg: Request.Argument? = null) {
            ContractTestUtils.test(RawContract, Request(command, arg))
        }

        testCase(Commands.GET_DAY_REPORT, Request.Argument.Integer(123))
        testCase(Commands.GET_DAY_RANGE_REPORT, Request.Argument.DateRange(
            ShortDateRange(ShortDate.now(), ShortDate.nowAndPlusDays(1))
        ))
        testCase(Commands.GET_AVAILABLE_DATE_RANGE)
        testCase(Commands.GET_LAST_WEATHER)
    }

    @Test
    fun repoResponse_empty() {
        ContractTestUtils.test<Any>(RawContract, Response.Empty)
    }

    @Test
    fun repoResponse_error() {
        fun testCase(errorId: Int) {
            val response = Response.error(errorId)

            ContractTestUtils.test<Any>(RawContract, response)
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

        val obj = TestObject(random.nextBytes(RawContract.RESPONSE_BUFFER_SIZE / 2))
        val response = Response.ok(obj)

        ContractTestUtils.test<TestObject>(RawContract, response)
    }

    @Test
    fun repoResponse_ok_greaterThanBufferSize() {
        val random = Random(0)

        val obj = TestObject(random.nextBytes(RawContract.RESPONSE_BUFFER_SIZE * 2))
        val response = Response.ok(obj)

        ContractTestUtils.test<TestObject>(RawContract, response)
    }
}