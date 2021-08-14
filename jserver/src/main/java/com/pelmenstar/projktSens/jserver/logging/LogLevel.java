package com.pelmenstar.projktSens.jserver.logging;

public final class LogLevel {
    public static final int DEBUG = 0;
    public static final int INFO = 1;
    public static final int ERROR = 2;

    private static final char[] PREFIXES = new char[] {
            // yes it's looking strange, but it's total coincidence
            'D',
            'I',
            'E'
    };

    private LogLevel() {}

    public static char getPrefix(int level) {
        return PREFIXES[level];
    }
}
