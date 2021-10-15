package com.pelmenstar.projktSens.shared.android.charts;

import com.pelmenstar.projktSens.chartLite.formatter.ValueFormatter;
import com.pelmenstar.projktSens.shared.time.ShortDate;

import org.jetbrains.annotations.NotNull;

public final class DateChartFormatter extends ValueFormatter {
    @NotNull
    public static final DateChartFormatter INSTANCE = new DateChartFormatter();

    private final char @NotNull [] textCache = new char[10];

    private DateChartFormatter() {
        super(true);
    }

    @Override
    public char @NotNull [] formatToCharArray(float value) {
        char[] text = textCache;

        int epochDay = (int) value;
        ShortDate.writeToCharBuffer(ShortDate.ofEpochDay(epochDay), text, 0);

        return text;
    }
}
