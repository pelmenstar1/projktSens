@file:Suppress("NOTHING_TO_INLINE")

package com.pelmenstar.projktSens.weather.app

import com.pelmenstar.projktSens.serverProtocol.ProtoConfig
import com.pelmenstar.projktSens.serverProtocol.repo.RepoClient
import com.pelmenstar.projktSens.serverProtocol.repo.RepoCommands
import com.pelmenstar.projktSens.shared.time.ShortDate
import com.pelmenstar.projktSens.shared.time.ShortDateInt
import com.pelmenstar.projktSens.shared.time.ShortDateRange
import com.pelmenstar.projktSens.weather.models.*

class NetworkDataSource(config: ProtoConfig) : WeatherDataSource {
    private val client = RepoClient(config)

    override suspend fun getDayReport(@ShortDateInt date: Int): DayReport? {
        require(ShortDate.isValid(date)) { "date" }

        return useClient { request(RepoCommands.GEN_DAY_REPORT, date) }
    }

    override suspend fun getDayRangeReport(range: ShortDateRange): DayRangeReport? {
        return useClient { request(RepoCommands.GEN_DAY_RANGE_REPORT, range) }
    }

    override suspend fun getAvailableDateRange(): ShortDateRange? {
        return useClient { request(RepoCommands.GET_AVAILABLE_DATE_RANGE) }
    }

    override suspend fun getLastWeather(): WeatherInfo? {
        return useClient { request(RepoCommands.GET_LAST_WEATHER) }
    }

    private inline fun <T> useClient(
        expr: RepoClient.() -> T
    ): T {
        return try {
            expr(client)
        } catch (e: Exception) {
            throw DataSourceException(e)
        }
    }
}