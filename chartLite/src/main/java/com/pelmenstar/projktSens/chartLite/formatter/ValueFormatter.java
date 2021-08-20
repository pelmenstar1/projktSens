package com.pelmenstar.projktSens.chartLite.formatter;

import org.jetbrains.annotations.NotNull;

/**
 * Responsible for converting float to proper string representation for user
 */
public abstract class ValueFormatter {
    private final boolean supportsFormattingToCharArray;

    protected ValueFormatter(boolean supportsFormattingToCharArray) {
        this.supportsFormattingToCharArray = supportsFormattingToCharArray;
    }

    public final boolean supportsFormattingToCharArray() {
        return supportsFormattingToCharArray;
    }

    /**
     * Returns string representation of value
     * May not be implemented if {@link ValueFormatter#supportsFormattingToCharArray()} returns true
     * But if {@link ValueFormatter#supportsFormattingToCharArray()} returns false, it should be.
     */
    @NotNull
    public String formatToString(float value) {
        if(supportsFormattingToCharArray) {
            char[] buffer = formatToCharArray(value);
            if(buffer == null) {
                throw new RuntimeException(
                        "supportsFormattingToCharArray() returns true but formatToCharArray() returned null"
                );
            }

            return new String(buffer);
        } else {
            throw new RuntimeException("This operation is not supported");
        }
    }

    /**
     * Returns representation of value as char array.
     * By contract, it should return same reference each time it's called
     */
    public char @NotNull [] formatToCharArray(float value) {
        throw new RuntimeException("This operation is not supported");
    }
}
