package com.pelmenstar.projktSens.shared;

/**
 * Static class that responsible for creating pairs of float`s packed to long and extracting it back
 */
public final class FloatPair {
    /**
     * Creates pair of two float`s. Bit format: <br/>
     * - 64..32 bits | second <br/>
     * - 32..0 bits | first <br/>
     *
     * @param first first float
     * @param second second float
     */
    public static long of(float first, float second) {
        return IntPair.of(Float.floatToIntBits(first), Float.floatToIntBits(second));
    }

    /**
     * Extracts first float of pair
     */
    public static float getFirst(long pair) {
        return Float.intBitsToFloat(IntPair.getFirst(pair));
    }

    /**
     * Extracts second float of pair
     */
    public static float getSecond(long pair) {
        return Float.intBitsToFloat((IntPair.getSecond(pair)));
    }

    public static long withFirst(long pair, float value) {
        return IntPair.withFirst(pair, Float.floatToIntBits(value));
    }

    public static long withSecond(long pair, float value) {
        return IntPair.withSecond(pair, Float.floatToIntBits(value));
    }
}
