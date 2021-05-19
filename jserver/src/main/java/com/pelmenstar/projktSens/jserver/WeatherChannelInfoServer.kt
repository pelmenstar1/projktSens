package com.pelmenstar.projktSens.jserver

import com.pelmenstar.projktSens.serverProtocol.ProtoConfig
import com.pelmenstar.projktSens.serverProtocol.socketAddress
import com.pelmenstar.projktSens.shared.buildByteArray
import com.pelmenstar.projktSens.shared.writeSuspend
import com.pelmenstar.projktSens.shared.writeLong
import java.net.Socket

class WeatherChannelInfoServer(config: ProtoConfig): ServerBase(
   config.socketAddress { weatherChannelInfoPort },
) {
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