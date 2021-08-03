package com.pelmenstar.projktSens.weather.app

import com.pelmenstar.projktSens.serverProtocol.HostedProtoConfig
import com.pelmenstar.projktSens.serverProtocol.repo.RepoClient
import com.pelmenstar.projktSens.serverProtocol.repo.RepoCommands
import com.pelmenstar.projktSens.shared.connectSuspend
import com.pelmenstar.projktSens.shared.getLong
import com.pelmenstar.projktSens.shared.readNSuspend
import com.pelmenstar.projktSens.weather.models.WeatherChannelInfoProvider
import java.net.Socket

class NetworkWeatherChannelInfoProvider(config: HostedProtoConfig) : WeatherChannelInfoProvider {
    private val client = RepoClient(config)

    override val receiveInterval: Long = config.weatherChannelReceiveInterval.toLong()

    override suspend fun getWaitTimeForNextWeather(): Long {
        var waitTime = client.request<Long>(RepoCommands.GET_WAIT_TIME_FOR_NEXT_WEATHER) ?: 0
        if(waitTime < 0) {
            waitTime = 0
        }

        return waitTime
    }
}