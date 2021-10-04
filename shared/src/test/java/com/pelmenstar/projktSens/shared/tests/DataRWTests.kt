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

        writer.int8(1)
        writer.int16(2)
        writer.int32(3)
        writer.int64(4)

        assertEquals(1, reader.int8())
        assertEquals(2, reader.int16())
        assertEquals(3, reader.int32())
        assertEquals(4, reader.int64())
    }
}