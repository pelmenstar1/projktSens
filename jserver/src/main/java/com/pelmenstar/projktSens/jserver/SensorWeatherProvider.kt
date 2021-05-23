package com.pelmenstar.projktSens.jserver

import com.pelmenstar.projktSens.shared.connectSuspend
import com.pelmenstar.projktSens.shared.getFloat
import com.pelmenstar.projktSens.shared.readNSuspend
import com.pelmenstar.projktSens.shared.time.ShortDateTime
import com.pelmenstar.projktSens.shared.writeSuspend
import com.pelmenstar.projktSens.weather.models.ValueUnitsPacked
import com.pelmenstar.projktSens.weather.models.WeatherInfo
import com.pelmenstar.projktSens.weather.models.WeatherInfoProvider
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

class SensorWeatherProvider: WeatherInfoProvider {
    private val address = InetSocketAddress(InetAddress.getLoopbackAddress(), 10001)

    override suspend fun getWeather(): WeatherInfo {
        try {
            Socket().use { socket ->
                socket.connectSuspend(address)
                socket.soTimeout = 10000

                val input = socket.getInputStream()
                val output = socket.getOutputStream()

                output.writeSuspend(MSG)

                val buffer = input.readNSuspend(12)

                val temp = buffer.getFloat(0)
                val hum = buffer.getFloat(4)
                val press = buffer.getFloat(8)

                return WeatherInfo(
                    ValueUnitsPacked.CELSIUS_MM_OF_MERCURY,
                    ShortDateTime.now(),
                    temp,
                    hum,
                    press
                )
            }
        } catch (e: IOException) {
            throw RuntimeException("Cannot receive weather", e)
        }
    }

    companion object {
        private val MSG = byteArrayOf(0)
    }
}