package com.pelmenstar.projktSens.jserver

import android.os.Build
import androidx.annotation.RequiresApi
import com.pelmenstar.projktSens.jserver.logging.Logger
import com.pelmenstar.projktSens.jserver.logging.LoggerConfig
import com.pelmenstar.projktSens.serverProtocol.*
import com.pelmenstar.projktSens.shared.AppendableToStringBuilder
import com.pelmenstar.projktSens.shared.acceptSuspend
import com.pelmenstar.projktSens.shared.bindSuspend
import com.pelmenstar.projktSens.shared.time.ShortDate
import com.pelmenstar.projktSens.shared.time.ShortDateRange
import com.pelmenstar.projktSens.weather.models.DayRangeReport
import com.pelmenstar.projktSens.weather.models.DayReport
import com.pelmenstar.projktSens.weather.models.WeatherRepository
import kotlinx.coroutines.*
import java.io.Closeable
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.nio.channels.AsynchronousServerSocketChannel
import java.nio.channels.AsynchronousSocketChannel

/**
 * Server that connects server weather repository and client.
 * After [Request] has been sent, server will respond with [Response].
 *
 * Command of request should be one of these:
 * - [Commands.GET_DAY_REPORT]. Arguments should contain date of desired report.
 *   If date is out of available data, server will return empty response, otherwise, will return instance of [DayReport]
 *
 * - [Commands.GET_DAY_RANGE_REPORT]. Arguments should contain range of dates of desired report.
 *   Arguments should be instance of [ShortDateRange].
 *   If range is out of available data, server will return empty response, otherwise, will return instance of [DayRangeReport].
 *
 * - [Commands.GET_AVAILABLE_DATE_RANGE]. No arguments is required.
 *  By name of command, you can assume that server will return date range of available data and you will be right, it does exactly this.
 *
 *  - [Commands.GET_LAST_WEATHER]. No arguments is required.
 *  Last added weather will be returned.
 */
