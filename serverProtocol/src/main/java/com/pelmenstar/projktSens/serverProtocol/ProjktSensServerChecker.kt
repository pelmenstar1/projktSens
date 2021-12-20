package com.pelmenstar.projktSens.serverProtocol

import android.os.Build
import androidx.annotation.RequiresApi
import com.pelmenstar.projktSens.shared.connectSuspend
import com.pelmenstar.projktSens.shared.io.Input
import com.pelmenstar.projktSens.shared.io.Output
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.channels.AsynchronousSocketChannel

object ProjktSensServerChecker {
    enum class Status {
        UNAVAILABLE,
        NOT_PROJKT_SENS_SERVER,
        IS_PROJKT_SENS_SERVER
    }

    private val REQUESTS = arrayOf(Request(Commands.GET_MAGIC_NUMBER))
    private val RESPONSE_CLASSES: Array<Class<*>> = arrayOf(Long::class.java)

    suspend fun isProjktSensServer(contract: Contract, ip: InetSocketAddress): Status {
        return try {
            return if (Build.VERSION.SDK_INT >= 26) {
                isProjktSensServerAsync(contract, ip)
            } else {
                isProjktSensServerBlocking(contract, ip)
            }
        } catch(e: Exception) {
            Status.NOT_PROJKT_SENS_SERVER
        }
    }

    @RequiresApi(26)
    private suspend fun isProjktSensServerAsync(contract: Contract, ip: InetSocketAddress): Status {
        val channel = AsynchronousSocketChannel.open()
        try {
            channel.connectSuspend(ip, 5000)
        } catch (e: Exception) {
            return Status.UNAVAILABLE
        }

        val status = handle(contract, Input.of(channel), Output.of(channel))
        channel.close()

        return status
    }

    private suspend fun isProjktSensServerBlocking(contract: Contract, ip: InetSocketAddress): Status {
        val socket = Socket()
        try {
            socket.connect(ip, 5000)
        } catch (e: Exception) {
            return Status.UNAVAILABLE
        }

        val status = handle(contract, Input.of(socket), Output.of(socket))
        socket.close()

        return status
    }

    private suspend fun handle(contract: Contract, input: Input, output: Output): Status {
        try {
            contract.writeRequests(REQUESTS, output)
            val response = contract.readResponses(input, RESPONSE_CLASSES)[0]

            return when (response) {
                Response.Empty, is Response.Error -> Status.NOT_PROJKT_SENS_SERVER
                is Response.Ok<*> -> {
                    val magicNumber = response.value as Long

                    if (magicNumber == ProjktSensServer.MAGIC_NUMBER)
                        Status.IS_PROJKT_SENS_SERVER
                    else
                        Status.NOT_PROJKT_SENS_SERVER
                }
            }
        } catch (e: Exception) {
            return Status.NOT_PROJKT_SENS_SERVER
        }
    }
}