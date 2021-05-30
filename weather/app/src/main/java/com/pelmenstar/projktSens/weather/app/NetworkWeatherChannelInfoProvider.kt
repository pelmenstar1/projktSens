package com.pelmenstar.projktSens.weather.app

import com.pelmenstar.projktSens.serverProtocol.HostedProtoConfig
import com.pelmenstar.projktSens.shared.connectSuspend
import com.pelmenstar.projktSens.shared.getLong
import com.pelmenstar.projktSens.shared.readNSuspend
import com.pelmenstar.projktSens.weather.models.WeatherChannelInfoProvider
import java.net.Socket

class NetworkWeatherChannelInfoProvider(config: HostedProtoConfig) : WeatherChannelInfoProvider {
    private val address = config.socketAddress { weatherChannelInfoPort }
    override val receiveInterval: Long = config.weatherChannelReceiveInterval.toLong()

    override suspend fun getWaitTimeForNextWeather(): Long {
        return Socket().use { socket ->
            socket.connectSuspend(address)
            socket.soTimeout = 5000

            val input = socket.getInputStream()
            val buffer = input.readNSuspend(8)
            var waitTime = buffer.getLong(0)

            if (waitTime < 0) {
                waitTime = 0
            }

            waitTime
        }
    }
}