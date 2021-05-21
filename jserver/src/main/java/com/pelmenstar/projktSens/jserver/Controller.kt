package com.pelmenstar.projktSens.jserver

import com.pelmenstar.projktSens.shared.time.ShortDate
import com.pelmenstar.projktSens.weather.models.debugGenDb
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class Controller(private val config: Config) {
    private val repoServer: RepoServer
    private val weatherChannelInfoServer: WeatherChannelInfoServer
    private val statusServer: StatusServer

    init {
        serverConfig = config

        repoServer = RepoServer()
        weatherChannelInfoServer = WeatherChannelInfoServer()
        statusServer = StatusServer()
    }

    fun startAll() {
        WeatherMonitor.start()
        repoServer.start()
        weatherChannelInfoServer.start()
        statusServer.start()
    }

    fun stopAll() {
        WeatherMonitor.stop()
        repoServer.stop()
        weatherChannelInfoServer.stop()
        statusServer.stop()
    }

    fun clearRepository() {
        GlobalScope.launch {
            config.sharedRepo.clear()
        }
    }

    fun debugGenDb() {
        GlobalScope.launch {
            val startDate = ShortDate.minusDays(ShortDate.now(), 31)

            config.sharedRepo.debugGenDb(
                startDate,
                89280
            )
        }
    }
}