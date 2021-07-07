package com.pelmenstar.projktSens.jserver

import com.pelmenstar.projktSens.weather.models.WeatherInfoProvider
import com.pelmenstar.projktSens.weather.models.WeatherRepository
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicLong

/**
 * Requests weather in [WeatherInfoProvider] and puts it to default [WeatherRepository]
 */
object WeatherMonitor {
    private val scope = CoroutineScope(Dispatchers.Default)

    private const val TAG = "WeatherMonitor"

    @JvmStatic
    private var job: Job? = null
    private val nextWeatherRequestTime = AtomicLong()

    private val log: Logger

    init {
        val config = serverConfig
        log = Logger("WeatherMonitor", config.loggerConfig)
    }

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
            log.error("monitor is already running")
            return
        }

        job = scope.launch {
            val config = serverConfig
            val protoConfig = config.protoConfig
            val dataProvider = config.weatherProvider
            val repo = config.sharedRepo
            val interval = protoConfig.weatherChannelReceiveInterval.toLong()

            while (isActive) {
                try {
                    nextWeatherRequestTime.set(System.currentTimeMillis() + interval)

                    if (!BuildConfig.DEBUG) {
                        // additional try-block 'cause if exception occurs here,
                        // coroutine won't be delayed
                        try {
                            repo.put(dataProvider.getWeather())
                        } catch (e: Exception) {
                            log.error(e)
                        }
                    }

                    delay(interval)
                } catch (e: Exception) {
                    log.error(e)
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