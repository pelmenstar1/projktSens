package com.pelmenstar.projktSens.jserver

import com.pelmenstar.projktSens.serverProtocol.ProtoConfig
import com.pelmenstar.projktSens.serverProtocol.AvailabilityProvider
import com.pelmenstar.projktSens.serverProtocol.ServerStatus
import com.pelmenstar.projktSens.serverProtocol.socketAddress
import java.net.Socket
import com.pelmenstar.projktSens.shared.connectSuspend
import com.pelmenstar.projktSens.shared.readNSuspend
import com.pelmenstar.projktSens.shared.writeSuspend
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ServerAvailabilityProvider(config: ProtoConfig): AvailabilityProvider {
    private val address = config.socketAddress { serverStatusPort }
    private val getStatusMsg = byteArrayOf(1)

    override suspend fun getStatus(): ServerStatus {
        return withContext(Dispatchers.IO) {
            try {
                Socket().use { socket ->
                    socket.connectSuspend(address)
                    socket.soTimeout = 5000

                    val input = socket.getInputStream()
                    val output = socket.getOutputStream()

                    output.writeSuspend(getStatusMsg)

                    val status = input.readNSuspend(1)[0].toInt()

                    if(status == 1) {
                        ServerStatus.AVAILABLE
                    } else {
                        ServerStatus.NOT_AVAILABLE
                    }
                }
            } catch (e: Exception) {
                ServerStatus.NOT_AVAILABLE
            }
        }
    }
}