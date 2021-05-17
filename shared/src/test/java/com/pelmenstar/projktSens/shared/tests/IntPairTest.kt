package com.pelmenstar.projktSens.shared.tests

import com.pelmenstar.projktSens.shared.IntPair
import org.junit.Assert
import org.junit.Test
import kotlin.random.Random
import kotlin.test.assertEquals

class IntPairTest {
    private val random = Random(0)

    @Test
    fun of() {
        repeat(10) {
            val first = random.nextInt()
            val second = random.nextInt()

            val range = IntPair.of(first, second)

            assertEquals(IntPair.getFirst(range), first)
            assertEquals(IntPair.getSecond(range), second)
        }
    }
}