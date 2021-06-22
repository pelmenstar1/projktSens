package com.pelmenstar.projktSens.serverProtocol

import com.pelmenstar.projktSens.shared.equalsPattern
import com.pelmenstar.projktSens.shared.serialization.ObjectSerializer
import com.pelmenstar.projktSens.shared.serialization.ValueReader
import com.pelmenstar.projktSens.shared.serialization.ValueWriter

class TestObject(private val rawData: ByteArray) {
    companion object {
        @Suppress("unused")
        @JvmField
        val SERIALIZER = object : ObjectSerializer<TestObject> {
            override fun getSerializedObjectSize(value: TestObject): Int {
                return value.rawData.size + 4
            }

            override fun writeObject(value: TestObject, writer: ValueWriter) {
                writer.emitInt32(value.rawData.size)
                writer.emitByteArray(value.rawData)
            }

            override fun readObject(reader: ValueReader): TestObject {
                val size = reader.readInt32()
                val rawData = reader.readByteArray(size)

                return TestObject(rawData)
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        return equalsPattern(other) { o -> rawData contentEquals o.rawData }
    }

    override fun hashCode(): Int {
        return rawData.contentHashCode()
    }
}