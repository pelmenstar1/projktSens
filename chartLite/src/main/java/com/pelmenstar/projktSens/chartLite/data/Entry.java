package com.pelmenstar.projktSens.chartLite.data;

import com.pelmenstar.projktSens.shared.FloatPair;

import org.jetbrains.annotations.NotNull;

/**
 * Responsible for packing X and Y coordinate to long and obtaining it back
 */
public final class Entry {
    private Entry() {
    }

    /**
     * Packs given x and y value to long.
     */
    public static long of(float x, float y) {
        return FloatPair.of(x, y);
    }

    /**
     * Returns X coordinate of entry
     */
    public static float getX(long e) {
        return FloatPair.getFirst(e);
    }

    /**
     * Returns X coordinate of entry
     */
    public static float getY(long e) {
        return FloatPair.getSecond(e);
    }

    /**
     * Converts given entry to readable {@link String}
     */
    @NotNull
    public static String toString(long e) {
        return "(" + getX(e) + ';' + getY(e) + ')';
    }
}
