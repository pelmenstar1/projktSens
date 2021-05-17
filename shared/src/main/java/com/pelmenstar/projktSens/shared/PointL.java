package com.pelmenstar.projktSens.shared;

public final class PointL {
    public static long of(float x, float y) {
        return FloatPair.of(x, y);
    }

    public static float getX(long p) {
        return FloatPair.getFirst(p);
    }

    public static float getY(long p) {
        return FloatPair.getSecond(p);
    }
}
