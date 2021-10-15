package com.pelmenstar.projktSens.shared;

import org.jetbrains.annotations.NotNull;

/**
 * A helper class which contains empty arrays of common types
 */
public final class EmptyArray {
    public static final @NotNull Object @NotNull [] OBJECT = new Object[0];

    public static final @NotNull String @NotNull [] STRING = new String[0];

    public static final char @NotNull [] CHAR = new char[0];

    public static final float @NotNull [] FLOAT = new float[0];

    public static final int @NotNull [] INT = new int[0];

    public static final long @NotNull [] LONG = new long[0];

    private EmptyArray() {
    }
}
