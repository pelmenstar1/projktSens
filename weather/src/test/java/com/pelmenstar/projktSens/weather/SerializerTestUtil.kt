package com.pelmenstar.projktSens.weather

import com.pelmenstar.projktSens.shared.serialization.ValueReader
import com.pelmenstar.projktSens.shared.serialization.ValueWriter
import com.pelmenstar.projktSens.shared.serialization.Serializable
import org.junit.Assert

object SerializerTestUtil {
    inline fun<reified T:Any> readWrite(value: T) {
        val serializer = Serializable.getSerializer(T::class.java)

        val buffer = ByteArray(serializer.getSerializedObjectSize(value))
        val writer =
            ValueWriter(buffer)

        serializer.writeObject(value, writer)
        Assert.assertTrue(writer.inEnd())

        val reader =
            ValueReader(buffer)
        val fromBuffer = serializer.readObject(reader)
        Assert.assertTrue(reader.inEnd())

        Assert.assertEquals(value, fromBuffer)
    }
}