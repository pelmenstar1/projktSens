package com.pelmenstar.projktSens.shared.tests

import com.pelmenstar.projktSens.shared.time.ShortDate
import com.pelmenstar.projktSens.shared.time.ShortDateTime
import com.pelmenstar.projktSens.shared.time.TimeConstants
import com.pelmenstar.projktSens.shared.time.TimeUtils
import org.junit.Assert
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.random.Random
import kotlin.test.assertEquals

class ShortDateTimeTests {
    @Test
    fun of() {
        val random = Random(0)

        repeat(50) {
            val year = random.nextInt(ShortDate.MAX_YEAR)
            val month = random.nextInt(1, 12)
            val day = random.nextInt(1, TimeUtils.getDaysInMonth(year, month) + 1)

            val shortDate = ShortDate.of(year, month, day)
            val time = random.nextInt(TimeConstants.SECONDS_IN_DAY)

            val dateTime = ShortDateTime.of(shortDate, time)

            assertEquals(shortDate, ShortDateTime.getDate(dateTime))
            assertEquals(time, ShortDateTime.getTime(dateTime))
        }
    }

    @Test
    fun toEpochSecond() {
        val random = Random(0)

        repeat(20) {
            val epoch = random.nextLong(UNTIL_9999_YEAR)
            val dateTime = LocalDateTime.ofEpochSecond(epoch, 0, ZoneOffset.UTC)

            val input = ShortDateTime.of(
                    dateTime.year,
                    dateTime.monthValue,
                    dateTime.dayOfMonth,
                dateTime.toLocalTime().toSecondOfDay()
            )
            val expected = dateTime.toEpochSecond(ZoneOffset.UTC)
            val actual = ShortDateTime.toEpochSecond(input)

            assertEquals(expected, actual)
        }
    }

    @Test
    fun ofEpochSecond() {
        val random = Random(0)

        repeat(100) {
            val input = random.nextLong(UNTIL_9999_YEAR)
            val dateTime = LocalDateTime.ofEpochSecond(input, 0, ZoneOffset.UTC)
            val expected = ShortDateTime.of(
                dateTime.year,
                dateTime.monthValue,
                dateTime.dayOfMonth,
                dateTime.toLocalTime().toSecondOfDay()
            )

            val actual = ShortDateTime.ofEpochSecond(input)

            assertEquals(expected, actual)
        }
    }
}