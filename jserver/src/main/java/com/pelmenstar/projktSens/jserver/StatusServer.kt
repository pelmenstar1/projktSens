package com.pelmenstar.projktSens.jserver

import com.pelmenstar.projktSens.shared.readNSuspend
import com.pelmenstar.projktSens.shared.writeSuspend
import java.io.IOException
import java.net.Socket

/**
 * Server that gives a capability to get status of servers
 *
 * To retrieve status of server, you have to send `1`.
 * If all servers are available, it will return `1`, otherwise it will return nothing or `0` byte
 */
class StatusServer: ServerBase({ serverStatusPort }) {
    override suspend fun processClient(client: Socket) {
        val input = client.getInputStream()

        val buffer = input.readNSuspend(1)
        if(buffer[0].toInt() != 1) {
            throw IOException()
        }

        val output = client.getOutputStream()
        output.writeSuspend(AVAILABLE_MSG)
    }

    companion object {
        private val AVAILABLE_MSG = byteArrayOf(1)
    }
}