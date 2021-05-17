package com.pelmenstar.projktSens.shared;

public final class MyMath {
    private MyMath() {}

    public static float round(float number) {
        return (int)(number * 10f) / 10f;
    }

    /**
     * Does the same as {@link Math#floorMod(long, long)}.
     * {@link Math#floorMod(long, long)} can be used only from 24 API level (Current is 21)
     */
    public static long floorMod(long x, long y) {
        return x - floorDiv(x, y) * y;
    }

    /**
     * Does the same as {@link Math#floorDiv(long, long)}.
     * {@link Math#floorDiv(long, long)} can be used only from 24 API level (Current is 21)
     */
    public static long floorDiv(long x, long y) {
        long r = x / y;

        // if the signs are different and modulo not zero, round down
        if ((x ^ y) < 0 && (r * y != x)) {
            r--;
        }
        return r;
    }

    public static int clamp(int value, int min, int max) {
        if(value < min) {
            return min;
        } else if(value > max) {
            return max;
        }

        return value;
    }

    public static float clamp(float value, float min, float max) {
        if(value < min) {
            return min;
        } else if(value > max) {
            return max;
        }

        return value;
    }
}
