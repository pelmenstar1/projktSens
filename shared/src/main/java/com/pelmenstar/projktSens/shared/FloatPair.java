package com.pelmenstar.projktSens.shared;

public final class FloatPair {
    public static long of(float first, float second) {
        return IntPair.of(Float.floatToIntBits(first), Float.floatToIntBits(second));
    }

    public static float getFirst(long pair) {
        return Float.intBitsToFloat(IntPair.getFirst(pair));
    }

    public static float getSecond(long pair) {
        return Float.intBitsToFloat((IntPair.getSecond(pair)));
    }
}
