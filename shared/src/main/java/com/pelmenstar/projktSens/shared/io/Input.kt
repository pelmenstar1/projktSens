package com.pelmenstar.projktSens.shared.io

import com.pelmenstar.projktSens.shared.readNBufferedSuspend
import com.pelmenstar.projktSens.shared.readNBufferedToByteArraySuspend
import java.io.InputStream
import java.net.Socket
import java.nio.channels.AsynchronousByteChannel

abstract class Input {
    private class OfInputStream(private val inputStream: InputStream): Input() {
        override suspend fun readN(n: Int): ByteArray {
            return inputStream.readNBufferedSuspend(n)
        }
    }

    private class OfAsyncByteChanel(private val channel: AsynchronousByteChannel): Input() {
        override suspend fun readN(n: Int): ByteArray {
            return channel.readNBufferedToByteArraySuspend(n)
        }
    }

    abstract suspend fun readN(n: Int): ByteArray

    companion object {
        fun of(socket: Socket): Input {
            return of(socket.getInputStream())
        }

        fun of(stream: InputStream): Input {
            return OfInputStream(stream)
        }

        fun of(channel: AsynchronousByteChannel): Input {
            return OfAsyncByteChanel(channel)
        }
    }
}