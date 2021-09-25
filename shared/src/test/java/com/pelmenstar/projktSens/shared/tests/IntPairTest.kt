package com.pelmenstar.projktSens.shared.tests

import com.pelmenstar.projktSens.shared.IntPair
import org.junit.Test
import kotlin.random.Random
import kotlin.test.assertEquals

class IntPairTest {
    private val random = Random(0)

    @Test
    fun create() {
        repeat(10) {
            val first = random.nextInt()
            val second = random.nextInt()

            val range = IntPair.create(first, second)

            assertEquals(IntPair.getFirst(range), first)
            assertEquals(IntPair.getSecond(range), second)
        }
    }
}