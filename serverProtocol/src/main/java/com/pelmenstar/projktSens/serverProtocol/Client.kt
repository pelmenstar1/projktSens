package com.pelmenstar.projktSens.serverProtocol

import android.os.Build
import androidx.annotation.RequiresApi
import com.pelmenstar.projktSens.shared.connectSuspend
import java.io.IOException
import java.net.Socket
import java.nio.channels.AsynchronousSocketChannel

/**
 * Represents client for repo-server
 */
class Client(config: ProtoConfig) {
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

    /**
     * Works the same as [Client.request],
     * but type parameter [T] have to be known in compile-time ([T] marked as reified).
     * Additionally, it is more readable because actual class of [T] don't need to be passed
     */
    suspend inline fun <reified T : Any> request(request: Request): T? {
        return request(request, T::class.java)
    }

    /**
     * Makes a request to repo-server
     *
     * @param request input request
     * @param responseValueClass class of [T]
     *
     * @return object that repo-server returned, can be null if response was empty
     *
     * @throws ServerException is repo-server responded error
     * @throws IOException if IO error happened while exchanging data through network
     */
    @Suppress("UNCHECKED_CAST")
    suspend fun <T : Any> request(request: Request, responseValueClass: Class<T>): T? {
        return when (val response = requestRawResponse(request, responseValueClass)) {
            Response.Empty -> null
            is Response.Error -> throw ServerException(response.error)
            is Response.Ok<*> -> response.value as T
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

    /**
     * Makes a request to repo-server.
     *
     * Unlike [request], returns [Response] without additional mappings
     */
    suspend fun requestRawResponse(request: Request, responseValueClass: Class<*>): Response {
        return if(Build.VERSION.SDK_INT >= 26) {
            requestRawResponseAsync(request, responseValueClass)
        } else {
            requestRawResponseSync(request, responseValueClass)
        }
    }

    private suspend fun requestRawResponseSync(request: Request, valueClass: Class<*>): Response {
        return Socket().use { socket ->
            socket.soTimeout = 5000
            socket.connectSuspend(address, 5000)

            contract.writeRequest(request, socket.getOutputStream())
            contract.readResponse(socket.getInputStream(), valueClass)
        }
    }

    @RequiresApi(26)
    private suspend fun requestRawResponseAsync(request: Request, valueClass: Class<*>): Response {
        return AsynchronousSocketChannel.open().use { channel ->
            channel.connectSuspend(address, 5000)

            contract.writeRequest(request, channel)
            contract.readResponse(channel, valueClass)
        }
    }
}