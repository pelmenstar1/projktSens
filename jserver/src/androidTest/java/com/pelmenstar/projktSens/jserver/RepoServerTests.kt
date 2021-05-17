package com.pelmenstar.projktSens.jserver

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.pelmenstar.projktSens.jserver.repo.DbServerWeatherRepository
import com.pelmenstar.projktSens.serverProtocol.DefaultProtoConfig
import com.pelmenstar.projktSens.serverProtocol.Errors
import com.pelmenstar.projktSens.serverProtocol.ProtoConfig
import com.pelmenstar.projktSens.serverProtocol.repo.RepoClient
import com.pelmenstar.projktSens.serverProtocol.repo.RepoCommands
import com.pelmenstar.projktSens.serverProtocol.repo.RepoRequest
import com.pelmenstar.projktSens.serverProtocol.repo.RepoResponse
import com.pelmenstar.projktSens.shared.buildByteArray
import com.pelmenstar.projktSens.shared.time.ShortDate
import com.pelmenstar.projktSens.shared.time.ShortDateRange
import com.pelmenstar.projktSens.shared.writeInt
import com.pelmenstar.projktSens.weather.models.*
import kotlinx.coroutines.runBlocking
import org.junit.AfterClass
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith

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

            Assert.assertTrue(response.isEmpty())
        }
    }

    @Test
    fun getAvailableDateRange_returns_valid_ok_response_if_db_is_not_empty() {
        runBlocking {
            repo.debugGenDb(ShortDate.now(), 48)

            client.request<ShortDateRange>(RepoCommands.GET_AVAILABLE_DATE_RANGE) ?: throw RuntimeException("Empty response")
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

            Assert.assertEquals(Errors.INVALID_ARGUMENTS, error)
        }
    }

    @Test
    fun genDayReport_returns_invalid_args_error_if_arguments_had_invalid_size() {
        runBlocking {
            val response = client.requestRawResponse<Any>(RepoCommands.GEN_DAY_REPORT, byteArrayOf(1))
            val error = (response as RepoResponse.Error).error

            Assert.assertEquals(Errors.INVALID_ARGUMENTS, error)
        }
    }

    @Test
    fun genDayReport_returns_invalid_args_error_if_date_was_invalid() {
        runBlocking {
            val response = client.requestRawResponse<Any>(RepoCommands.GEN_DAY_REPORT, buildByteArray(4) {
                writeInt(0, ShortDate.NONE)
            })
            val error = (response as RepoResponse.Error).error

            Assert.assertEquals(Errors.INVALID_ARGUMENTS, error)
        }
    }

    @Test
    fun genDayReport_returns_valid_report() {
        runBlocking<Unit> {
            val nowDate = ShortDate.now()
            repo.debugGenDb(nowDate, 48)

            client.request<DayReport>(RepoCommands.GEN_DAY_REPORT, buildByteArray(4) {
                writeInt(0, nowDate)
            })
        }
    }

    @Test
    fun genDayReport_returns_empty_response_if_db_is_empty() {
        runBlocking {
            val nowDate = ShortDate.now()
            repo.clear()

            val response = client.requestRawResponse<Any>(RepoCommands.GEN_DAY_REPORT, buildByteArray(4) {
                writeInt(0, nowDate)
            })

            Assert.assertTrue(response.isEmpty())
        }
    }

    @Test
    fun genDayReport_returns_empty_response_if_date_out_of_range() {
        runBlocking {
            val nowDate = ShortDate.now()

            val response = client.requestRawResponse<Any>(RepoCommands.GEN_DAY_REPORT, buildByteArray(4) {
                writeInt(0, ShortDate.minusDays(nowDate, 3))
            })

            Assert.assertTrue(response.isEmpty())
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

            Assert.assertEquals(Errors.INVALID_ARGUMENTS, error)
        }
    }

    @Test
    fun genDayRangeReport_returns_invalid_args_error_if_arguments_had_invalid_size() {
        runBlocking {
            val response = client.requestRawResponse<Any>(RepoCommands.GEN_DAY_RANGE_REPORT, byteArrayOf(1))
            val error = (response as RepoResponse.Error).error

            Assert.assertEquals(Errors.INVALID_ARGUMENTS, error)
        }
    }

    @Test
    fun genDayRangeReport_returns_invalid_args_error_if_date_range_was_invalid() {
        runBlocking {
            val response = client.requestRawResponse<Any>(RepoCommands.GEN_DAY_RANGE_REPORT, buildByteArray(8) {
                writeInt(0, ShortDate.NONE)
                writeInt(4, ShortDate.NONE)
            })

            val error = (response as RepoResponse.Error).error

            Assert.assertEquals(Errors.INVALID_ARGUMENTS, error)
        }
    }

    @Test
    fun genDayRangeReport_returns_valid_report() {
        runBlocking {
            val nowDate = ShortDate.now()
            repo.debugGenDb(nowDate, 24 * 4)

            val report = client.request<DayRangeReport>(RepoCommands.GEN_DAY_RANGE_REPORT, buildByteArray(8) {
                writeInt(0, nowDate)
                writeInt(4, ShortDate.plusDays(nowDate, 1))
            })

            Assert.assertNotNull(report)
        }
    }

    @Test
    fun genDayRangeReport_returns_empty_response_if_db_is_empty() {
        runBlocking {
            val nowDate = ShortDate.now()
            repo.clear()

            val response = client.requestRawResponse<Any>(RepoCommands.GEN_DAY_RANGE_REPORT, buildByteArray(8) {
                writeInt(0, nowDate)
                writeInt(4, ShortDate.plusDays(nowDate, 1))
            })

            Assert.assertTrue(response.isEmpty())
        }
    }

    @Test
    fun genDayRangeReport_returns_empty_response_if_date_range_out_of_range() {
        runBlocking {
            val nowDate = ShortDate.now()
            repo.debugGenDb(nowDate, 48)

            val response = client.requestRawResponse<Any>(RepoCommands.GEN_DAY_RANGE_REPORT, buildByteArray(8) {
                writeInt(0, ShortDate.minusDays(nowDate, 3))
                writeInt(4, ShortDate.minusDays(nowDate, 2))
            })

            Assert.assertTrue(response.isEmpty())
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

            Assert.assertTrue(response.isEmpty())
        }
    }

    @Test
    fun getLastWeather_returns_valid_weather() {
        runBlocking {
            repo.debugGenDb(ShortDate.now(), 48)

            Assert.assertNotNull(client.request<WeatherInfo>(RepoCommands.GET_LAST_WEATHER))
        }
    }

    companion object {
        private val context = InstrumentationRegistry.getInstrumentation().context
        private val client = RepoClient(DefaultProtoConfig)
        private lateinit var repo: WeatherRepository
        private lateinit var repoServer: RepoServer

        @BeforeClass
        @JvmStatic
        fun before() {
            serverConfig = TestConfig(context)
            repo = serverConfig.sharedRepo
            repoServer = RepoServer(DefaultProtoConfig).also {
                it.start()
            }
        }

        @AfterClass
        @JvmStatic
        fun after() {
            repoServer.stop()
        }
    }

    class TestConfig(context: Context): Config() {
        override val protoConfig: ProtoConfig
            get() = DefaultProtoConfig

        override val sharedRepo: WeatherRepository = DbServerWeatherRepository.inMemory(context)
        override val weatherProvider: WeatherInfoProvider = SensorWeatherProvider(protoConfig)
    }
}