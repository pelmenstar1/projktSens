package com.pelmenstar.projktSens.serverProtocol.repo

import com.pelmenstar.projktSens.serverProtocol.Errors
import com.pelmenstar.projktSens.serverProtocol.HostedProtoConfig
import com.pelmenstar.projktSens.serverProtocol.ServerException
import com.pelmenstar.projktSens.shared.connectSuspend
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

/**
 * Represents client for repo-server
 */
class RepoClient(config: HostedProtoConfig) {
    private val repoContract = config.repoContract
    private val repoAddress: InetSocketAddress = config.socketAddress { repoServerPort }

    suspend inline fun<T:Any> request(command: Int, responseValueClass: Class<T>): T? {
        return request(RepoRequest(command), responseValueClass)
    }

    suspend inline fun<T:Any> request(command: Int, arg: Any, responseValueClass: Class<T>): T? {
        return request(RepoRequest(command, arg), responseValueClass)
    }

    suspend inline fun<reified T:Any> request(command: Int): T? {
        return request(command, T::class.java)
    }

    suspend inline fun<reified T:Any> request(command: Int, arg: Any): T? {
        return request(command, arg, T::class.java)
    }

    /**
     * Works the same as [RepoClient.request],
     * but type parameter [T] have to be known in compile-time ([T] marked as reified).
     * Additionally, it is more readable because actual class of [T] don't need to be passed
     */
    suspend inline fun <reified T:Any> request(request: RepoRequest): T? {
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
    suspend fun <T:Any> request(request: RepoRequest, responseValueClass: Class<T>): T? {
        return when (val response = requestRawResponse(request, responseValueClass)) {
            RepoResponse.Empty -> null
            is RepoResponse.Error -> throw ServerException(response.error)
            is RepoResponse.Ok<*> -> response.value as T

            else -> throw ServerException(Errors.INVALID_RESPONSE)
        }
    }

    suspend inline fun<reified T:Any> requestRawResponse(command: Int): RepoResponse {
        return requestRawResponse(command, T::class.java)
    }

    suspend inline fun<reified T:Any> requestRawResponse(command: Int, arg: Any): RepoResponse {
        return requestRawResponse(command, arg, T::class.java)
    }

    suspend inline fun<T:Any> requestRawResponse(command: Int, responseValueClass: Class<T>): RepoResponse {
        return requestRawResponse(RepoRequest(command), responseValueClass)
    }

    suspend inline fun<T:Any> requestRawResponse(command: Int, arg: Any, responseValueClass: Class<T>): RepoResponse {
        return requestRawResponse(RepoRequest(command, arg), responseValueClass)
    }

    /**
     * Makes a request to repo-server.
     *
     * Unlike [request], returns [RepoResponse] without additional mappings
     */
    suspend fun<T:Any> requestRawResponse(request: RepoRequest, responseValueClass: Class<T>): RepoResponse {
        return withContext(Dispatchers.IO) {
            Socket().use { socket ->
                socket.soTimeout = 5000
                socket.connectSuspend(repoAddress)

                repoContract.writeRequest(request, socket.getOutputStream())
                repoContract.readResponse(socket.getInputStream(), responseValueClass)
            }
        }
    }
}