package com.pelmenstar.projktSens.weather.models

/**
 * Implements [WeatherDataSource].
 * The only difference is that [WeatherRepository] is mutable (is able to add and remove data)
 */
interface WeatherRepository : WeatherDataSource {
    suspend fun putMany(values: Array<WeatherInfo>)
    suspend fun put(weather: WeatherInfo)
    suspend fun clear()
}