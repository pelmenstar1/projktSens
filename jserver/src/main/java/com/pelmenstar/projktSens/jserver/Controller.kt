package com.pelmenstar.projktSens.jserver

import com.pelmenstar.projktSens.shared.time.ShortDate
import com.pelmenstar.projktSens.weather.models.debugGenDb
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Controller(private val config: Config) {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val repoServer: RepoServer
    private val weatherChannelInfoServer: WeatherChannelInfoServer

    init {
        serverConfig = config

        repoServer = RepoServer()
        weatherChannelInfoServer = WeatherChannelInfoServer()
    }

    fun startAll() {
        WeatherMonitor.start()
        repoServer.start()
        weatherChannelInfoServer.start()
    }

    fun stopAll() {
        WeatherMonitor.stop()
        repoServer.stop()
        weatherChannelInfoServer.stop()
    }

    fun clearRepository() {
        scope.launch {
            config.sharedRepo.clear()
        }
    }

    fun debugGenDb() {
        scope.launch {
            val startDate = ShortDate.minusDays(ShortDate.now(), 31)

            config.sharedRepo.debugGenDb(
                startDate,
                89280
            )
        }
    }
}