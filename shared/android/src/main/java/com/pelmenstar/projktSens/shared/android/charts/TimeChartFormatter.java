package com.pelmenstar.projktSens.shared.android.charts;

import com.pelmenstar.projktSens.chartLite.formatter.ValueFormatter;
import com.pelmenstar.projktSens.shared.StringUtils;
import com.pelmenstar.projktSens.shared.time.TimeConstants;

import org.jetbrains.annotations.NotNull;

public final class TimeChartFormatter extends ValueFormatter {
    private static final TimeChartFormatter NO_ROUNDING = new TimeChartFormatter(1);
    private static final TimeChartFormatter ROUND_TO_HOUR = new TimeChartFormatter(TimeConstants.SECONDS_IN_HOUR);

    private final char @NotNull [] textCache = new char[5];
    private final int granularity;

    private TimeChartFormatter(int granularity) {
        super(true);

        this.granularity = granularity;
        textCache[2] = ':';
    }

    @NotNull
    public static TimeChartFormatter withNoRounding() {
        return NO_ROUNDING;
    }

    @NotNull
    public static TimeChartFormatter withRoundingToHour() {
        return ROUND_TO_HOUR;
    }

    @NotNull
    public static TimeChartFormatter withRounding(int granularity) {
        return new TimeChartFormatter(granularity);
    }

    @Override
    public char @NotNull [] formatToCharArray(float value) {
        char[] text = textCache;

        int time = (int) value;
        time = (time / granularity) * granularity;

        int hour = time / 3600;
        int minute = (time - (hour * 3600)) / 60;

        StringUtils.writeTwoDigits(text, 0, hour);
        StringUtils.writeTwoDigits(text, 3, minute);

        return text;
    }
}
