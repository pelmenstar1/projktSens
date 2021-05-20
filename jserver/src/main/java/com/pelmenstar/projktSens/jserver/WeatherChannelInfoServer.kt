package com.pelmenstar.projktSens.jserver

import com.pelmenstar.projktSens.shared.buildByteArray
import com.pelmenstar.projktSens.shared.writeLong
import com.pelmenstar.projktSens.shared.writeSuspend
import java.net.Socket

class WeatherChannelInfoServer: ServerBase({ weatherChannelInfoPort }) {
    override suspend fun processClient(client: Socket) {
        val output = client.getOutputStream()
        val nextTime = WeatherMonitor.getNextWeatherRequestTime()
        val waitTime = nextTime - System.currentTimeMillis()
        val buffer = buildByteArray(8) {
            writeLong(0, waitTime)
        }

        output.writeSuspend(buffer)
    }
}