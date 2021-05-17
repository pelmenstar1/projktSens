package com.pelmenstar.projktSens.weather

import com.pelmenstar.projktSens.weather.models.ValueUnit
import org.junit.Assert
import org.junit.Test

class ValueUnitTests {
    @Test
    fun isTemperatureUnit() {
        Assert.assertTrue(
            ValueUnit.isTemperatureUnit(
                ValueUnit.CELSIUS))
        Assert.assertTrue(
            ValueUnit.isTemperatureUnit(
                ValueUnit.KELVIN))
        Assert.assertTrue(
            ValueUnit.isTemperatureUnit(
                ValueUnit.FAHRENHEIT))

        Assert.assertFalse(
            ValueUnit.isTemperatureUnit(
                ValueUnit.HUMIDITY))
        Assert.assertFalse(
            ValueUnit.isTemperatureUnit(
                ValueUnit.MM_OF_MERCURY))
        Assert.assertFalse(
            ValueUnit.isTemperatureUnit(
                ValueUnit.PASCAL))
    }

    @Test
    fun isPressureUnit() {
        Assert.assertTrue(
            ValueUnit.isPressureUnit(
                ValueUnit.MM_OF_MERCURY))
        Assert.assertTrue(
            ValueUnit.isPressureUnit(
                ValueUnit.PASCAL))

        Assert.assertFalse(
            ValueUnit.isPressureUnit(
                ValueUnit.CELSIUS))
        Assert.assertFalse(
            ValueUnit.isPressureUnit(
                ValueUnit.KELVIN))
        Assert.assertFalse(
            ValueUnit.isPressureUnit(
                ValueUnit.FAHRENHEIT))
        Assert.assertFalse(
            ValueUnit.isPressureUnit(
                ValueUnit.HUMIDITY))
    }

}