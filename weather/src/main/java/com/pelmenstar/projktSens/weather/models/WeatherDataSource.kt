package com.pelmenstar.projktSens.weather.models

import com.pelmenstar.projktSens.shared.time.ShortDateInt
import com.pelmenstar.projktSens.shared.time.ShortDateRange

/**
 * Responsible for getting data from some source like network, internal db or others.
 * Based on Kotlin Coroutines
 */
interface WeatherDataSource {
    /**
     * Gets [DayReport] taking data in specified [date], can be null if source doesn't contain data of given [date]
     */
    suspend fun getDayReport(@ShortDateInt date: Int): DayReport?

    /**
     * Gets [DayRangeReport] taking data at specified date range, can be null if source doesn't contain data at given range
     */
    suspend fun getDayRangeReport(@ShortDateInt start: Int, @ShortDateInt end: Int): DayRangeReport?

    /**
     * Gets available range that contains the source, can be null if source is empty
     */
    suspend fun getAvailableDateRange(): ShortDateRange?

    /**
     * Gets last added weather, can be null if source is empty
     */
    suspend fun getLastWeather(): WeatherInfo?
}