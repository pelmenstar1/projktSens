package com.pelmenstar.projktSens.shared;

public final class PackedSizeF {
    private PackedSizeF() {}

    public static long create(float width, float height) {
        return FloatPair.of(width, height);
    }

    public static float getWidth(long size) {
        return FloatPair.getFirst(size);
    }

    public static float getHeight(long size) {
        return FloatPair.getSecond(size);
    }
}
