package com.pelmenstar.projktSens.shared;

import org.jetbrains.annotations.NotNull;

import java.util.Random;

public final class RandomUtils {
    private RandomUtils() {}

    public static int nextInt(@NotNull Random random, int min, int max) {
        return min + random.nextInt(Math.abs(max - min));
    }

    public static float nextFloat(@NotNull Random random, float min, float max) {
        return min + random.nextFloat() * Math.abs(max - min);
    }
}
