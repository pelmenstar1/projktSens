package com.pelmenstar.projktSens.weather.models

/**
 * Provides information about weather channel
 */
interface WeatherChannelInfoProvider {
    /**
     * Returns interval in milliseconds of taking weather
     */
    val receiveInterval: Long

    /**
     * Returns time to wait for the next weather
     */
    suspend fun getWaitTimeForNextWeather(): Long
}