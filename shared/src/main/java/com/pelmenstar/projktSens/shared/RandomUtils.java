package com.pelmenstar.projktSens.shared;

import org.jetbrains.annotations.NotNull;

import java.util.Random;

/**
 * Util class for {@link Random}
 */
public final class RandomUtils {
    private RandomUtils() {}

    /**
     * Generates random int in range of [min; max)
     * @param random source of values
     * @param min min value of random int
     * @param max max value of random int
     */
    public static int nextInt(@NotNull Random random, int min, int max) {
        return min + random.nextInt(Math.abs(max - min));
    }

    /**
     * Generates random float in range of [min; max)
     * @param random source of values
     * @param min min value of random float
     * @param max max value of random float
     */
    public static float nextFloat(@NotNull Random random, float min, float max) {
        return min + random.nextFloat() * Math.abs(max - min);
    }
}
