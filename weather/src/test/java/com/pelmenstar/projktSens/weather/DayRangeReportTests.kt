package com.pelmenstar.projktSens.weather

import com.pelmenstar.projktSens.weather.models.DayRangeReport
import com.pelmenstar.projktSens.weather.models.ValueUnit
import com.pelmenstar.projktSens.weather.models.ValueUnitsPacked
import org.junit.Test

class DayRangeReportTests {

    @Test
    fun serializer_readWrite() {
        TestData.use {
            for (units in TestData.allUnitPackedCombinations()) {
                val tempUnit = ValueUnitsPacked.getTemperatureUnit(units)
                val pressUnit = ValueUnitsPacked.getPressureUnit(units)

                val header = TestData.reportHeader(units)
                val data = Array(20) { randomEntry(tempUnit, pressUnit) }
                val report = DayRangeReport(data, header)

                SerializerTestUtil.readWrite(report)
            }
        }
    }

    private fun randomEntry(tempUnit: Int, pressUnit: Int): DayRangeReport.Entry {
        return DayRangeReport.Entry(
            TestData.shortDate(),
            TestData.value(tempUnit),
            TestData.value(tempUnit),

            TestData.value(ValueUnit.HUMIDITY),
            TestData.value(ValueUnit.HUMIDITY),

            TestData.value(pressUnit),
            TestData.value(pressUnit),
        )
    }
}