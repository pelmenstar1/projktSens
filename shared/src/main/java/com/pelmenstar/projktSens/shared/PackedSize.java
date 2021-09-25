package com.pelmenstar.projktSens.shared;

/**
 * Static class that responsible for packing integer size and extracting width & height back.
 */
public final class PackedSize {
    /**
     * Creates integer size, packed in long
     */
    public static long create(int width, int height) {
        return IntPair.create(width, height);
    }

    /**
     * Extracts width from size
     */
    public static int getWidth(long size) {
        return IntPair.getFirst(size);
    }

    /**
     * Extracts width from size
     */
    public static int getHeight(long size) {
        return IntPair.getSecond(size);
    }
}
