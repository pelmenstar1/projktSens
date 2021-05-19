package com.pelmenstar.projktSens.jserver

import android.content.Context
import com.pelmenstar.projktSens.jserver.repo.DbServerWeatherRepository
import com.pelmenstar.projktSens.serverProtocol.DefaultProtoConfig
import com.pelmenstar.projktSens.serverProtocol.ProtoConfig
import com.pelmenstar.projktSens.weather.models.WeatherInfoProvider
import com.pelmenstar.projktSens.weather.models.WeatherRepository

private var _serverConfig: Config? = null
var serverConfig: Config
    get() = _serverConfig ?: throw NullPointerException("serverConfig")
    set(value) {
        _serverConfig = value
    }

abstract class Config {
    abstract val protoConfig: ProtoConfig
    abstract val sharedRepo: WeatherRepository
    abstract val weatherProvider: WeatherInfoProvider
    abstract val loggerConfig: LoggerConfig
}

class MainConfig(context: Context): Config() {
    override val protoConfig
        get() = DefaultProtoConfig

    override val sharedRepo = DbServerWeatherRepository.file(context)
    override val weatherProvider = SensorWeatherProvider(protoConfig)
    override val loggerConfig = LoggerConfig(
        AndroidLogDelegate,
        LogLevel.DEBUG
    )
}