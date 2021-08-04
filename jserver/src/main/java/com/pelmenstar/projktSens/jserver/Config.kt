package com.pelmenstar.projktSens.jserver

import android.content.Context
import com.pelmenstar.projktSens.jserver.logging.AndroidLogDelegate
import com.pelmenstar.projktSens.jserver.repo.DbServerWeatherRepository
import com.pelmenstar.projktSens.serverProtocol.ProtoConfig
import com.pelmenstar.projktSens.serverProtocol.ProtoConfigImpl
import com.pelmenstar.projktSens.serverProtocol.repo.RepoContractType
import com.pelmenstar.projktSens.weather.models.WeatherInfoProvider
import com.pelmenstar.projktSens.weather.models.WeatherRepository
import java.net.InetAddress
import java.net.InetSocketAddress

private var _serverConfig: Config? = null
var serverConfig: Config
    get() = _serverConfig ?: throw NullPointerException("serverConfig")
    set(value) {
        _serverConfig = value
    }

abstract class Config {
    abstract val host: InetAddress
    abstract val protoConfig: ProtoConfig
    abstract val sharedRepo: WeatherRepository
    abstract val weatherProvider: WeatherInfoProvider
    abstract val loggerConfig: LoggerConfig
}

class MainConfig(private val context: Context): Config() {
    override val host: InetAddress = InetUtils.getInetAddress(context)
        ?: throw RuntimeException("Device is not connected to internet")

    override val protoConfig: ProtoConfig
        get() {
            val prefs = AppPreferences.of(context)
            return ProtoConfigImpl(
                InetSocketAddress(host, prefs.repoPort),
                prefs.weatherSendInterval,
                RepoContractType.get(prefs.serverContract)
            )
        }

    override val sharedRepo = DbServerWeatherRepository.file(context)
    override val weatherProvider = SensorWeatherProvider()
    override val loggerConfig = LoggerConfig(
        AndroidLogDelegate,
        LogLevel.DEBUG
    )

}