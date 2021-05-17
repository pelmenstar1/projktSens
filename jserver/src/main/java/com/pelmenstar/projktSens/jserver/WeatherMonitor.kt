package com.pelmenstar.projktSens.jserver

import android.util.Log
import com.pelmenstar.projktSens.weather.models.WeatherInfoProvider
import com.pelmenstar.projktSens.weather.models.WeatherRepository
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicLong

/**
 * Requests weather in [WeatherInfoProvider] and puts it to default [WeatherRepository]
 */
object WeatherMonitor {
    private const val TAG = "WeatherMonitor"

    @JvmStatic
    private var job: Job? = null
    private val nextWeatherRequestTime = AtomicLong()

    /**
     * Returns epoch millis when next weather will be requested
     */
    fun getNextWeatherRequestTime(): Long {
        if(job == null) {
            throw IllegalStateException("Monitor is not started")
        }

        return nextWeatherRequestTime.get()
    }

    /**
     * Starts monitoring weather. Monitor will not be started if monitor is already running
     */
    fun start() {
        if(job != null) {
            Log.e(TAG, "monitor is already running")
            return
        }

        job = GlobalScope.launch {
            val config = serverConfig
            val protoConfig = config.protoConfig
            val dataProvider = config.weatherProvider
            val interval = protoConfig.weatherChannelReceiveInterval.toLong()

            while (isActive) {
                try {
                    nextWeatherRequestTime.set(System.currentTimeMillis() + interval)

                    delay(interval)
                } catch (e: Exception) {
                    Log.e(TAG, null, e)
                }
            }
        }
    }

    /**
     * Stops monitor
     */
    fun stop() {
        job?.cancel()
        job = null
    }
}