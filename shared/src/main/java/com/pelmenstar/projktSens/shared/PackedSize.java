package com.pelmenstar.projktSens.shared;

public final class PackedSize {
    public static long create(int width, int height) {
        return IntPair.of(width, height);
    }

    public static int getWidth(long size) {
        return IntPair.getFirst(size);
    }

    public static int getHeight(long size) {
        return IntPair.getSecond(size);
    }
}
