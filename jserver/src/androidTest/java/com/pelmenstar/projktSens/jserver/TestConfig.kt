package com.pelmenstar.projktSens.jserver

import android.content.Context
import com.pelmenstar.projktSens.jserver.logging.AndroidLogDelegate
import com.pelmenstar.projktSens.jserver.repo.DbServerWeatherRepository
import com.pelmenstar.projktSens.serverProtocol.ProtoConfig
import com.pelmenstar.projktSens.serverProtocol.ProtoConfigImpl
import com.pelmenstar.projktSens.serverProtocol.RawContract
import com.pelmenstar.projktSens.weather.models.WeatherInfoProvider
import com.pelmenstar.projktSens.weather.models.WeatherRepository
import java.net.InetAddress
import java.net.InetSocketAddress

class TestConfig(context: Context): Config() {
    override val host: InetAddress =
        InetUtils.getInetAddress(context)!!
    override val protoConfig: ProtoConfig =  ProtoConfigImpl(
        InetSocketAddress(host, 10001),
        10000,
        RawContract
    )

    override val sharedRepo: WeatherRepository = DbServerWeatherRepository.inMemory(context)
    override val weatherProvider: WeatherInfoProvider = SensorWeatherProvider()
    override val loggerConfig = LoggerConfig(
        AndroidLogDelegate,
        LogLevel.DEBUG
    )
}