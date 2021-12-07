package com.pelmenstar.projktSens.serverProtocol

import android.os.Build
import androidx.annotation.RequiresApi
import com.pelmenstar.projktSens.shared.connectSuspend
import com.pelmenstar.projktSens.shared.io.SmartInputStream
import com.pelmenstar.projktSens.shared.io.SmartOutputStream
import java.net.Socket
import java.nio.channels.AsynchronousSocketChannel

/**
 * Represents client for repo-server
 */
class Client(config: ProtoConfig, private val forceBlocking: Boolean = false) {
    private val contract = config.contract
    private val address = config.socketAddress

    suspend inline fun <T : Any> request(command: Int, responseValueClass: Class<T>): T? {
        return request(Request(command), responseValueClass)
    }

    suspend inline fun <T : Any> request(command: Int, arg: Request.Argument, responseValueClass: Class<T>): T? {
        return request(Request(command, arg), responseValueClass)
    }

    suspend inline fun <reified T : Any> request(command: Int): T? {
        return request(command, T::class.java)
    }

    suspend inline fun <reified T : Any> request(command: Int, arg: Request.Argument): T? {
        return request(command, arg, T::class.java)
    }

    suspend inline fun <reified T : Any> request(request: Request): T? {
        return request(request, T::class.java)
    }

    suspend fun <T : Any> request(request: Request, responseValueClass: Class<T>): T? {
        return handleRawResponseCast(requestRawResponse(request, responseValueClass))
    }

    @Suppress("UNCHECKED_CAST")
    private fun<T : Any> handleRawResponseCast(response: Response): T? {
        return handleRawResponse(response) as T?
    }

    private fun handleRawResponse(response: Response): Any? {
        return when(response) {
            Response.Empty -> null
            is Response.Error -> throw ServerException(response.error)
            is Response.Ok<*> -> response.value
        }
    }

    suspend inline fun <reified T : Any> requestRawResponse(command: Int): Response {
        return requestRawResponse(command, T::class.java)
    }

    suspend inline fun <reified T : Any> requestRawResponse(command: Int, arg: Request.Argument): Response {
        return requestRawResponse(command, arg, T::class.java)
    }

    suspend inline fun requestRawResponse(command: Int, responseValueClass: Class<*>): Response {
        return requestRawResponse(Request(command), responseValueClass)
    }

    suspend inline fun requestRawResponse(
        command: Int,
        arg: Request.Argument,
        responseValueClass: Class<*>
    ): Response {
        return requestRawResponse(Request(command, arg), responseValueClass)
    }

    suspend fun requestRawResponse(request: Request, responseValueClass: Class<*>): Response {
        return requestMultipleRaw(arrayOf(request), arrayOf(responseValueClass))[0]
    }

    suspend fun requestMultiple(
        requests: Array<Request>,
        valueClasses: Array<Class<*>>
    ): Array<Any?> {
        val raw = requestMultipleRaw(requests, valueClasses)

        return Array(raw.size) { i -> handleRawResponse(raw[i]) }
    }

    suspend fun requestMultipleRaw(
        requests: Array<Request>,
        valueClasses: Array<Class<*>>
    ): Array<Response> {
        return if(Build.VERSION.SDK_INT >= 26 && !forceBlocking) {
            requestMultipleRawAsync(requests, valueClasses)
        } else {
            requestMultipleBlocking(requests, valueClasses)
        }
    }

    @RequiresApi(26)
    private suspend fun requestMultipleRawAsync(
        requests: Array<Request>,
        valueClasses: Array<Class<*>>
    ): Array<Response> {
        return AsynchronousSocketChannel.open().use { channel ->
            channel.connectSuspend(address, 5000)

            val input = SmartInputStream.toSmart(channel)
            val output = SmartOutputStream.toSmart(channel)

            contract.writeRequests(requests, output)

            contract.readResponses(input, valueClasses)
        }
    }

    private suspend fun requestMultipleBlocking(
        requests: Array<Request>,
        valueClasses: Array<Class<*>>
    ): Array<Response> {
        return Socket().use { socket ->
            socket.connectSuspend(address, 5000)

            val input = SmartInputStream.toSmart(socket)
            val output = SmartOutputStream.toSmart(socket)

            contract.writeRequests(requests, output)

            contract.readResponses(input, valueClasses)
        }
    }
}