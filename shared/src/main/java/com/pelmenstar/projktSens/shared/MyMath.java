package com.pelmenstar.projktSens.shared;

public final class MyMath {
    private static final float[] POW_10_POSITIVE = new float[] {
            1f,
            10f,
            100f,
            1000f,
            10000f,
            100000f,
            1000000f,
            10000000f,
            100000000f,
    };

    private static final float[] POW_10_NEGATIVE = new float[] {
            1f,
            1e-1f,
            1e-2f,
            1e-3f,
            1e-4f,
            1e-5f,
            1e-6f,
            1e-7f,
            1e-8f,
    };

    private MyMath() {
    }

    public static float pow10(int a) {
        if(a >= 0 && a < 8) {
            return POW_10_POSITIVE[a];
        } else if(a < 0 && a > -8) {
            return POW_10_NEGATIVE[-a];
        } else {
            return 100000000f;
        }
    }

    /**
     * Rounds particular number for 1 decimal place
     */
    public static float round(float number) {
        return (int) (number * 10f) / 10f;
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

    /**
     * Forces specified value in range represented by min and max
     */
    public static int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        } else if (value > max) {
            return max;
        }

        return value;
    }

    /**
     * Forces specified value in range represented by min and max
     */
    public static float clamp(float value, float min, float max) {
        if (value < min) {
            return min;
        } else if (value > max) {
            return max;
        }

        return value;
    }

    /**
     * Returns count of decimal digits in value
     */
    public static int decimalDigitCount(int value) {
        if (value < 10) {
            return 1;
        } else if (value < 100) {
            return 2;
        } else if (value < 1000) {
            return 3;
        } else if (value < 10000) {
            return 4;
        } else if (value < 100000) {
            return 5;
        } else if (value < 1000000) {
            return 6;
        } else if (value < 10000000) {
            return 7;
        } else if (value < 100000000) {
            return 8;
        } else if (value < 1000000000) {
            return 9;
        } else {
            return 10;
        }
    }

    public static int hashCodeLong(long value) {
        return (int)(value ^ (value >>> 32));
    }
}
