package com.pelmenstar.projktSens.shared;

/**
 * Static class that responsible for creating pairs of int`s packed to long and extracting it back
 */
public final class IntPair {
    /**
     * Creates pair of two ints. Bit format: <br/>
     * - 64..32 bits | second <br/>
     * - 32..0 bits | first <br/>
     *
     * @param first first int
     * @param second second int
     */
    public static long of(int first, int second) {
        return ((long)second << 32) | ((long)first & 0xffffffffL);
    }

    /**
     * Extracts first int of pair
     */
    public static int getFirst(long pair) {
        return (int)(pair);
    }

    /**
     * Extracts second int of pair
     */
    public static int getSecond(long pair) {
        return (int)(pair >> 32);
    }

    public static long withFirst(long pair, int value) {
        return (pair & 0xffffffff00000000L) | ((long)value & 0xffffffffL);
    }

    public static long withSecond(long pair, int value) {
        return (pair & 0x00000000ffffffffL) | ((long)value << 32);
    }
}
