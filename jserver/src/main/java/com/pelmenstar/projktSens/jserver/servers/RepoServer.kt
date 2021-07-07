package com.pelmenstar.projktSens.jserver.servers

import com.pelmenstar.projktSens.jserver.serverConfig
import com.pelmenstar.projktSens.serverProtocol.Errors
import com.pelmenstar.projktSens.serverProtocol.repo.RepoCommands
import com.pelmenstar.projktSens.serverProtocol.repo.RepoContract
import com.pelmenstar.projktSens.serverProtocol.repo.RepoRequest
import com.pelmenstar.projktSens.serverProtocol.repo.RepoResponse
import com.pelmenstar.projktSens.shared.time.ShortDate
import com.pelmenstar.projktSens.shared.time.ShortDateRange
import com.pelmenstar.projktSens.weather.models.DayRangeReport
import com.pelmenstar.projktSens.weather.models.DayReport
import com.pelmenstar.projktSens.weather.models.WeatherRepository
import java.net.Socket

/**
 * Server that connects server weather repository and client.
 * After [RepoRequest] has been sent, server will respond with [RepoResponse].
 *
 * Command of request should be one of these:
 * - [RepoCommands.GEN_DAY_REPORT]. Arguments should contain date of desired report.
 *   If date is out of available data, server will return empty response, otherwise, will return instance of [DayReport]
 *
 * - [RepoCommands.GEN_DAY_RANGE_REPORT]. Arguments should contain range of dates of desired report.
 *   Arguments should be instance of [ShortDateRange].
 *   If range is out of available data, server will return empty response, otherwise, will return instance of [DayRangeReport].
 *
 * - [RepoCommands.GET_AVAILABLE_DATE_RANGE]. No arguments is required.
 *  By name of command, you can assume that server will return date range of available data and you will be right, it does exactly this.
 *
 *  - [RepoCommands.GET_LAST_WEATHER]. No arguments is required.
 *  Last added weather will be returned.
 */
class RepoServer : ServerBase({ repoServerPort }) {
    private val contract: RepoContract
    private val repo: WeatherRepository

    init {
        val serverConfig = serverConfig
        val protoConfig = serverConfig.protoConfig

        contract = protoConfig.repoContract
        repo = serverConfig.sharedRepo
    }

    override suspend fun processClient(client: Socket) {
        try {
            val input = client.getInputStream()
            val out = client.getOutputStream()

            val request = contract.readRequest(input)
            log.info {
                append("request=")
                request.append(this)
            }

            val response = processRequest(request)

            log.info {
                append("response=")
                response.append(this)
            }

            contract.writeResponse(response, out)
        } catch (e: Exception) {
            log.error(e)
        }
    }

    private suspend fun processRequest(request: RepoRequest): RepoResponse {
        return try {
            val arg = request.argument

            when (request.command) {
                RepoCommands.GET_AVAILABLE_DATE_RANGE -> {
                    val range = repo.getAvailableDateRange()

                    RepoResponse.okOrEmpty(range)
                }
                RepoCommands.GEN_DAY_REPORT -> {
                    if(arg == null) {
                        return RepoResponse.error(Errors.INVALID_ARGUMENTS)
                    }

                    val date = arg as Int

                    if(!ShortDate.isValid(date)) {
                        return RepoResponse.error(Errors.INVALID_ARGUMENTS)
                    }

                    log.info {
                        append("date: ")
                        ShortDate.append(date, this)
                    }

                    val report = repo.getDayReport(date)

                    RepoResponse.okOrEmpty(report)
                }
                RepoCommands.GEN_DAY_RANGE_REPORT -> {
                    if(arg == null) {
                        return RepoResponse.error(Errors.INVALID_ARGUMENTS)
                    }

                    val range = arg as ShortDateRange

                    log.info {
                        append("range=")
                        append(range)
                    }

                    val report = repo.getDayRangeReport(range)

                    RepoResponse.okOrEmpty(report)
                }
                RepoCommands.GET_LAST_WEATHER -> {
                    val weather = repo.getLastWeather()

                    RepoResponse.okOrEmpty(weather)
                }
                else -> RepoResponse.error(Errors.INVALID_COMMAND)
            }
        } catch (e: Exception) {
            log.error(e)

            RepoResponse.error(e)
        }
    }
}