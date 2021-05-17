package com.pelmenstar.projktSens.shared;

import org.jetbrains.annotations.NotNull;

/**
 * A helper class which contains empty arrays of common types
 */
public final class EmptyArray {
    /**
     * Empty {@link Object} array
     */
    @NotNull
    public static final Object[] OBJECT = new Object[0];

    /**
     * Empty {@link String} array
     */
    @NotNull
    public static final String[] STRING = new String[0];

    /**
     * Empty char array
     */
    @NotNull
    public static final char[] CHAR = new char[0];

    /**
     * Empty float array
     */
    @NotNull
    public static final float[] FLOAT = new float[0];

    /**
     * Empty long array
     */
    @NotNull
    public static final long[] LONG = new long[0];

    private EmptyArray() {}
}
