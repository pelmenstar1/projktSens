package com.pelmenstar.projktSens.weather.app

import com.pelmenstar.projktSens.shared.time.PrettyDateFormatter
import com.pelmenstar.projktSens.weather.app.formatters.UnitFormatter
import org.junit.Test
import kotlin.random.Random

class ValueUnitFormatterTests {
    private val random = Random(0)

    @Test
    fun formatValueAndDelta() {
        val unitFormatter = UnitFormatter(arrayOf("a", "ab", "abc"), object: PrettyDateFormatter() {
            override fun appendToday(sb: StringBuilder) {
            }

            override fun appendYesterday(sb: StringBuilder) {
            }

            override fun appendDayAndMonth(month: Int, day: Int, sb: StringBuilder) {
            }

            override fun appendMonth(month: Int, sb: StringBuilder) {
            }
        })

        repeat(10) {
            var value = random.nextFloat() * 100
            if(random.nextBoolean()) {
                value = -value
            }

            var delta = random.nextFloat() * 100
            if(random.nextBoolean()) {
                delta = -delta
            }

            val unit = random.nextInt(0, 3)

            println(unitFormatter.formatValueAndDelta(value, delta, unit))

        }

    }
}