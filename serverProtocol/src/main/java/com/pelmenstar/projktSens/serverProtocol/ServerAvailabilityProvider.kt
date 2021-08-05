package com.pelmenstar.projktSens.serverProtocol

import com.pelmenstar.projktSens.serverProtocol.ProtoConfig
import com.pelmenstar.projktSens.shared.connectSuspend
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.Socket

class ServerAvailabilityProvider(config: ProtoConfig) {
    private val address = config.socketAddress

    suspend fun isAvailable(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val socket = Socket()
                socket.connectSuspend(address, 5000)

                true
            } catch (e: Exception) {
                false
            }
        }
    }
}