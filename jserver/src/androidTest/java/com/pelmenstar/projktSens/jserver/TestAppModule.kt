package com.pelmenstar.projktSens.jserver

import android.content.Context
import com.pelmenstar.projktSens.jserver.di.AppModule
import com.pelmenstar.projktSens.jserver.logging.AndroidLogDelegate
import com.pelmenstar.projktSens.jserver.logging.LogLevel
import com.pelmenstar.projktSens.jserver.logging.LoggerConfig
import com.pelmenstar.projktSens.jserver.repo.DbServerWeatherRepository
import com.pelmenstar.projktSens.serverProtocol.ProtoConfig
import com.pelmenstar.projktSens.serverProtocol.RawContract
import com.pelmenstar.projktSens.weather.models.WeatherRepository
import java.net.InetSocketAddress

class TestAppModule(context: Context): AppModule(context) {
    private val weatherRepo = DbServerWeatherRepository.inMemory(context)

    override fun protoConfig(): ProtoConfig {
        return ProtoConfig(
            InetSocketAddress(host(), 10001),
            10000,
            RawContract
        )
    }

    override fun weatherRepository(): WeatherRepository = weatherRepo

    override fun loggerConfig(): LoggerConfig {
        return LoggerConfig(AndroidLogDelegate, LogLevel.DEBUG)
    }
}