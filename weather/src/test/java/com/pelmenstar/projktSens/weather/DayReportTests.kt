package com.pelmenstar.projktSens.weather

import com.pelmenstar.projktSens.weather.models.DayReport
import com.pelmenstar.projktSens.weather.models.ValueUnit
import com.pelmenstar.projktSens.weather.models.ValueUnitsPacked
import org.junit.Test

class DayReportTests {
    @Test
    fun serializer_readWrite() {
        TestData.use {
            for (units in TestData.allUnitPackedCombinations()) {
                val tempUnit = ValueUnitsPacked.getTemperatureUnit(units)
                val pressUnit = ValueUnitsPacked.getPressureUnit(units)

                val header = TestData.reportHeader(units)
                val data = Array(20) { randomEntry(tempUnit, pressUnit) }
                val report = DayReport(data, header)

                SerializerTestUtil.readWrite(report)
            }
        }
    }

    private fun randomEntry(tempUnit: Int, pressUnit: Int): DayReport.Entry {
        return DayReport.Entry(
            TestData.shortTime(),
            TestData.value(tempUnit),
            TestData.value(ValueUnit.HUMIDITY),
            TestData.value(pressUnit)
        )
    }
}