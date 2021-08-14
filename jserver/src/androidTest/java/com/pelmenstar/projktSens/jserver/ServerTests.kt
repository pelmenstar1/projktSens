package com.pelmenstar.projktSens.jserver

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

// Tests only the server request-response logic, not exactly repository logic
@RunWith(AndroidJUnit4::class)
class ServerTests {
    //
    // getAvailableDateRange
    //
    @Test
    fun getAvailableDateRange_returns_empty_response_if_db_is_empty() {
        runBlocking {
            weatherRepo.clear()
            val response = client.requestRawResponse<Any>(Commands.GET_AVAILABLE_DATE_RANGE)

            assertTrue(response.isEmpty())
        }
    }

    @Test
    fun getAvailableDateRange_returns_valid_ok_response_if_db_is_not_empty() {
        runBlocking {
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
        runBlocking {
            val response = client.requestRawResponse<Any>(Commands.GET_DAY_REPORT)
            val error = (response as Response.Error).error

            assertEquals(Errors.INVALID_ARGUMENTS, error)
        }
    }

    @Test
    fun genDayReport_returns_invalid_args_error_if_date_was_invalid() {
        runBlocking {
            val response = client.requestRawResponse<Any>(Commands.GET_DAY_REPORT, ShortDate.NONE)
            val error = (response as Response.Error).error

            assertEquals(Errors.INVALID_ARGUMENTS, error)
        }
    }

    @Test
    fun genDayReport_returns_valid_report() {
        runBlocking<Unit> {
            val nowDate = ShortDate.now()
            weatherRepo.debugGenDb(nowDate, 48)

            val report = client.request<DayReport>(Commands.GET_DAY_REPORT, nowDate)
            assertNotNull(report)
        }
    }

    @Test
    fun genDayReport_returns_empty_response_if_db_is_empty() {
        runBlocking {
            val nowDate = ShortDate.now()
            weatherRepo.clear()

            val response = client.requestRawResponse<Any>(Commands.GET_DAY_REPORT, nowDate)

            assertTrue(response.isEmpty())
        }
    }

    @Test
    fun genDayReport_returns_empty_response_if_date_out_of_range() {
        runBlocking {
            val nowDate = ShortDate.now()

            val response = client.requestRawResponse<Any>(
                Commands.GET_DAY_REPORT,
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
            val response = client.requestRawResponse<Any>(Commands.GET_DAY_RANGE_REPORT)
            val error = (response as Response.Error).error

            assertEquals(Errors.INVALID_ARGUMENTS, error)
        }
    }

    @Test
    fun genDayRangeReport_returns_valid_report() {
        runBlocking {
            val nowDate = ShortDate.now()
            weatherRepo.debugGenDb(nowDate, 24 * 4)

            val report = client.request<DayRangeReport>(
                Commands.GET_DAY_RANGE_REPORT,
                ShortDateRange(nowDate, ShortDate.plusDays(nowDate, 1))
            )

            assertNotNull(report)
        }
    }

    @Test
    fun genDayRangeReport_returns_empty_response_if_db_is_empty() {
        runBlocking {
            val nowDate = ShortDate.now()
            weatherRepo.clear()

            val response = client.requestRawResponse<Any>(
                Commands.GET_DAY_RANGE_REPORT,
                ShortDateRange(nowDate, ShortDate.plusDays(nowDate, 1))
            )

            assertTrue(response.isEmpty())
        }
    }

    @Test
    fun genDayRangeReport_returns_empty_response_if_date_range_out_of_range() {
        runBlocking {
            val nowDate = ShortDate.now()
            weatherRepo.debugGenDb(nowDate, 48)

            val response = client.requestRawResponse<Any>(
                Commands.GET_DAY_RANGE_REPORT,
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
            weatherRepo.clear()

            val response = client.requestRawResponse<Any>(Commands.GET_LAST_WEATHER)

            assertTrue(response.isEmpty())
        }
    }

    @Test
    fun getLastWeather_returns_valid_weather() {
        runBlocking {
            weatherRepo.debugGenDb(ShortDate.now(), 48)

            assertNotNull(client.request<WeatherInfo>(Commands.GET_LAST_WEATHER))
        }
    }

    companion object {
        private val context = InstrumentationRegistry.getInstrumentation().context
        private lateinit var client: Client
        private lateinit var weatherRepo: WeatherRepository
        private lateinit var server: Server

        @BeforeClass
        @JvmStatic
        fun before() {
            val component = DaggerAppComponent.builder().appModule(TestAppModule(context)).build()
            client = Client(component.protoConfig())

            weatherRepo = component.weatherRepository()
            server = component.server().also {
                it.startOnNewThread()
            }
        }

        @AfterClass
        @JvmStatic
        fun after() {
            server.stop()
        }
    }
}