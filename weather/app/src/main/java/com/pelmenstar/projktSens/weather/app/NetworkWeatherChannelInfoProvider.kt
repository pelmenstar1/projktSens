package com.pelmenstar.projktSens.weather.app

import com.pelmenstar.projktSens.serverProtocol.ProtoConfig
import com.pelmenstar.projktSens.serverProtocol.socketAddress
import com.pelmenstar.projktSens.shared.connectSuspend
import com.pelmenstar.projktSens.shared.getLong
import com.pelmenstar.projktSens.shared.readNSuspend
import com.pelmenstar.projktSens.weather.models.WeatherChannelInfoProvider
import java.net.Socket

class NetworkWeatherChannelInfoProvider(protoConfig: ProtoConfig) : WeatherChannelInfoProvider {
    private val address = protoConfig.socketAddress { serverStatusPort }

    override val receiveInterval: Long = protoConfig.weatherChannelReceiveInterval.toLong()

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