package com.pelmenstar.projktSens.serverProtocol

import com.pelmenstar.projktSens.shared.connectSuspend
import java.net.Socket

class ServerAvailabilityProvider(config: ProtoConfig) {
    private val address = config.socketAddress

    suspend fun isAvailable(): Boolean {
        return try {
            val socket = Socket()
            socket.connectSuspend(address, 5000)

            true
        } catch (e: Exception) {
            false
        }
    }
}