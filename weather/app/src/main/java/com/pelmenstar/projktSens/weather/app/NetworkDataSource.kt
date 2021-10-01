@file:Suppress("NOTHING_TO_INLINE")

package com.pelmenstar.projktSens.weather.app

import com.pelmenstar.projktSens.serverProtocol.Client
import com.pelmenstar.projktSens.serverProtocol.Commands
import com.pelmenstar.projktSens.serverProtocol.ProtoConfig
import com.pelmenstar.projktSens.serverProtocol.Request
import com.pelmenstar.projktSens.shared.time.ShortDate
import com.pelmenstar.projktSens.shared.time.ShortDateInt
import com.pelmenstar.projktSens.shared.time.ShortDateRange
import com.pelmenstar.projktSens.weather.models.*

class NetworkDataSource(config: ProtoConfig) : WeatherDataSource {
    private val client = Client(config)

    override suspend fun getDayReport(@ShortDateInt date: Int): DayReport? {
        require(ShortDate.isValid(date)) { "date" }

        return requestRethrow(
            Commands.GET_DAY_REPORT, Request.Argument.Integer(date), DayReport::class.java
        )
    }

    override suspend fun getDayRangeReport(
        @ShortDateInt start: Int, @ShortDateInt end: Int
    ): DayRangeReport? {
        return requestRethrow(
            Commands.GET_DAY_RANGE_REPORT,
            Request.Argument.DateRange(start, end),
            DayRangeReport::class.java
        )
    }

    override suspend fun getAvailableDateRange(): ShortDateRange? {
        return requestRethrow(Commands.GET_AVAILABLE_DATE_RANGE, null, ShortDateRange::class.java)
    }

    override suspend fun getLastWeather(): WeatherInfo? {
        return requestRethrow(Commands.GET_LAST_WEATHER, null, WeatherInfo::class.java)
    }

    private suspend fun <T : Any> requestRethrow(
        command: Int, arg: Request.Argument?,
        responseClass: Class<T>
    ): T? {
        return try {
            client.request(Request(command, arg), responseClass)
        } catch (e: Exception) {
            throw DataSourceException(e)
        }
    }
}