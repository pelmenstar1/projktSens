package com.pelmenstar.projktSens.shared.tests

import com.pelmenstar.projktSens.shared.time.TimeUtils
import org.junit.Test
import java.time.chrono.IsoChronology
import kotlin.test.assertEquals

const val UNTIL_9999_YEAR = 253402214399
const val UNTIL_9999_YEAR_DAY = 2932895

class TimeUtilsTests  {
    @Test
    fun getDaysInMonth() {
        assertEquals(29, TimeUtils.getDaysInMonth(2004, 2)) // leap, february
        for(month in 1..12) {
            // non-leap year
            assertEquals(
                daysInMonthTable[month],
                TimeUtils.getDaysInMonth(2005, month)
            )
        }
    }

    @Test
    fun isLeapYear() {
        val iso = IsoChronology.INSTANCE

        for(year in 0 until 9999) {
            val expected = iso.isLeapYear(year.toLong())
            val actual = TimeUtils.isLeapYear(year)

            assertEquals(expected, actual)
        }
    }

    companion object {
        private val daysInMonthTable = intArrayOf(
                0,
                31,
                28,
                31,
                30,
                31,
                30,
                31,
                31,
                30,
                31,
                30,
                31
        )
    }
}