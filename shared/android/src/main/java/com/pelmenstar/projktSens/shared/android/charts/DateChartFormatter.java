package com.pelmenstar.projktSens.shared.android.charts;

import com.pelmenstar.projktSens.chartLite.formatter.ValueFormatter;
import com.pelmenstar.projktSens.shared.time.ShortDate;

import org.jetbrains.annotations.NotNull;

public final class DateChartFormatter implements ValueFormatter {
    @NotNull
    public static final DateChartFormatter INSTANCE = new DateChartFormatter();
    private final char[] textCache = new char[10];

    private DateChartFormatter() {}

    @NotNull
    @Override
    public String format(float value) {
        char[] text = textCache;

        int epochDay = (int)value;
        ShortDate.writeToCharBuffer(ShortDate.ofEpochDay(epochDay), text, 0);

        return new String(text, 0, 10);
    }
}
