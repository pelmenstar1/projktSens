package com.pelmenstar.projktSens.weather.models

/**
 * Provider of [WeatherInfo]
 */
interface WeatherInfoProvider {
    /**
     * Returns some [WeatherInfo] from some source
     */
    suspend fun getWeather(): WeatherInfo
}