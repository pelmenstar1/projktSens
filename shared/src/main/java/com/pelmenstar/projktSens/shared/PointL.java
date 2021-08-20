package com.pelmenstar.projktSens.shared;

/**
 * Static class that responsible for creating 2D points represented in long and extracting coordinates back
 */
public final class PointL {
    /**
     * Creates 2D point
     *
     * @param x x-axis value
     * @param y y-axis value
     */
    public static long of(float x, float y) {
        return FloatPair.of(x, y);
    }

    /**
     * Extracts x-axis value from specified point long
     */
    public static float getX(long p) {
        return FloatPair.getFirst(p);
    }

    /**
     * Extracts y-axis value from specified point long
     */
    public static float getY(long p) {
        return FloatPair.getSecond(p);
    }
}