class Server(
    protoConfig: ProtoConfig,
    loggerConfig: LoggerConfig,
    private val weatherRepo: WeatherRepository
) {
    @Volatile
    private var serverSocket: ServerSocket? = null

    @Volatile
    private var serverChannel: AsynchronousServerSocketChannel? = null

    @Volatile
    private var job: Job? = null
    private val lock = Any()

    @Volatile
    private var isStarted: Boolean = false
    private val isStartedLock = Any()

    var weatherMonitor: WeatherMonitor? = null

    private val address: InetSocketAddress = protoConfig.socketAddress
    private val contract: Contract = protoConfig.contract

    private val log: Logger = Logger(javaClass.simpleName, loggerConfig)

    fun startOnNewThread() {
        synchronized(lock) {
            job = scope.launch {
                startOnCurrentThread()
            }
        }
    }

    private suspend fun startOnCurrentThread() {
        synchronized(isStartedLock) {
            if(isStarted) {
               throw RuntimeException("Server is already started")
            }

            isStarted = true
        }

        if(Build.VERSION.SDK_INT >= 26) {
            startOnCurrentThreadAsync()
        } else {
            startOnCurrentThreadSync()
        }
    }

    private suspend fun startOnCurrentThreadSync() {
        try {
            ServerSocket().use { server ->
                server.bindSuspend(address, BACKLOG)
                logServerStarted()

                synchronized(lock) {
                    serverSocket = server
                }

                while (true) {
                    val client = server.acceptSuspend()

                    launchAndProcessClient(client, ::processClientSync)
                }
            }
        } catch (e: Throwable) {
            log error e

            stop()
        }
    }

    @RequiresApi(26)
    private suspend fun startOnCurrentThreadAsync() {
        try {
            AsynchronousServerSocketChannel.open().use { server ->
                server.bind(address, BACKLOG)
                logServerStarted()

                synchronized(lock) {
                    serverChannel = server
                }

                while(true) {
                    val client = server.acceptSuspend()

                    launchAndProcessClient(client, ::processClientAsync)
                }
            }
        } catch (e: Throwable) {
            log error e

            stop()
        }
    }

    /**
     * Stops the server and cleans up all resources connected with it
     */
    fun stop() {
        try {
            synchronized(lock) {
                job?.cancel()
                job = null

                serverSocket?.close()
                serverSocket = null

                serverChannel?.close()
                serverChannel = null

                synchronized(isStartedLock) {
                    isStarted = false
                }
            }
        } catch (e: Exception) {
            log error e
        }
    }

    private inline fun <T : Closeable> launchAndProcessClient(
        client: T,
        crossinline block: suspend (T) -> Unit
    ) {
        scope.launch {
            try {
                block(client)
            } catch (e: Exception) {
                log error e
            }
        }
    }

    @RequiresApi(26)
    private suspend fun processClientAsync(client: AsynchronousSocketChannel) {
        try {
            val request = contract.readRequest(client)
            logAppendable("request", request)

            val response = processRequest(request)
            logAppendable("response", response)

            contract.writeResponse(response, client)
        } catch (e: Exception) {
            log error e
        }
    }

    private suspend fun processClientSync(client: Socket) {
        try {
            val input = client.getInputStream()
            val out = client.getOutputStream()

            val request = contract.readRequest(input)
            logAppendable("request", request)

            val response = processRequest(request)
            logAppendable("response", response)

            contract.writeResponse(response, out)
        } catch (e: Exception) {
            log error e
        }
    }

    private suspend fun processRequest(request: Request): Response {
        return try {
            val arg = request.argument

            when (request.command) {
                Commands.GET_AVAILABLE_DATE_RANGE -> {
                    val range = weatherRepo.getAvailableDateRange()

                    Response.okOrEmpty(range)
                }
                Commands.GET_DAY_REPORT -> {
                    if (arg == null) {
                        return Response.error(Errors.INVALID_ARGUMENTS)
                    }

                    val date: Int

                    if(arg is Request.Argument.Integer) {
                        date = arg.value
                    } else {
                        logInvalidTypeOfArgument(arg.type)

                        return Response.error(Errors.INVALID_ARGUMENTS)
                    }

                    if (!ShortDate.isValid(date)) {
                        return Response.error(Errors.INVALID_ARGUMENTS)
                    }

                    log info {
                        append("date=")
                        ShortDate.append(date, this)
                    }

                    val report = weatherRepo.getDayReport(date)

                    Response.okOrEmpty(report)
                }
                Commands.GET_DAY_RANGE_REPORT -> {
                    if (arg == null) {
                        return Response.error(Errors.INVALID_ARGUMENTS)
                    }

                    val start: Int
                    val end: Int

                    if(arg is Request.Argument.DateRange) {
                        start = arg.start
                        end = arg.endInclusive
                    } else {
                        logInvalidTypeOfArgument(arg.type)

                        return Response.error(Errors.INVALID_ARGUMENTS)
                    }

                    log info {
                        append("range={start=")
                        ShortDate.append(start, this)
                        append(", end=")
                        ShortDate.append(end, this)
                        append("}}")
                    }

                    val report = weatherRepo.getDayRangeReport(start, end)

                    Response.okOrEmpty(report)
                }
                Commands.GET_LAST_WEATHER -> {
                    val weather = weatherRepo.getLastWeather()

                    Response.okOrEmpty(weather)
                }
                Commands.GET_NEXT_WEATHER_TIME -> {
                    val time = weatherMonitor?.getNextWeatherRequestTime() ?: 0

                    Response.ok(time)
                }
                else -> Response.error(Errors.INVALID_COMMAND)
            }
        } catch (e: Exception) {
            log error e

            Response.error(e)
        }
    }

    private fun logAppendable(prefix: String, obj: AppendableToStringBuilder) {
        log info {
            append(prefix)
            append('=')
            obj.append(this)
        }
    }

    private fun logInvalidTypeOfArgument(type: Int) {
        log error {
            append("Invalid type of argument (")
            append(Request.Argument.typeToString(type))
            append(')')
        }
    }

    private fun logServerStarted() {
        log info "Server has been started successfully"
    }

    companion object {
        private val scope = CoroutineScope(Dispatchers.IO)
        private const val BACKLOG = 5
    }
}