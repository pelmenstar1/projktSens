package com.pelmenstar.projktSens.jserver

import android.util.Log
import com.pelmenstar.projktSens.shared.acceptSuspend
import com.pelmenstar.projktSens.shared.bindSuspend
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket

/**
 * Represents a common implementation of TCP server.
 *
 * @param address address of server
 * @param tag Android Log tag
 */
abstract class ServerBase protected constructor(private val address: InetSocketAddress, private val tag: String) {
    @Volatile
    private var serverSocket: ServerSocket? = null

    @Volatile
    private var job: Job? = null


    /**
     * Starts server in another thread.
     *
     * It will not start again if server is already started,
     * if so, [ServerBase.stop] must be called before
     */
    fun start() {
        if (job != null) {
            Log.i(tag, "server is not stopped")
            return
        }

        job = GlobalScope.launch(Dispatchers.IO) {
            try {
                ServerSocket().use { server ->
                    server.bindSuspend(address, 5)
                    Log.i(tag, "server started")

                    serverSocket = server

                    while (isActive) {
                        val client = server.acceptSuspend()

                        GlobalScope.launch(Dispatchers.IO) {
                            try {
                                client.use {
                                    processClient(client)
                                }
                            } catch (e: Throwable) {
                                Log.e(tag, "while processing client", e)
                            }
                        }
                    }
                }
            } catch (e: Throwable) {
                Log.e(tag, "while binding server", e)
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
            Log.e(tag, "when stopping server", e)
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