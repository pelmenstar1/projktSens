@file:Suppress("NewApi")

package com.pelmenstar.projktSens.shared

import kotlinx.coroutines.withTimeout
import java.io.IOException
import java.io.InputStream
import java.net.SocketAddress
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousByteChannel
import java.nio.channels.AsynchronousServerSocketChannel
import java.nio.channels.AsynchronousSocketChannel
import java.nio.channels.CompletionHandler
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.math.min

/**
 * Reads data from [InputStream] and writes it to start of [buffer].
 *
 * @throws IOException if the length of data read from [InputStream] `!= [buffer].size` ,
 * or another IO error happened
 */
fun InputStream.readAndThrowIfNotEnough(buffer: ByteArray) {
    return readAndThrowIfNotEnough(buffer, 0, buffer.size)
}

/**
 * Reads data from the [InputStream] and writes it to [buffer], with [length], at specified [offset].
 * Internally this method does the same as [readSuspend], but the reason to use exactly this method described below:
 * - If you uncover compiled Java Bytecode, you can note that every `suspend` method returns instance of [Object].
 * So if `suspend` method returns for example [Int], internally it will be boxed, which causes unnecessary heap allocation.
 * And to avoid heap allocation, [readAndThrowIfNotEnough] can be used. All the same, it is necessary to check if
 * all data is read from [InputStream]
 *
 * @throws IOException if the length of data read from [InputStream] `!= [length]`,
 *                     or another IO error happened
 */
fun InputStream.readAndThrowIfNotEnough(
    buffer: ByteArray,
    offset: Int,
    length: Int
) {
    val bytesRead = read(buffer, offset, length)

    if (bytesRead != length) {
        throw IOException("Cannot read data")
    }
}

/**
 * Reads data from [InputStream] and writes to internal buffer with specified [size].
 * Internally it calls [readAndThrowIfNotEnough].
 *
 * @throws IOException if the length of data read from [InputStream] `!= [size]`
 */
fun InputStream.readN(size: Int): ByteArray {
    val buffer = ByteArray(size)
    readAndThrowIfNotEnough(buffer, 0, size)

    return buffer
}

fun InputStream.readNBuffered(size: Int, bufferSize: Int = 1024): ByteArray {
    val bytes = ByteArray(size)

    if (size < bufferSize) {
        readAndThrowIfNotEnough(bytes)
    } else {
        var offset = 0
        while (offset < size) {
            val expectedToRead = min(size - offset, bufferSize)
            readAndThrowIfNotEnough(bytes, offset, expectedToRead)
            offset += expectedToRead
        }
    }

    return bytes
}

suspend fun AsynchronousSocketChannel.connectSuspend(address: SocketAddress) {
    suspendCoroutine<Unit> { cont ->
        connect(address, cont, ConnectCompletionHandler)
    }
}

suspend fun AsynchronousSocketChannel.connectSuspend(address: SocketAddress, timeout: Int) {
    withTimeout(timeout.toLong()) {
        connectSuspend(address)
    }
}

suspend fun AsynchronousServerSocketChannel.acceptSuspend(): AsynchronousSocketChannel {
    return suspendCoroutine { cont ->
        accept(cont, AcceptCompletionHandler)
    }
}

suspend fun AsynchronousByteChannel.readSuspend(buffer: ByteArray): Int {
    return readSuspend(ByteBuffer.wrap(buffer))
}

suspend fun AsynchronousByteChannel.readSuspend(buffer: ByteBuffer): Int {
    return suspendCoroutine { cont ->
        read(buffer, cont, IntIntUnitCompletionHandler)
    }
}

suspend fun AsynchronousByteChannel.readToArraySuspend(n: Int): ByteArray {
    val array = ByteArray(n)
    val buffer = ByteBuffer.wrap(array)
    readAndThrowIfNotEnough(buffer)

    return array
}

suspend fun AsynchronousByteChannel.readAndThrowIfNotEnough(buffer: ByteBuffer) {
    suspendCoroutine<Unit> { c ->
        val initialRemaining = buffer.remaining()
        val initialPos = buffer.position()

        read(buffer, c, object : CompletionHandler<Int, Continuation<Unit>> {
            override fun completed(bytesRead: Int, cont: Continuation<Unit>) {
                val result: Result<Unit> = if (bytesRead == initialRemaining) {
                    unitResult()
                } else {
                    Result.failure(IOException("Cannot read data"))
                }

                buffer.position(initialPos)
                cont.resumeWith(result)
            }

            override fun failed(th: Throwable, cont: Continuation<Unit>) {
                cont.resumeWithException(th)
            }
        })
    }
}

suspend fun AsynchronousByteChannel.readNBufferedToByteArraySuspend(
    size: Int, bufferSize: Int = 1024
): ByteArray {
    val array = ByteArray(size)
    val buffer = ByteBuffer.wrap(array)

    if (size < bufferSize) {
        readAndThrowIfNotEnough(buffer)
    } else {
        var offset = 0
        while (offset < size) {
            val expectedToRead = min(size - offset, bufferSize)
            readAndThrowIfNotEnough(buffer)
            offset += expectedToRead
            buffer.position(offset)
        }
    }

    return array
}

suspend fun AsynchronousByteChannel.writeSuspend(buffer: ByteArray) {
    writeSuspend(ByteBuffer.wrap(buffer))
}

suspend fun AsynchronousByteChannel.writeSuspend(buffer: ByteBuffer) {
    suspendCoroutine<Unit> { c ->
        write(buffer, c, IntUnitCompletionHandler)
    }
}

private object ConnectCompletionHandler : CompletionHandler<Void, Continuation<Unit>> {
    override fun completed(p0: Void?, cont: Continuation<Unit>) {
        cont.resumeWithSuccess()
    }

    override fun failed(th: Throwable, cont: Continuation<Unit>) {
        cont.resumeWithException(th)
    }
}

private object AcceptCompletionHandler :
    CompletionHandler<AsynchronousSocketChannel, Continuation<AsynchronousSocketChannel>> {

    override fun completed(
        client: AsynchronousSocketChannel,
        cont: Continuation<AsynchronousSocketChannel>
    ) {
        cont.resume(client)
    }

    override fun failed(th: Throwable, cont: Continuation<AsynchronousSocketChannel>) {
        cont.resumeWithException(th)
    }
}

private object IntIntUnitCompletionHandler : CompletionHandler<Int, Continuation<Int>> {
    override fun completed(arg: Int, cont: Continuation<Int>) = cont.resume(arg)
    override fun failed(th: Throwable, cont: Continuation<Int>) = cont.resumeWithException(th)
}

private object IntUnitCompletionHandler : CompletionHandler<Int, Continuation<Unit>> {
    override fun completed(p0: Int?, cont: Continuation<Unit>) = cont.resumeWithSuccess()
    override fun failed(th: Throwable, cont: Continuation<Unit>) = cont.resumeWithException(th)
}