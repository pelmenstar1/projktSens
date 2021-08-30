package com.pelmenstar.projktSens.shared.tests

import com.pelmenstar.projktSens.shared.StringUtils
import com.pelmenstar.projktSens.shared.round
import org.junit.Test
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertFails

class StringUtilsTests  {
    @Test
    fun twoDigits() {
        for(number in 0..99) {
            val expected = twoDigitsVerified(number)
            val actual = StringUtils.twoDigits(number)

            assertEquals(expected, actual)
        }
    }

    @Test
    fun twoDigits_throwsWhenArgumentOutOfRage() {
        val params = intArrayOf(-1, 100, 1000, -9, Int.MAX_VALUE, Int.MIN_VALUE)

        for(n in params) {
            assertFails { StringUtils.twoDigits(n) }
        }
    }

    @Test
    fun fourDigits() {
        for(number in  0..9999) {
            val expected = fourDigitsVerified(number)
            val actual = StringUtils.fourDigits(number)

            assertEquals(expected, actual)
        }
    }

    @Test
    fun fourDigits_throwsWhenArgumentOutOfRage() {
        val params = intArrayOf(-1, 10000, -900000, Int.MAX_VALUE, Int.MIN_VALUE)

        for(n in params) {
            assertFails { StringUtils.fourDigits(n)  }
        }
    }

    private fun fourDigitsVerified(value: Int): String {
        return buildString(4) {
            when {
                value < 10 -> {
                    append("000")
                }
                value < 100 -> {
                    append("00")
                }
                value < 1000 -> {
                    append("0")
                }
            }

            append(value)
        }
    }

    private fun twoDigitsVerified(value: Int): String {
        return buildString(2) {
            if (value < 10) {
                append('0')
            }

            append(value)
        }
    }
}