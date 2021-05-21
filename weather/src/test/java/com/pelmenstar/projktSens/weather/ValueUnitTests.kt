package com.pelmenstar.projktSens.weather

import com.pelmenstar.projktSens.weather.models.ValueUnit
import org.junit.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ValueUnitTests {
    @Test
    fun isTemperatureUnit() {
        assertTrue(ValueUnit.isTemperatureUnit(ValueUnit.CELSIUS))
        assertTrue(ValueUnit.isTemperatureUnit(ValueUnit.KELVIN))
        assertTrue(ValueUnit.isTemperatureUnit(ValueUnit.FAHRENHEIT))

        assertFalse(ValueUnit.isTemperatureUnit(ValueUnit.HUMIDITY))
        assertFalse(ValueUnit.isTemperatureUnit(ValueUnit.MM_OF_MERCURY))
        assertFalse(ValueUnit.isTemperatureUnit(ValueUnit.PASCAL))
    }

    @Test
    fun isPressureUnit() {
        assertTrue(ValueUnit.isPressureUnit(ValueUnit.MM_OF_MERCURY))
        assertTrue(ValueUnit.isPressureUnit(ValueUnit.PASCAL))
        assertFalse(ValueUnit.isPressureUnit(ValueUnit.CELSIUS))

        assertFalse(ValueUnit.isPressureUnit(ValueUnit.KELVIN))
        assertFalse(ValueUnit.isPressureUnit(ValueUnit.FAHRENHEIT))
        assertFalse(ValueUnit.isPressureUnit(ValueUnit.HUMIDITY))
    }

}