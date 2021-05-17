package com.pelmenstar.projktSens.weather

import com.pelmenstar.projktSens.weather.models.ValueUnit
import com.pelmenstar.projktSens.weather.models.ValueUnitsPacked
import com.pelmenstar.projktSens.weather.models.WeatherInfo
import org.junit.Test

class WeatherInfoTests {
    @Test
    fun serializer_readWrite() {
        TestData.use {
            for (units in TestData.allUnitPackedCombinations()) {
                val tempUnit = ValueUnitsPacked.getTemperatureUnit(units)
                val pressUnit = ValueUnitsPacked.getPressureUnit(units)

                val dateTime = TestData.shortDateTime()
                val temp = TestData.value(tempUnit)
                val hum = TestData.value(ValueUnit.HUMIDITY)
                val press = TestData.value(pressUnit)

                SerializerTestUtil.readWrite(WeatherInfo(units, dateTime, temp, hum, press))
            }
        }
    }
}