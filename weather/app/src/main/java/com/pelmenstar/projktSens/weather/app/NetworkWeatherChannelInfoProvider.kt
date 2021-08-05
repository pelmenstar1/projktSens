package com.pelmenstar.projktSens.weather.app

import com.pelmenstar.projktSens.serverProtocol.ProtoConfig
import com.pelmenstar.projktSens.serverProtocol.Client
import com.pelmenstar.projktSens.serverProtocol.Commands
import com.pelmenstar.projktSens.weather.models.WeatherChannelInfoProvider

class NetworkWeatherChannelInfoProvider(config: ProtoConfig) : WeatherChannelInfoProvider {
    private val client = Client(config)

    override val receiveInterval: Long = config.weatherChannelReceiveInterval.toLong()

    override suspend fun getWaitTimeForNextWeather(): Long {
        var waitTime = client.request<Long>(Commands.GET_WAIT_TIME_FOR_NEXT_WEATHER) ?: 0
        if(waitTime < 0) {
            waitTime = 0
        }

        return waitTime
    }
}