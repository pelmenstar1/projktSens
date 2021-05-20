package com.pelmenstar.projktSens.jserver

import com.pelmenstar.projktSens.serverProtocol.ProtoConfig
import com.pelmenstar.projktSens.shared.acceptSuspend
import com.pelmenstar.projktSens.shared.bindSuspend
import kotlinx.coroutines.*
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket

/**
 * Represents a common implementation of TCP server.
 */
abstract class ServerBase protected constructor(
    port: ProtoConfig.() -> Int,
) {
    @Volatile
    private var serverSocket: ServerSocket? = null

    @Volatile
    private var job: Job? = null

    private val address: InetSocketAddress

    protected val log: Logger = Logger(javaClass.simpleName, serverConfig.loggerConfig)

    init {
        val protoConfig = serverConfig.protoConfig

        address = InetSocketAddress(protoConfig.serverIp, port(protoConfig))
    }

    /**
     * Starts server in another thread.
     *
     * It will not start again if server is already started,
     * if so, [ServerBase.stop] must be called before
     */
    fun start() {
        if (job != null) {
            log.error("server is not stopped")
            return
        }

        job = GlobalScope.launch(Dispatchers.IO) {
            try {
                ServerSocket().use { server ->
                    server.bindSuspend(address, 5)
                    log.info("server started")

                    serverSocket = server

                    while (isActive) {
                        val client = server.acceptSuspend()

                        GlobalScope.launch(Dispatchers.IO) {
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


    /**
     * This method processes messages from the incoming client.
     * Implementation don't need to worry about exception. Any thrown exception will be handled and logged
     * This method always executes in [Dispatchers.IO] context.
     *
     * @param client incoming client
     */
    protected abstract suspend fun processClient(client: Socket)
}