package com.pelmenstar.projktSens.shared;

import org.jetbrains.annotations.NotNull;

/**
 * A helper class which contains empty arrays of common types
 */
public final class EmptyArray {
    /**
     * Empty {@link Object} array
     */
    public static final @NotNull Object @NotNull [] OBJECT = new Object[0];

    /**
     * Empty {@link String} array
     */
    public static final @NotNull String  @NotNull [] STRING = new String[0];

    /**
     * Empty char array
     */
    public static final char @NotNull [] CHAR = new char[0];

    /**
     * Empty float array
     */
    public static final float @NotNull [] FLOAT = new float[0];

    /**
     * Empty long array
     */
    public static final long @NotNull [] LONG = new long[0];

    private EmptyArray() {}
}
