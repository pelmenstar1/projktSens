package com.pelmenstar.projktSens.weather

import com.pelmenstar.projktSens.weather.models.UnitValue
import com.pelmenstar.projktSens.weather.models.ValueUnit
import org.junit.Assert
import org.junit.Test

class UnitValueTests {
    @Test
    fun create() {
        fun internal(unit: Int) {
            repeat(5) {
                val value = TestData.value(unit)
                val unitValue = UnitValue.of(value, unit)

                Assert.assertEquals(UnitValue.getUnit(unitValue), unit)
                Assert.assertEquals(UnitValue.getAbsoluteValue(unitValue), value)
            }
        }

        internal(ValueUnit.CELSIUS)
        internal(ValueUnit.FAHRENHEIT)
        internal(ValueUnit.KELVIN)
        internal(ValueUnit.HUMIDITY)
        internal(ValueUnit.MM_OF_MERCURY)
        internal(ValueUnit.PASCAL)
    }
}