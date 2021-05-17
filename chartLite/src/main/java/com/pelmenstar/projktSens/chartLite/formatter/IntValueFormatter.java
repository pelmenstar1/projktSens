package com.pelmenstar.projktSens.chartLite.formatter;

import org.jetbrains.annotations.NotNull;

/**
 * {@link ValueFormatter} that threats input float as integer.
 * <br/> <br/>
 * This class is singleton. Can be accessed through {@link IntValueFormatter#INSTANCE}
 */
public final class IntValueFormatter implements ValueFormatter {
    @NotNull
    public static final IntValueFormatter INSTANCE = new IntValueFormatter();

    private IntValueFormatter() {}

    @Override
    @NotNull
    public String format(float value) {
        return Integer.toString((int)value);
    }
}
