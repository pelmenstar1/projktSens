package com.pelmenstar.projktSens.shared;

public final class IntPair {
    public static long of(int first, int second) {
        return ((long)second << 32) | ((long)first & 0xffffffffL);
    }

    public static int getFirst(long pair) {
        return (int)(pair);
    }

    public static int getSecond(long pair) {
        return (int)(pair >> 32);
    }

}
