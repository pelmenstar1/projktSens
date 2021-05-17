package com.pelmenstar.projktSens.shared.tests

import com.pelmenstar.projktSens.shared.serialization.ValueReader
import com.pelmenstar.projktSens.shared.serialization.ValueWriter
import org.junit.Test
import kotlin.test.assertEquals

class DataRWTests {
    @Test
    fun readWrite() {
        val buffer = ByteArray(16)
        val writer =
            ValueWriter(buffer, 1)
        val reader =
            ValueReader(buffer, 1)

        writer.emitInt8(1)
        writer.emitInt16(2)
        writer.emitInt32(3)
        writer.emitInt64(4)

        assertEquals(1, reader.readInt8())
        assertEquals(2, reader.readInt16())
        assertEquals(3, reader.readInt32())
        assertEquals(4, reader.readInt64())
    }
}