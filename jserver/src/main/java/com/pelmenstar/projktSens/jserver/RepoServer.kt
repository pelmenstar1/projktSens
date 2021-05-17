package com.pelmenstar.projktSens.jserver

import android.util.Log
import com.pelmenstar.projktSens.serverProtocol.*
import com.pelmenstar.projktSens.serverProtocol.repo.RepoCommands
import com.pelmenstar.projktSens.serverProtocol.repo.RepoRequest
import com.pelmenstar.projktSens.serverProtocol.repo.RepoResponse
import com.pelmenstar.projktSens.serverProtocol.repo.RepoContract
import com.pelmenstar.projktSens.shared.*
import com.pelmenstar.projktSens.shared.time.ShortDate
import com.pelmenstar.projktSens.shared.time.ShortDateRange
import com.pelmenstar.projktSens.weather.models.DayReport
import com.pelmenstar.projktSens.weather.models.DayRangeReport
import java.net.Socket
import java.util.concurrent.atomic.AtomicLong
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

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
class RepoServer(config: ProtoConfig) : ServerBase(
    config.socketAddress { repoServerPort },
    TAG
) {
    private val contract = config.repoContract
    private val lastClientToken = AtomicLong()

    override suspend fun processClient(client: Socket) {
        try {
            val input = client.getInputStream()
            val out = client.getOutputStream()

            val token = lastClientToken.getAndIncrement()
            val request = contract.readRequest(input)
            log(token) {
                append("request=")
                request.append(this)
            }

            val response = processRequest(token, request)

            log(token) {
                append("response=")
                response.append(this)
            }

            contract.writeResponse(response, out)
        } catch (e: Exception) {
            Log.e(TAG, "while processing client", e)
        }
    }

    private suspend fun processRequest(token: Long, request: RepoRequest): RepoResponse {
        return try {
            val repo = serverConfig.sharedRepo
            val args = request.args

            when (request.command) {
                RepoCommands.GET_AVAILABLE_DATE_RANGE -> {
                    val range = repo.getAvailableDateRange()

                    RepoResponse.okOrEmpty(range)
                }
                RepoCommands.GEN_DAY_REPORT -> {
                    if (args == null || args.size != 4) {
                        return RepoResponse.error(Errors.INVALID_ARGUMENTS)
                    }

                    val date = args.getInt(0)
                    if(!ShortDate.isValid(date)) {
                        return RepoResponse.error(Errors.INVALID_ARGUMENTS)
                    }

                    log(token) {
                        append("date: ")
                        ShortDate.append(date, this)
                    }

                    val report = repo.getDayReport(date)

                    RepoResponse.okOrEmpty(report)
                }
                RepoCommands.GEN_DAY_RANGE_REPORT -> {
                    if (args == null || args.size != 8) {
                        return RepoResponse.error(Errors.INVALID_ARGUMENTS)
                    }

                    val startDate = args.getInt(0)
                    val endDate = args.getInt(4)

                    if(!ShortDate.isValid(startDate) || !ShortDate.isValid(endDate)) {
                        return RepoResponse.error(Errors.INVALID_ARGUMENTS)
                    }

                    log(token) {
                        append("startDate: ")
                        ShortDate.append(startDate, this)
                        append("; endDate: ")
                        ShortDate.append(endDate, this)
                    }

                    val report = repo.getDayRangeReport(ShortDateRange(startDate, endDate))

                    RepoResponse.okOrEmpty(report)
                }
                RepoCommands.GET_LAST_WEATHER -> {
                    val weather = repo.getLastWeather()

                    RepoResponse.okOrEmpty(weather)
                }
                else -> RepoResponse.error(Errors.INVALID_COMMAND)
            }
        } catch (e: Exception) {
            Log.e(TAG, null, e)

            RepoResponse.error(e)
        }
    }

    companion object {
        private const val TAG = "RepoServer"
        private const val LOG = true

        private inline fun log(token: Long, lazyMsg: StringBuilder.() -> Unit) {
            contract {
                callsInPlace(lazyMsg, InvocationKind.AT_MOST_ONCE)
            }

            if(LOG) {
                val msg = buildString {
                    append("token ")
                    append(token)
                    append(' ')
                    append('[')
                    append(' ')
                    lazyMsg()
                    append(' ')
                    append(']')
                }
                Log.i(TAG, msg)
            }
        }
    }
}