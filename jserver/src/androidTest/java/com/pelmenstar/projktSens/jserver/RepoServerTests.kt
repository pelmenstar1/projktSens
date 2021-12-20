package com.pelmenstar.projktSens.jserver

import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.pelmenstar.projktSens.jserver.di.DaggerAppComponent
import com.pelmenstar.projktSens.serverProtocol.*
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

@RunWith(AndroidJUnit4::class)
//@Ignore
class RepoServerTests {
    //
    // getAvailableDateRange
    //
    @Test
    fun getAvailableDateRange_returns_empty_response_if_db_is_empty() {
        runWithBlockingAndAsyncClients { client ->
            weatherRepo.clear()

            val response = client.requestRawResponse<Any>(Commands.GET_AVAILABLE_DATE_RANGE)

            assertTrue(response.isEmpty())
        }
    }

    @Test
    fun getAvailableDateRange_returns_valid_ok_response_if_db_is_not_empty() {
        runWithBlockingAndAsyncClients { client ->
            weatherRepo.debugGenDb(ShortDate.now(), 48)

            val range = client.request<ShortDateRange>(Commands.GET_AVAILABLE_DATE_RANGE)
            assertNotNull(range)
        }
    }

    //
    // genDayReport
    //
    @Test
    fun genDayReport_returns_invalid_args_error_if_no_arguments_were_given() {
        runWithBlockingAndAsyncClients { client ->
            val response = client.requestRawResponse<Any>(Commands.GET_DAY_REPORT)
            val error = (response as Response.Error).error

            assertEquals(Errors.INVALID_ARGUMENTS, error)
        }
    }

    @Test
    fun genDayReport_returns_invalid_args_error_if_date_was_invalid() {
        runWithBlockingAndAsyncClients { client ->
            val response = client.requestRawResponse<Any>(
                Commands.GET_DAY_REPORT, Request.Argument.Integer(ShortDate.NONE)
            )
            val error = (response as Response.Error).error

            assertEquals(Errors.INVALID_ARGUMENTS, error)
        }
    }

    @Test
    fun genDayReport_returns_valid_report() {
        runWithBlockingAndAsyncClients { client ->
            val nowDate = ShortDate.now()
            weatherRepo.debugGenDb(nowDate, 48)

            val report = client.request<DayReport>(
                Commands.GET_DAY_REPORT, Request.Argument.Integer(nowDate)
            )
            assertNotNull(report)
        }
    }

    @Test
    fun genDayReport_returns_empty_response_if_db_is_empty() {
        runWithBlockingAndAsyncClients { client ->
            val nowDate = ShortDate.now()
            weatherRepo.clear()

            val response = client.requestRawResponse<Any>(
                Commands.GET_DAY_REPORT, Request.Argument.Integer(nowDate)
            )

            assertTrue(response.isEmpty())
        }
    }

    @Test
    fun genDayReport_returns_empty_response_if_date_out_of_range() {
        runWithBlockingAndAsyncClients { client ->
            val nowDate = ShortDate.now()

            val response = client.requestRawResponse<Any>(
                Commands.GET_DAY_REPORT,
                Request.Argument.Integer(ShortDate.minusDays(nowDate, 3))
            )

            assertTrue(response.isEmpty())
        }
    }

    //
    // genDayRangeReport
    //
    @Test
    fun genDayRangeReport_returns_invalid_args_error_if_no_arguments_were_given() {
        runWithBlockingAndAsyncClients { client ->
            val response = client.requestRawResponse<Any>(Commands.GET_DAY_RANGE_REPORT)
            val error = (response as Response.Error).error

            assertEquals(Errors.INVALID_ARGUMENTS, error)
        }
    }

    @Test
    fun genDayRangeReport_returns_valid_report() {
        runWithBlockingAndAsyncClients { client ->
            val nowDate = ShortDate.now()
            weatherRepo.debugGenDb(nowDate, 24 * 4)

            val report = client.request<DayRangeReport>(
                Commands.GET_DAY_RANGE_REPORT,
                Request.Argument.DateRange(nowDate, ShortDate.plusDays(nowDate, 1))
            )

            assertNotNull(report)
        }
    }

    @Test
    fun genDayRangeReport_returns_empty_response_if_db_is_empty() {
        runWithBlockingAndAsyncClients { client ->
            val nowDate = ShortDate.now()
            weatherRepo.clear()

            val response = client.requestRawResponse<Any>(
                Commands.GET_DAY_RANGE_REPORT,
                Request.Argument.DateRange(nowDate, ShortDate.plusDays(nowDate, 1))
            )

            assertTrue(response.isEmpty())
        }
    }

    @Test
    fun genDayRangeReport_returns_empty_response_if_date_range_out_of_range() {
        runWithBlockingAndAsyncClients { client ->
            val nowDate = ShortDate.now()
            weatherRepo.debugGenDb(nowDate, 48)

            val response = client.requestRawResponse<Any>(
                Commands.GET_DAY_RANGE_REPORT,
                Request.Argument.DateRange(
                    ShortDate.minusDays(nowDate, 3),
                    ShortDate.minusDays(nowDate, 2)
                )
            )

            assertTrue(response.isEmpty())
        }
    }

    //
    // getLastWeather
    //
    @Test
    fun getLastWeather_returns_empty_response_if_db_is_empty() {
        runWithBlockingAndAsyncClients { client ->
            weatherRepo.clear()

            val response = client.requestRawResponse<Any>(Commands.GET_LAST_WEATHER)

            assertTrue(response.isEmpty())
        }
    }

    @Test
    fun getLastWeather_returns_valid_weather() {
        runWithBlockingAndAsyncClients { client ->
            weatherRepo.debugGenDb(ShortDate.now(), 48)

            assertNotNull(client.request<WeatherInfo>(Commands.GET_LAST_WEATHER))
        }
    }

    companion object {
        private val context = InstrumentationRegistry.getInstrumentation().context

        private lateinit var weatherRepo: WeatherRepository
        private lateinit var server: RepoServer
        private lateinit var blockingClient: Client
        private lateinit var asyncClient: Client

        @BeforeClass
        @JvmStatic
        fun before() {
            val component = DaggerAppComponent.builder().appModule(TestAppModule(context)).build()

            val protoConfig = component.protoConfig()
            blockingClient = Client(protoConfig, true)
            asyncClient = Client(protoConfig, false)

            weatherRepo = component.weatherRepository()
            server = component.repoServer().also {
                it.startOnNewThread()
                Thread.sleep(2000)
            }
        }

        @AfterClass
        @JvmStatic
        fun after() {
            server.stop()
        }

        private fun runWithBlockingAndAsyncClients(block: suspend (Client) -> Unit) {
            runBlocking {
                block(blockingClient)
                if(Build.VERSION.SDK_INT >= 26) {
                    block(asyncClient)
                }
            }
        }
    }
}