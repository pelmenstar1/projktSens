package com.pelmenstar.projktSens.weather

import com.pelmenstar.projktSens.shared.time.*
import com.pelmenstar.projktSens.weather.models.*
import kotlin.random.Random

object TestData {
    private var random = Random(0)

    fun use(block: () -> Unit) {
        random = Random(0)
        block()
    }

    @ShortDateInt
    fun shortDate(): Int {
        val year = random.nextInt(ShortDate.MAX_YEAR)
        val month = random.nextInt(1, 13)
        val day = random.nextInt(1, TimeUtils.getDaysInMonth(year, month) + 1)

        return ShortDate.of(year, month, day)
    }

    @TimeInt
    fun shortTime(): Int {
        return random.nextInt(0, TimeConstants.SECONDS_IN_DAY)
    }

    @ShortDateTimeLong
    fun shortDateTime(): Long {
        return ShortDateTime.of(shortDate(), shortTime())
    }

    fun allUnitPackedCombinations(): IntArray {
        val combs = IntArray(ValueUnit.TEMPERATURE_UNITS.size * ValueUnit.PRESSURE_UNITS.size)
        var i = 0

        for (tempUnit in ValueUnit.TEMPERATURE_UNITS) {
            for (pressUnit in ValueUnit.PRESSURE_UNITS) {
                combs[i++] = ValueUnitsPacked.create(tempUnit, pressUnit)
            }
        }

        return combs
    }

    fun value(unit: Int): Float {
        return when (unit) {
            ValueUnit.CELSIUS -> random.nextDouble(-30.0, 30.0).toFloat()
            ValueUnit.FAHRENHEIT -> random.nextDouble(-140.0, 200.0).toFloat()
            ValueUnit.KELVIN -> random.nextDouble(173.0, 373.0).toFloat()
            ValueUnit.HUMIDITY -> random.nextDouble(50.0, 90.0).toFloat()
            ValueUnit.MM_OF_MERCURY -> random.nextDouble(700.0, 800.0).toFloat()
            ValueUnit.PASCAL -> random.nextDouble(0.0, 133322.0).toFloat()

            else -> throw IllegalArgumentException("unit")
        }
    }

    fun valueWithDate(unit: Int): ValueWithDate {
        return ValueWithDate(shortDateTime(), value(unit))
    }

    fun paramStats(unit: Int): ParameterStats {
        return ParameterStats(
            value(unit),
            value(unit),
            value(unit),
            value(unit)
        )
    }

    fun reportHeader(units: Int): ReportStats {
        val tempUnit = ValueUnitsPacked.getTemperatureUnit(units)
        val pressUnit = ValueUnitsPacked.getPressureUnit(units)

        return ReportStats(
            units,
            paramStats(tempUnit),
            paramStats(ValueUnit.HUMIDITY),
            paramStats(pressUnit)
        )
    }
}