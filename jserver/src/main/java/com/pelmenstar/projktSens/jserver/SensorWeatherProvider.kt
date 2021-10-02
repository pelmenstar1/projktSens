package com.pelmenstar.projktSens.jserver

import android.os.Build
import androidx.annotation.RequiresApi
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
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousSocketChannel

class SensorWeatherProvider : WeatherInfoProvider {
    private val address = InetSocketAddress(InetAddress.getLoopbackAddress(), 10001)

    override suspend fun getWeather(): WeatherInfo {
        return if(Build.VERSION.SDK_INT >= 26) {
            getWeatherAsync()
        } else {
            getWeatherSync()
        }
    }

    private suspend fun getWeatherSync(): WeatherInfo {
        try {
            return Socket().use { socket ->
                socket.connectSuspend(address, 5000)

                val input = socket.getInputStream()
                val output = socket.getOutputStream()

                output.writeSuspend(MSG)

                val buffer = input.readNSuspend(12)

                val temp = buffer.getFloat(0)
                val hum = buffer.getFloat(4)
                val press = buffer.getFloat(8)

                createWeather(temp, hum, press)
            }
        } catch (e: IOException) {
            throw RuntimeException("Cannot receive weather", e)
        }
    }

    @RequiresApi(26)
    private suspend fun getWeatherAsync(): WeatherInfo {
        try {
            return AsynchronousSocketChannel.open().use { channel ->
                channel.connectSuspend(address, 5000)

                channel.writeSuspend(MSG_BUFFER)
                val buffer = channel.readNSuspend(12)

                val temp = buffer.float
                val hum = buffer.float
                val press = buffer.float

                createWeather(temp, hum, press)
            }
        } catch (e: IOException) {
            throw RuntimeException("Cannot receive weather", e)
        }
    }

    private fun createWeather(temp: Float, hum: Float, press: Float): WeatherInfo {
        return WeatherInfo(
            ValueUnitsPacked.CELSIUS_MM_OF_MERCURY,
            ShortDateTime.now(),
            temp, hum, press
        )
    }

    companion object {
        private val MSG = byteArrayOf(0)
        private val MSG_BUFFER = ByteBuffer.wrap(MSG)
    }
}