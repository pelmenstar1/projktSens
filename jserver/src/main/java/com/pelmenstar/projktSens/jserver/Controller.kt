package com.pelmenstar.projktSens.jserver

class Controller {
    private val repoServer = Server()

    fun startAll() {
        WeatherMonitor.start()
        repoServer.startOnNewThread()
    }

    fun stopAll() {
        WeatherMonitor.stop()
        repoServer.stop()
    }
}