package com.pelmenstar.projktSens.chartLite.formatter;

import org.jetbrains.annotations.NotNull;

/**
 * Responsible for converting float to proper string representation with clarity for user
 */
public interface ValueFormatter {
    /**
     * Returns clarity string representation of specified value
     */
    @NotNull
    String format(float value);
}
