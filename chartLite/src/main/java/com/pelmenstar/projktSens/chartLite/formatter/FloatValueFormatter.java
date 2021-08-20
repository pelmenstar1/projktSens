package com.pelmenstar.projktSens.chartLite.formatter;

import com.pelmenstar.projktSens.shared.MyMath;

import org.jetbrains.annotations.NotNull;

/**
 * {@link ValueFormatter} that just formatting float in guess what? float.
 * But previously rounds it for 1 decimal place.
 * If given value is literally integer,
 * it will threat input as integer (output will not be like 10.0 or 27.0, it will be like 10 or 27).
 * <br/> <br/>
 * This class is singleton. Can be accessed through {@link FloatValueFormatter#INSTANCE}
 */
public final class FloatValueFormatter implements ValueFormatter {
    @NotNull
    public static final FloatValueFormatter INSTANCE = new FloatValueFormatter();

    private FloatValueFormatter() {
    }

    @NotNull
    @Override
    public String format(float value) {
        int i = (int) value;
        if (i == value) {
            return Integer.toString(i);
        }

        return Float.toString(MyMath.round(value));
    }
}
