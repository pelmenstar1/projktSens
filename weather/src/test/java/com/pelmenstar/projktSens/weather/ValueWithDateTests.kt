package com.pelmenstar.projktSens.weather

import com.pelmenstar.projktSens.weather.models.ValueUnit
import org.junit.Test

class ValueWithDateTests {
    @Test
    fun serializer_readWrite() {
        TestData.use {
            repeat(10) {
                val vd = TestData.valueWithDate(ValueUnit.CELSIUS)

                SerializerTestUtil.readWrite(vd)
            }
        }
    }

}