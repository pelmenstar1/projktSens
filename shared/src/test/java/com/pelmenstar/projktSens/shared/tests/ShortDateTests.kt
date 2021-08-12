package com.pelmenstar.projktSens.shared.tests

import com.pelmenstar.projktSens.shared.time.ShortDate
import com.pelmenstar.projktSens.shared.time.TimeUtils
import org.junit.Test
import java.time.LocalDate
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertFails

class ShortDateTests {
    @Test
    fun of() {
        val random = Random(0)

        repeat(10) {
            val year = random.nextInt(ShortDate.MAX_YEAR)
            val month = random.nextInt(1, 13)
            val day = random.nextInt(1, TimeUtils.getDaysInMonth(year, month) + 1)

            val shortDate = ShortDate.of(year, month, day)

            assertEquals(year, ShortDate.getYear(shortDate))
            assertEquals(month, ShortDate.getMonth(shortDate))
            assertEquals(day, ShortDate.getDayOfMonth(shortDate))
        }
    }

    @Test
    fun of_fails_on_invalid_args() {
        fun assertOfFails(year: Int, month: Int, day: Int) {
            assertFails {
                ShortDate.of(year, month, day)
            }
        }

        // year
        assertOfFails(-1, 1, 1)
        assertOfFails(ShortDate.MAX_YEAR + 1, 1, 1)

        // month
        assertOfFails(2021, 0, 1)
        assertOfFails(2021, 13, 1)

        // day
        assertOfFails(2021, 1, 0)
        assertOfFails(2021, 1, 32)
        assertOfFails(
            2021,
            1,
            TimeUtils.getDaysInMonth(2021, 1) + 1
        )
    }

    @Test
    fun toEpochDay() {
        val random = Random(0)

        repeat(100) {
            val expected = random.nextInt(0, UNTIL_9999_YEAR_DAY)
            val date = LocalDate.ofEpochDay(expected.toLong())
            val input = ShortDate.of(date.year, date.monthValue, date.dayOfMonth)

            val actual = ShortDate.toEpochDay(input)

            assertEquals(expected, actual)
        }
    }

    @Test
    fun toEpochDay_fails_on_invalid_date() {
        assertDateInputMethodFails(ShortDate::toEpochDay)
    }

    @Test
    fun ofEpochDay() {
        val random = Random(0)

        repeat(100) {
            val input = random.nextInt(0, UNTIL_9999_YEAR_DAY)
            val date = LocalDate.ofEpochDay(input.toLong())
            val expectedYear = date.year
            val expectedMonth = date.monthValue
            val expectedDay = date.dayOfMonth

            val actual = ShortDate.ofEpochDay(input)

            assertEquals(expectedYear, ShortDate.getYear(actual))
            assertEquals(expectedMonth, ShortDate.getMonth(actual))
            assertEquals(expectedDay, ShortDate.getDayOfMonth(actual))
        }
    }

    @Test
    fun ofEpochDay_fails_on_negative_epoch_day() {
        assertFails { ShortDate.ofEpochDay(-1) }
    }

    @Test
    fun getDayOfYear() {
        val random = Random(0)

        repeat(100) {
            val date = randomLocalDate(random)
            val input = ShortDate.of(date.year, date.monthValue, date.dayOfMonth)
            val expectedDayOfYear = date.dayOfYear

            val actual = ShortDate.getDayOfYear(input)

            assertEquals(expectedDayOfYear, actual)
        }
    }

    @Test
    fun getDayOfYear_fails_on_invalid_date() {
        assertDateInputMethodFails(ShortDate::getDayOfYear)
    }

    @Test
    fun getDayOfWeek() {
        val random = Random(0)

        repeat(20) {
            val date = randomLocalDate(random)
            val input = ShortDate.of(date.year, date.monthValue, date.dayOfMonth)
            val expected = date.dayOfWeek.value
            val actual = ShortDate.getDayOfWeek(input)

            assertEquals(expected, actual)
        }
    }

    @Test
    fun getDayOfWeek_fails_on_invalid_date() {
        assertDateInputMethodFails(ShortDate::getDayOfWeek)
    }

    private fun assertDateInputMethodFails(method: (Int) -> Any) {
        fun assertMethodFails(year: Int, month: Int, day: Int) {
            assertFails("year: $year, month: $month, day: $day") {
                method(ShortDate.ofInternal(year, month, day))
            }
        }

        assertMethodFails(-1, 1, 1)
        assertMethodFails(ShortDate.MAX_YEAR + 1, 1, 1)

        assertMethodFails(2021, 0, 1)
        assertMethodFails(2021, 13, 1)

        assertMethodFails(2021, 1, 0)
        assertMethodFails(2021, 1, 32)
    }

    private fun randomLocalDate(random: Random): LocalDate {
        val year = random.nextInt(ShortDate.MAX_YEAR)
        val month = random.nextInt(1, 13)
        val day = random.nextInt(1, TimeUtils.getDaysInMonth(year, month) + 1)

        return LocalDate.of(year, month, day)
    }
}