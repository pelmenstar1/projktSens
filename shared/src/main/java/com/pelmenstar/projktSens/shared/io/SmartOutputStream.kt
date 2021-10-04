package com.pelmenstar.projktSens.shared.io

import com.pelmenstar.projktSens.shared.writeSuspend
import java.io.OutputStream
import java.net.Socket
import java.nio.channels.AsynchronousByteChannel

abstract class SmartOutputStream {
    private class FromOutputStream(private val outStream: OutputStream): SmartOutputStream() {
        override suspend fun write(buffer: ByteArray) {
            outStream.writeSuspend(buffer)
        }
    }

    private class FromAsyncByteChannel(private val channel: AsynchronousByteChannel): SmartOutputStream() {
        override suspend fun write(buffer: ByteArray) {
            channel.writeSuspend(buffer)
        }
    }

    abstract suspend fun write(buffer: ByteArray)

    companion object {
        fun toSmart(socket: Socket): SmartOutputStream {
            return toSmart(socket.getOutputStream())
        }

        fun toSmart(outStream: OutputStream): SmartOutputStream {
            return FromOutputStream(outStream)
        }

        fun toSmart(channel: AsynchronousByteChannel): SmartOutputStream {
            return FromAsyncByteChannel(channel)
        }
    }
}