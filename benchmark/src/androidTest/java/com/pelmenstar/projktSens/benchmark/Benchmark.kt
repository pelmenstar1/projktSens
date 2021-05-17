package com.pelmenstar.projktSens.benchmark

import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.random.Random

@RunWith(AndroidJUnit4::class)
class Benchmark {
    private var daysInMonth: Int = 0
    private val monthTable = intArrayOf(
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

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    @Test
    fun getDaysInMonth_1() {
        val month = Random(0).nextInt(1, 13)

        benchmarkRule.measureRepeated {
           daysInMonth = monthTable[month - 1]
        }
    }

    @Test
    fun getDaysInMonth_2() {
        val month = Random(0).nextInt(1, 13)

        benchmarkRule.measureRepeated {
            daysInMonth = 28 + (0xEEFBB3 shr (month - 1 shl 1) and 0x3)
        }
    }
}