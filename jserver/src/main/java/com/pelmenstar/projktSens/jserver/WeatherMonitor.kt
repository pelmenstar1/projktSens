package com.pelmenstar.projktSens.jserver

import com.pelmenstar.projktSens.jserver.logging.Logger
import com.pelmenstar.projktSens.jserver.logging.LoggerConfig
import com.pelmenstar.projktSens.serverProtocol.ProtoConfig
import com.pelmenstar.projktSens.weather.models.WeatherInfoProvider
import com.pelmenstar.projktSens.weather.models.WeatherRepository
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicLong

/**
 * Requests weather in [WeatherInfoProvider] and puts it to default [WeatherRepository]
 */
class WeatherMonitor(
    private val protoConfig: ProtoConfig,
    loggerConfig: LoggerConfig,
    private val dataProvider: WeatherInfoProvider,
    private val weatherRepo: WeatherRepository,
) {
    private val scope = CoroutineScope(Dispatchers.Default)

    private var job: Job? = null
    private val nextWeatherRequestTime = AtomicLong()

    private val log = Logger("WeatherMonitor", loggerConfig)

    /**
     * Returns epoch millis when next weather will be requested
     */
    fun getNextWeatherRequestTime(): Long {
        if (job == null) {
            throw IllegalStateException("Monitor is not started")
        }

        return nextWeatherRequestTime.get()
    }

    /**
     * Starts monitoring weather. Monitor will not be started if monitor is already running
     */
    fun start() {
        if (job != null) {
            log error "monitor is already running"
            return
        }

        job = scope.launch {
            val interval = protoConfig.weatherChannelReceiveInterval.toLong()

            while (isActive) {
                try {
                    nextWeatherRequestTime.set(System.currentTimeMillis() + interval)

                    if (!BuildConfig.DEBUG) {
                        // additional try-block 'cause if exception occurs here,
                        // coroutine won't be delayed
                        try {
                            weatherRepo.put(dataProvider.getWeather())
                        } catch (e: Exception) {
                            log error e
                        }
                    }

                    delay(interval)
                } catch (e: Exception) {
                    log error e
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