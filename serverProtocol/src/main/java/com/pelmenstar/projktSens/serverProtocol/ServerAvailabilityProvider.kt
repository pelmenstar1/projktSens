package com.pelmenstar.projktSens.serverProtocol

import android.os.Build
import androidx.annotation.RequiresApi
import com.pelmenstar.projktSens.shared.connectSuspend
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import java.net.Socket
import java.nio.channels.AsynchronousChannelGroup
import java.nio.channels.AsynchronousSocketChannel

class ServerAvailabilityProvider(
    config: ProtoConfig,
    private var forceBlocking: Boolean = false
) {
    private val address = config.socketAddress

    private var isAvailableAsyncHandler: (suspend CoroutineScope.() -> Boolean)? = null

    init {
        if(Build.VERSION.SDK_INT >= 26) {
            isAvailableAsyncHandler = {
                AsynchronousSocketChannel.open().use { channel ->
                    channel.connectSuspend(address)
                    true
                }
            }
        }
    }

    suspend fun isAvailable(): Boolean {
        return if(Build.VERSION.SDK_INT >= 26 && !forceBlocking) {
            isAvailableAsync()
        } else {
            isAvailableBlocking()
        }
    }

    private fun isAvailableBlocking(): Boolean {
        return try {
            val socket = Socket()
            socket.connect(address, 5000)

            true
        } catch (e: Exception) {
            false
        }
    }

    @RequiresApi(26)
    private suspend fun isAvailableAsync(): Boolean {
        return try {
            withTimeout(5000L, isAvailableAsyncHandler!!)
        } catch (e: Exception) {
            false
        }
    }
}