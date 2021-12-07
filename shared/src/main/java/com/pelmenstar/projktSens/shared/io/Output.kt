package com.pelmenstar.projktSens.shared.io

import com.pelmenstar.projktSens.shared.writeSuspend
import java.io.OutputStream
import java.net.Socket
import java.nio.channels.AsynchronousByteChannel

abstract class Output {
    private class OfOutputStream(private val outStream: OutputStream): Output() {
        override suspend fun write(buffer: ByteArray) {
            outStream.writeSuspend(buffer)
        }
    }

    private class OfAsyncByteChannel(private val channel: AsynchronousByteChannel): Output() {
        override suspend fun write(buffer: ByteArray) {
            channel.writeSuspend(buffer)
        }
    }

    abstract suspend fun write(buffer: ByteArray)

    companion object {
        fun of(socket: Socket): Output {
            return of(socket.getOutputStream())
        }

        fun of(outStream: OutputStream): Output {
            return OfOutputStream(outStream)
        }

        fun of(channel: AsynchronousByteChannel): Output {
            return OfAsyncByteChannel(channel)
        }
    }
}