package com.pelmenstar.projktSens.jserver.di

import android.content.Context
import com.pelmenstar.projktSens.jserver.*
import com.pelmenstar.projktSens.jserver.logging.AndroidLogDelegate
import com.pelmenstar.projktSens.jserver.logging.LogLevel
import com.pelmenstar.projktSens.jserver.logging.LoggerConfig
import com.pelmenstar.projktSens.jserver.repo.DbServerWeatherRepository
import com.pelmenstar.projktSens.serverProtocol.ContractType
import com.pelmenstar.projktSens.serverProtocol.ProtoConfig
import com.pelmenstar.projktSens.serverProtocol.ProtoConfigImpl
import com.pelmenstar.projktSens.weather.models.WeatherInfoProvider
import com.pelmenstar.projktSens.weather.models.WeatherRepository
import dagger.Module
import dagger.Provides
import java.net.InetAddress
import java.net.InetSocketAddress

@Module
open class AppModule(private val context: Context) {
    private val host by lazy { InetUtils.getInetAddress(context)!! }
    private val weatherRepo by lazy { DbServerWeatherRepository.file(context) }
    private val weatherProvider by lazy { SensorWeatherProvider() }
    private val loggerConfig by lazy {
        LoggerConfig(AndroidLogDelegate, minLogLevel = LogLevel.DEBUG)
    }

    @Provides
    open fun host(): InetAddress = host

    @Provides
    open fun protoConfig(): ProtoConfig {
        val prefs = AppPreferences.of(context)
        return ProtoConfigImpl(
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