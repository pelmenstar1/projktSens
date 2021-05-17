package com.pelmenstar.projktSens.weather

import org.junit.Test

class ReportStatsTests {
    @Test
    fun serializer_readWrite() {
        TestData.use {
            for (units in TestData.allUnitPackedCombinations()) {
                val header = TestData.reportHeader(units)

                SerializerTestUtil.readWrite(header)
            }
        }
    }
}