package com.pelmenstar.projktSens.jserver

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.pelmenstar.projktSens.serverProtocol.Errors
import com.pelmenstar.projktSens.serverProtocol.HostedProtoConfig
import com.pelmenstar.projktSens.serverProtocol.repo.RepoClient
import com.pelmenstar.projktSens.serverProtocol.repo.RepoCommands
import com.pelmenstar.projktSens.serverProtocol.repo.RepoResponse
import com.pelmenstar.projktSens.shared.time.ShortDate
import com.pelmenstar.projktSens.shared.time.ShortDateRange
import com.pelmenstar.projktSens.weather.models.*
import kotlinx.coroutines.runBlocking
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

// Tests only the server request-response logic, not exactly repository logic
@RunWith(AndroidJUnit4::class)
class RepoServerTests {
    //
    // getAvailableDateRange
    //
    @Test
    fun getAvailableDateRange_returns_empty_response_if_db_is_empty() {
        runBlocking {
            repo.clear()
            val response = client.requestRawResponse<Any>(RepoCommands.GET_AVAILABLE_DATE_RANGE)

            assertTrue(response.isEmpty())
        }
    }

    @Test
    fun getAvailableDateRange_returns_valid_ok_response_if_db_is_not_empty() {
        runBlocking {
            repo.debugGenDb(ShortDate.now(), 48)

            val range = client.request<ShortDateRange>(RepoCommands.GET_AVAILABLE_DATE_RANGE)
            assertNotNull(range)
        }
    }

    //
    // genDayReport
    //
    @Test
    fun genDayReport_returns_invalid_args_error_if_no_arguments_were_given() {
        runBlocking {
            val response = client.requestRawResponse<Any>(RepoCommands.GEN_DAY_REPORT)
            val error = (response as RepoResponse.Error).error

            assertEquals(Errors.INVALID_ARGUMENTS, error)
        }
    }

    @Test
    fun genDayReport_returns_invalid_args_error_if_date_was_invalid() {
        runBlocking {
            val response = client.requestRawResponse<Any>(RepoCommands.GEN_DAY_REPORT, ShortDate.NONE)
            val error = (response as RepoResponse.Error).error

            assertEquals(Errors.INVALID_ARGUMENTS, error)
        }
    }

    @Test
    fun genDayReport_returns_valid_report() {
        runBlocking<Unit> {
            val nowDate = ShortDate.now()
            repo.debugGenDb(nowDate, 48)

            val report = client.request<DayReport>(RepoCommands.GEN_DAY_REPORT, nowDate)
            assertNotNull(report)
        }
    }

    @Test
    fun genDayReport_returns_empty_response_if_db_is_empty() {
        runBlocking {
            val nowDate = ShortDate.now()
            repo.clear()

            val response = client.requestRawResponse<Any>(RepoCommands.GEN_DAY_REPORT, nowDate)

            assertTrue(response.isEmpty())
        }
    }

    @Test
    fun genDayReport_returns_empty_response_if_date_out_of_range() {
        runBlocking {
            val nowDate = ShortDate.now()

            val response = client.requestRawResponse<Any>(
                RepoCommands.GEN_DAY_REPORT,
                ShortDate.minusDays(nowDate, 3)
            )

            assertTrue(response.isEmpty())
        }
    }

    //
    // genDayRangeReport
    //
    @Test
    fun genDayRangeReport_returns_invalid_args_error_if_no_arguments_were_given() {
        runBlocking {
            val response = client.requestRawResponse<Any>(RepoCommands.GEN_DAY_RANGE_REPORT)
            val error = (response as RepoResponse.Error).error

            assertEquals(Errors.INVALID_ARGUMENTS, error)
        }
    }

    @Test
    fun genDayRangeReport_returns_valid_report() {
        runBlocking {
            val nowDate = ShortDate.now()
            repo.debugGenDb(nowDate, 24 * 4)

            val report = client.request<DayRangeReport>(
                RepoCommands.GEN_DAY_RANGE_REPORT,
                ShortDateRange(nowDate, ShortDate.plusDays(nowDate, 1))
            )

            assertNotNull(report)
        }
    }

    @Test
    fun genDayRangeReport_returns_empty_response_if_db_is_empty() {
        runBlocking {
            val nowDate = ShortDate.now()
            repo.clear()

            val response = client.requestRawResponse<Any>(
                RepoCommands.GEN_DAY_RANGE_REPORT,
                ShortDateRange(nowDate, ShortDate.plusDays(nowDate, 1))
            )

            assertTrue(response.isEmpty())
        }
    }

    @Test
    fun genDayRangeReport_returns_empty_response_if_date_range_out_of_range() {
        runBlocking {
            val nowDate = ShortDate.now()
            repo.debugGenDb(nowDate, 48)

            val response = client.requestRawResponse<Any>(
                RepoCommands.GEN_DAY_RANGE_REPORT,
                ShortDateRange(ShortDate.minusDays(nowDate, 3), ShortDate.minusDays(nowDate, 2))
            )

            assertTrue(response.isEmpty())
        }
    }

    //
    // getLastWeather
    //
    @Test
    fun getLastWeather_returns_empty_response_if_db_is_empty() {
        runBlocking {
            repo.clear()

            val response = client.requestRawResponse<Any>(RepoCommands.GET_LAST_WEATHER)

            assertTrue(response.isEmpty())
        }
    }

    @Test
    fun getLastWeather_returns_valid_weather() {
        runBlocking {
            repo.debugGenDb(ShortDate.now(), 48)

            assertNotNull(client.request<WeatherInfo>(RepoCommands.GET_LAST_WEATHER))
        }
    }

    companion object {
        private val context = InstrumentationRegistry.getInstrumentation().context
        private lateinit var client: RepoClient
        private lateinit var repo: WeatherRepository
        private lateinit var repoServer: RepoServer

        @BeforeClass
        @JvmStatic
        fun before() {
            val config = TestConfig(context)
            serverConfig = config
            client = RepoClient(HostedProtoConfig(config.host, config.protoConfig))

            repo = serverConfig.sharedRepo
            repoServer = RepoServer().also {
                it.startOnNewThread()
            }
        }

        @AfterClass
        @JvmStatic
        fun after() {
            repoServer.stop()
        }
    }
}