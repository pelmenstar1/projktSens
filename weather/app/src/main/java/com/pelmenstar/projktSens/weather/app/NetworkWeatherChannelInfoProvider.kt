package com.pelmenstar.projktSens.weather.app

import com.pelmenstar.projktSens.serverProtocol.Client
import com.pelmenstar.projktSens.serverProtocol.Commands
import com.pelmenstar.projktSens.serverProtocol.ProtoConfig
import com.pelmenstar.projktSens.weather.models.WeatherChannelInfoProvider

class NetworkWeatherChannelInfoProvider(config: ProtoConfig) : WeatherChannelInfoProvider {
    private val client = Client(config)

    override val receiveInterval: Long = config.weatherChannelReceiveInterval.toLong()

    override suspend fun getNextWeatherTime(): Long {
        return client.request(Commands.GET_NEXT_WEATHER_TIME)!!
    }
}