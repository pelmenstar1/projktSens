package com.pelmenstar.projktSens.shared.io

import com.pelmenstar.projktSens.shared.readNBufferedSuspend
import com.pelmenstar.projktSens.shared.readNBufferedToByteArraySuspend
import com.pelmenstar.projktSens.shared.readNSuspend
import java.io.InputStream
import java.net.Socket
import java.nio.channels.AsynchronousByteChannel

abstract class SmartInputStream {
    private class FromInputStream(private val inputStream: InputStream): SmartInputStream() {
        override suspend fun readN(n: Int): ByteArray {
            return inputStream.readNSuspend(n)
        }
        override suspend fun readNBuffered(n: Int, bufferSize: Int): ByteArray {
            return inputStream.readNBufferedSuspend(n, bufferSize)
        }
    }

    private class FromAsyncByteChanel(private val channel: AsynchronousByteChannel): SmartInputStream() {
        override suspend fun readN(n: Int): ByteArray {
            return channel.readNBufferedToByteArraySuspend(n)
        }

        override suspend fun readNBuffered(n: Int, bufferSize: Int): ByteArray {
            return channel.readNBufferedToByteArraySuspend(n, bufferSize)
        }
    }

    abstract suspend fun readN(n: Int): ByteArray
    abstract suspend fun readNBuffered(n: Int, bufferSize: Int = 1024): ByteArray

    companion object {
        fun toSmart(socket: Socket): SmartInputStream {
            return toSmart(socket.getInputStream())
        }

        fun toSmart(stream: InputStream): SmartInputStream {
            return FromInputStream(stream)
        }

        fun toSmart(channel: AsynchronousByteChannel): SmartInputStream {
            return FromAsyncByteChanel(channel)
        }
    }
}