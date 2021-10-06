package com.pelmenstar.projktSens.jserver.di

import android.content.Context
import com.pelmenstar.projktSens.jserver.*
import com.pelmenstar.projktSens.jserver.logging.AndroidLogDelegate
import com.pelmenstar.projktSens.jserver.logging.LogLevel
import com.pelmenstar.projktSens.jserver.logging.LoggerConfig
import com.pelmenstar.projktSens.jserver.repo.DbServerWeatherRepository
import com.pelmenstar.projktSens.serverProtocol.ContractType
import com.pelmenstar.projktSens.serverProtocol.ProtoConfig
import com.pelmenstar.projktSens.shared.time.ShortDateRange
import com.pelmenstar.projktSens.weather.models.*
import dagger.Module
import dagger.Provides
import java.net.InetAddress
import java.net.InetSocketAddress

@Module
open class AppModule(private val context: Context) {
    private val host by lazy { DeviceInetUtils.getInetAddress(context)!! }
    private val weatherRepo by lazy {
        object: WeatherRepository {
            override suspend fun putMany(values: Array<WeatherInfo>) {
            }

            override suspend fun put(weather: WeatherInfo) {
            }

            override suspend fun clear() {
            }

            override suspend fun getDayReport(date: Int): DayReport {
                return RandomWeatherDataSource.getDayReport(date)
            }

            override suspend fun getDayRangeReport(start: Int, end: Int): DayRangeReport {
                return RandomWeatherDataSource.getDayRangeReport(start, end)
            }

            override suspend fun getAvailableDateRange(): ShortDateRange {
                return RandomWeatherDataSource.getAvailableDateRange()
            }

            override suspend fun getLastWeather(): WeatherInfo {
                return RandomWeatherDataSource.getLastWeather()
            }

        }
    }
    private val weatherProvider by lazy { SensorWeatherProvider() }
    private val loggerConfig by lazy {
        LoggerConfig(AndroidLogDelegate, minLogLevel = LogLevel.DEBUG)
    }

    @Provides
    open fun host(): InetAddress = host

    @Provides
    open fun protoConfig(): ProtoConfig {
        val prefs = AppPreferences.of(context)
        return ProtoConfig(
            InetSocketAddress(host, prefs.serverPort),
            prefs.weatherSendInterval,
            ContractType.toObject(prefs.serverContract)
        )
    }

    @Provides
    open fun weatherRepository(): WeatherRepository = weatherRepo

    @Provides
    open fun weatherProvider(): WeatherInfoProvider = weatherProvider

    @Provides
    open fun loggerConfig(): LoggerConfig = loggerConfig

    @Provides
    fun server(): Server {
        return Server(protoConfig(), loggerConfig(), weatherRepository())
    }

    @Provides
    fun weatherMonitor(): WeatherMonitor {
        return WeatherMonitor(
            protoConfig(),
            loggerConfig(),
            weatherProvider(),
            weatherRepository()
        )
    }
}