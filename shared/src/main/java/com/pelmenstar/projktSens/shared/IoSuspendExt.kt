package com.pelmenstar.projktSens.shared

import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketAddress
import java.nio.charset.Charset
import kotlin.coroutines.suspendCoroutine
import kotlin.math.min

/*
private class ClosableCompletionHandler(private val closeable: Closeable): CompletionHandler {
    override fun invoke(cause: Throwable?) {
        try {
            closeable.close()
        } catch (e: Exception) {
        }
    }
}
 */

/**
 * Suspend version of [Socket.connect]
 */
suspend fun Socket.connectSuspend(address: SocketAddress) {
    suspendCancellableCoroutine<Unit> { cont ->
        //cont.invokeOnCancellation(ClosableCompletionHandler(this))

        connect(address)
        cont.resumeWith(UnitResult.SUCCESS)
    }
}

suspend fun Socket.connectSuspend(address: SocketAddress, timeout: Int) {
    suspendCoroutine<Unit> { cont ->
        connect(address, timeout)
        cont.resumeWith(UnitResult.SUCCESS)
    }
}

/**
 * Suspend version of [Socket.bind]
 */
suspend fun ServerSocket.bindSuspend(address: SocketAddress, backlog: Int) {
    suspendCancellableCoroutine<Unit> { cont ->
        //cont.invokeOnCancellation(ClosableCompletionHandler(this))

        bind(address, backlog)
        cont.resumeWith(UnitResult.SUCCESS)
    }
}

/**
 * Suspend version of [ServerSocket.accept]
 */
suspend fun ServerSocket.acceptSuspend(): Socket {
    return suspendCancellableCoroutine { cont ->
        //cont.invokeOnCancellation(ClosableCompletionHandler(this))

        val socket = accept()

        cont.resumeWith(Result.success(socket))
    }
}

/**
 * Suspend version of [OutputStream.write]
 */
suspend fun OutputStream.writeSuspend(buffer: ByteArray) {
    suspendCoroutine<Unit> { cont ->
        write(buffer, 0, buffer.size)
        cont.resumeWith(UnitResult.SUCCESS)
    }
}

/**
 * Suspend version of [InputStream.read]
 */
suspend fun InputStream.readSuspend(buffer: ByteArray): Int {
    return readSuspend(buffer, 0, buffer.size)
}

/**
 * Suspend version of [InputStream.read]
 */
suspend fun InputStream.readSuspend(buffer: ByteArray, offset: Int, length: Int): Int {
    return suspendCoroutine { cont ->
        val bytesRead = read(buffer, offset, length)

        cont.resumeWith(Result.success(bytesRead))
    }
}

/**
 * Reads data from [InputStream] and writes it to start of [buffer].
 *
 * @throws IOException if the length of data read from [InputStream] `!= [buffer].size` ,
 * or another IO error happened
 */
suspend fun InputStream.readSuspendAndThrowIfNotEnough(buffer: ByteArray) {
    return readSuspendAndThrowIfNotEnough(buffer, 0, buffer.size)
}

/**
 * Reads data from the [InputStream] and writes it to [buffer], with [length], at specified [offset].
 * Internally this method does the same as [readSuspend], but the reason to use exactly this method described below:
 * - If you uncover compiled Java Bytecode, you can note that every `suspend` method returns instance of [Object].
 * So if `suspend` method returns for example [Int], internally it will be boxed, which causes unnecessary heap allocation.
 * And to avoid heap allocation, [readSuspendAndThrowIfNotEnough] can be used. All the same, it is necessary to check if
 * all data is read from [InputStream]
 *
 * @throws IOException if the length of data read from [InputStream] `!= [length]`,
 *                     or another IO error happened
 */
suspend fun InputStream.readSuspendAndThrowIfNotEnough(buffer: ByteArray, offset: Int, length: Int) {
    return suspendCoroutine { cont ->
        val bytesRead = read(buffer, offset, length)

        val result = if(bytesRead != length) {
            Result.failure(IOException("Cannot read data"))
        } else {
            UnitResult.SUCCESS
        }

        cont.resumeWith(result)
    }
}

/**
 * Reads data from [InputStream] and writes to internal buffer with specified [size].
 * Internally it calls [readSuspendAndThrowIfNotEnough].
 *
 * @throws IOException if the length of data read from [InputStream] `!= [size]`
 */
suspend fun InputStream.readNSuspend(size: Int): ByteArray {
    val buffer = ByteArray(size)
    readSuspendAndThrowIfNotEnough(buffer, 0, size)

    return buffer
}

suspend fun InputStream.readNBufferedSuspend(size: Int, bufferSize: Int = 1024): ByteArray {
    val bytes = ByteArray(size)

    if(size < bufferSize) {
        readSuspendAndThrowIfNotEnough(bytes)
    } else {
        var offset = 0
        while(offset < size) {
            val expectedToRead = min(size - offset, bufferSize)
            readSuspendAndThrowIfNotEnough(bytes, offset, expectedToRead)
            offset += expectedToRead
        }
    }

    return bytes
}

suspend fun OutputStream.writeString(str: String, charset: Charset) {
    val bytes = str.toByteArray(charset)

    writeSuspend(buildByteArray(bytes.size + 4) {
        writeInt(0, bytes.size)
        System.arraycopy(bytes, 0, this, 4, bytes.size)
    })
}

suspend fun InputStream.readString(charset: Charset, bufferSize: Int = 1024): String {
    val byteLengthBuffer = readNSuspend(4)
    val byteLength = byteLengthBuffer.getInt(0)
    val bytes = readNBufferedSuspend(byteLength, bufferSize)

    return String(bytes, charset)
}