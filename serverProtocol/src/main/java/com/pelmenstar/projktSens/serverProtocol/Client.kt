package com.pelmenstar.projktSens.serverProtocol

import com.pelmenstar.projktSens.shared.connectSuspend
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.Socket

/**
 * Represents client for repo-server
 */
class Client(config: ProtoConfig) {
    private val contract = config.contract
    private val address = config.socketAddress

    suspend inline fun<T:Any> request(command: Int, responseValueClass: Class<T>): T? {
        return request(Request(command), responseValueClass)
    }

    suspend inline fun<T:Any> request(command: Int, arg: Any, responseValueClass: Class<T>): T? {
        return request(Request(command, arg), responseValueClass)
    }

    suspend inline fun<reified T:Any> request(command: Int): T? {
        return request(command, T::class.java)
    }

    suspend inline fun<reified T:Any> request(command: Int, arg: Any): T? {
        return request(command, arg, T::class.java)
    }

    /**
     * Works the same as [Client.request],
     * but type parameter [T] have to be known in compile-time ([T] marked as reified).
     * Additionally, it is more readable because actual class of [T] don't need to be passed
     */
    suspend inline fun <reified T:Any> request(request: Request): T? {
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
    suspend fun <T:Any> request(request: Request, responseValueClass: Class<T>): T? {
        return when (val response = requestRawResponse(request, responseValueClass)) {
            Response.Empty -> null
            is Response.Error -> throw ServerException(response.error)
            is Response.Ok<*> -> response.value as T

            else -> throw ServerException(Errors.INVALID_RESPONSE)
        }
    }

    suspend inline fun<reified T:Any> requestRawResponse(command: Int): Response {
        return requestRawResponse(command, T::class.java)
    }

    suspend inline fun<reified T:Any> requestRawResponse(command: Int, arg: Any): Response {
        return requestRawResponse(command, arg, T::class.java)
    }

    suspend inline fun<T:Any> requestRawResponse(command: Int, responseValueClass: Class<T>): Response {
        return requestRawResponse(Request(command), responseValueClass)
    }

    suspend inline fun<T:Any> requestRawResponse(command: Int, arg: Any, responseValueClass: Class<T>): Response {
        return requestRawResponse(Request(command, arg), responseValueClass)
    }

    /**
     * Makes a request to repo-server.
     *
     * Unlike [request], returns [Response] without additional mappings
     */
    suspend fun<T:Any> requestRawResponse(request: Request, responseValueClass: Class<T>): Response {
        return withContext(Dispatchers.IO) {
            Socket().use { socket ->
                socket.soTimeout = 5000
                socket.connectSuspend(address, 5000)

                contract.writeRequest(request, socket.getOutputStream())
                contract.readResponse(socket.getInputStream(), responseValueClass)
            }
        }
    }
}