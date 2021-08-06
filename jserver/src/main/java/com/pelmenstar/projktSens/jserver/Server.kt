package com.pelmenstar.projktSens.jserver

import com.pelmenstar.projktSens.serverProtocol.Errors
import com.pelmenstar.projktSens.serverProtocol.Commands
import com.pelmenstar.projktSens.serverProtocol.Contract
import com.pelmenstar.projktSens.serverProtocol.Request
import com.pelmenstar.projktSens.serverProtocol.Response
import com.pelmenstar.projktSens.shared.acceptSuspend
import com.pelmenstar.projktSens.shared.bindSuspend
import com.pelmenstar.projktSens.shared.time.ShortDate
import com.pelmenstar.projktSens.shared.time.ShortDateRange
import com.pelmenstar.projktSens.weather.models.DayRangeReport
import com.pelmenstar.projktSens.weather.models.DayReport
import com.pelmenstar.projktSens.weather.models.WeatherRepository
import kotlinx.coroutines.*
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket

/**
 * Server that connects server weather repository and client.
 * After [Request] has been sent, server will respond with [Response].
 *
 * Command of request should be one of these:
 * - [Commands.GEN_DAY_REPORT]. Arguments should contain date of desired report.
 *   If date is out of available data, server will return empty response, otherwise, will return instance of [DayReport]
 *
 * - [Commands.GEN_DAY_RANGE_REPORT]. Arguments should contain range of dates of desired report.
 *   Arguments should be instance of [ShortDateRange].
 *   If range is out of available data, server will return empty response, otherwise, will return instance of [DayRangeReport].
 *
 * - [Commands.GET_AVAILABLE_DATE_RANGE]. No arguments is required.
 *  By name of command, you can assume that server will return date range of available data and you will be right, it does exactly this.
 *
 *  - [Commands.GET_LAST_WEATHER]. No arguments is required.
 *  Last added weather will be returned.
 */
class Server {
    private val contract: Contract
    private val repo: WeatherRepository

    private val scope = CoroutineScope(Dispatchers.IO)

    @Volatile
    private var serverSocket: ServerSocket? = null

    @Volatile
    private var job: Job? = null

    private val address: InetSocketAddress

    private val log: Logger

    init {
        val serverConfig = serverConfig
        val protoConfig = serverConfig.protoConfig

        log = Logger(javaClass.simpleName, serverConfig.loggerConfig)
        address = protoConfig.socketAddress

        contract = protoConfig.contract
        repo = serverConfig.sharedRepo
    }

    fun startOnNewThread() {
        job = scope.launch {
            startOnCurrentThread()
        }
    }

    fun startOnCurrentThreadBlocking() {
        runBlocking { startOnCurrentThread() }
    }

    suspend fun startOnCurrentThread() {
        try {
            ServerSocket().use { server ->
                server.bindSuspend(address, 5)
                log.info("server started")

                serverSocket = server

                while (true) {
                    val client = server.acceptSuspend()

                    scope.launch {
                        try {
                            client.use {
                                processClient(client)
                            }
                        } catch (e: Throwable) {
                            log.error(e)
                        }
                    }
                }
            }
        } catch (e: Throwable) {
            log.error(e)
        }
    }

    /**
     * Stops the server and cleans up all resources connected with it
     */
    fun stop() {
        try {
            job?.cancel()
            job = null

            serverSocket?.close()
            serverSocket = null
        } catch (e: Exception) {
            log.error(e)
        }
    }

    private suspend fun processClient(client: Socket) {
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

    private suspend fun processRequest(request: Request): Response {
        return try {
            val arg = request.argument

            when (request.command) {
                Commands.GET_AVAILABLE_DATE_RANGE -> {
                    val range = repo.getAvailableDateRange()

                    Response.okOrEmpty(range)
                }
                Commands.GEN_DAY_REPORT -> {
                    if(arg == null) {
                        return Response.error(Errors.INVALID_ARGUMENTS)
                    }

                    val date = arg as Int

                    if(!ShortDate.isValid(date)) {
                        return Response.error(Errors.INVALID_ARGUMENTS)
                    }

                    log.info {
                        append("date: ")
                        ShortDate.append(date, this)
                    }

                    val report = repo.getDayReport(date)

                    Response.okOrEmpty(report)
                }
                Commands.GEN_DAY_RANGE_REPORT -> {
                    if(arg == null) {
                        return Response.error(Errors.INVALID_ARGUMENTS)
                    }

                    val range = arg as ShortDateRange

                    log.info {
                        append("range=")
                        append(range)
                    }

                    val report = repo.getDayRangeReport(range)

                    Response.okOrEmpty(report)
                }
                Commands.GET_LAST_WEATHER -> {
                    val weather = repo.getLastWeather()

                    Response.okOrEmpty(weather)
                }
                Commands.GET_NEXT_WEATHER_TIME -> {
                    val time = WeatherMonitor.getNextWeatherRequestTime()

                    Response.ok(time)
                }
                else -> Response.error(Errors.INVALID_COMMAND)
            }
        } catch (e: Exception) {
            log.error(e)

            Response.error(e)
        }
    }
}