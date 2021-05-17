package com.pelmenstar.projktSens.shared.tests

import com.pelmenstar.projktSens.shared.Bytes
import org.junit.Test
import kotlin.random.Random
import kotlin.test.assertEquals

class BytesTests {
    private val random = Random(0)

    @Test
    fun int16_readWrite() {
        repeat(10) {
            val num = random.nextInt(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()

            primitive_readWrite(2, num, Bytes::writeShort, Bytes::readShort)
        }
    }

    @Test
    fun int32_readWrite() {
        repeat(10) {
            val num = random.nextInt()

            primitive_readWrite(4, num, Bytes::writeInt, Bytes::readInt)
        }
    }

    @Test
    fun float_readWrite() {
        repeat(10) {
            val num = random.nextFloat()

            primitive_readWrite(4, num, Bytes::writeFloat, Bytes::readFloat)
        }
    }

    @Test
    fun int64_readWrite() {
        repeat(10) {
            val num = random.nextLong()

            primitive_readWrite(8, num, Bytes::writeLong, Bytes::readLong)
        }
    }

    private inline fun<T:Any> primitive_readWrite(
        sizeInBytes: Int,
        value: T,
        write: (T, ByteArray, Int) -> Unit,
        read: (ByteArray, Int) -> T
    ) {
        val buffer = ByteArray(sizeInBytes + 2)
        write(value, buffer, 1)
        val fromBuffer = read(buffer, 1)

        assertEquals(0, buffer[0])
        assertEquals(0, buffer[buffer.size - 1])
        assertEquals(value, fromBuffer)
    }
}