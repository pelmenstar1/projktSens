package com.pelmenstar.projktSens.shared.android.charts;

import com.pelmenstar.projktSens.chartLite.formatter.ValueFormatter;
import com.pelmenstar.projktSens.shared.StringUtils;

import org.jetbrains.annotations.NotNull;

public final class TimeChartFormatter extends ValueFormatter {
    @NotNull
    public static final TimeChartFormatter INSTANCE = new TimeChartFormatter();

    private final char[] textCache = new char[5];

    private TimeChartFormatter() {
        super(true);

        textCache[2] = ':';
    }

    @Override
    public char @NotNull [] formatToCharArray(float value) {
        char[] text = textCache;

        int time = (int) value;
        int hour = time / 3600;
        int minute = (time - (hour * 3600)) / 60;

        StringUtils.writeTwoDigits(text, 0, hour);
        StringUtils.writeTwoDigits(text, 3, minute);

        return text;
    }
}
