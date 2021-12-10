package com.pelmenstar.projktSens.serverProtocol

import android.os.Build
import androidx.annotation.RequiresApi
import com.pelmenstar.projktSens.shared.connectSuspend
import com.pelmenstar.projktSens.shared.io.Input
import com.pelmenstar.projktSens.shared.io.Output
import java.io.IOException
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

    suspend fun requestMultiple(
        requests: Array<Request>,
        valueClasses: Array<Class<*>>
    ): Array<Any?> {
        return if(Build.VERSION.SDK_INT >= 26) {
            requestMultipleAsync(requests, valueClasses)
        } else {
            requestMultipleBlocking(requests, valueClasses)
        }
    }

    @RequiresApi(26)
    private suspend fun requestMultipleAsync(
        requests: Array<Request>,
        valueClasses: Array<Class<*>>
    ): Array<Any?> {
        return AsynchronousSocketChannel.open().use { channel ->
            channel.connectSuspend(address, 5000)

            val input = Input.of(channel)
            val output = Output.of(channel)

            handleRequestMultiple(input, output, requests, valueClasses)
        }
    }

    private suspend fun requestMultipleBlocking(
        requests: Array<Request>,
        valueClasses: Array<Class<*>>
    ): Array<Any?> {
        return Socket().use { socket ->
            socket.connect(address, 5000)

            val input = Input.of(socket)
            val output = Output.of(socket)

            handleRequestMultiple(input, output, requests, valueClasses)
        }
    }

    private suspend fun handleRequestMultiple(
        input: Input,
        output: Output,
        requests: Array<Request>,
        valueClasses: Array<Class<*>>
    ): Array<Any?> {
        contract.openSession(output, requests.size)
        return Array(requests.size) { i ->
            val request = requests[i]

            val response = writeRequestAndReadResponse(
                request,
                valueClasses[i],
                input,
                output
            )

            handleRawResponse(response)
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
        return if(Build.VERSION.SDK_INT >= 26 && !forceBlocking) {
            requestRawResponseAsync(request, responseValueClass)
        } else {
            requestRawResponseBlocking(request, responseValueClass)
        }
    }

    private suspend fun requestRawResponseBlocking(
        request: Request, valueClass: Class<*>
    ): Response {
        return Socket().use { socket ->
            socket.connect(address, 5000)

            val input = Input.of(socket)
            val output = Output.of(socket)

            contract.openSession(output, 1)

            writeRequestAndReadResponse(
                request,
                valueClass,
                input,
                output
            )
        }
    }

    @RequiresApi(26)
    private suspend fun requestRawResponseAsync(
        request: Request, valueClass: Class<*>
    ): Response {
        return AsynchronousSocketChannel.open().use { channel ->
            channel.connectSuspend(address, 5000)

            val input = Input.of(channel)
            val output = Output.of(channel)

            contract.openSession(output, 1)

            writeRequestAndReadResponse(
                request,
                valueClass,
                input,
                output
            )
        }
    }

    private suspend fun writeRequestAndReadResponse(
        request: Request,
        valueClass: Class<*>,
        input: Input,
        output: Output
    ): Response {
        contract.writeRequest(request, output)

        return contract.readResponse(input, valueClass)
    }
}