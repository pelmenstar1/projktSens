package com.pelmenstar.projktSens.shared.tests

import com.pelmenstar.projktSens.shared.time.ShortDate
import com.pelmenstar.projktSens.shared.time.TimeUtils
import org.junit.Assert
import org.junit.Test
import java.time.LocalDate
import kotlin.random.Random
import kotlin.test.assertEquals

class ShortDateTests {
    @Test
    fun create() {
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
    fun getEpochDay() {
        val random = Random(0)

        repeat(100) {
            val expected = random.nextLong(0, UNTIL_9999_YEAR_DAY)
            val date = LocalDate.ofEpochDay(expected)
            val input = ShortDate.of(date.year, date.monthValue, date.dayOfMonth)

            val actual = ShortDate.toEpochDay(input)

            assertEquals(expected, actual)
        }
    }

    @Test
    fun ofEpochDay() {
        val random = Random(0)

        repeat(100) {
            val input = random.nextLong(0, UNTIL_9999_YEAR_DAY)
            val date = LocalDate.ofEpochDay(input)
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

    private fun randomLocalDate(random: Random): LocalDate {
        val year = random.nextInt(ShortDate.MAX_YEAR)
        val month = random.nextInt(1, 13)
        val day = random.nextInt(1, TimeUtils.getDaysInMonth(year, month) + 1)

        return LocalDate.of(year, month, day)
    }
}