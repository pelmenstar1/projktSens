package com.pelmenstar.projktSens.weather.app.formatters;

import android.content.res.Resources;

import com.pelmenstar.projktSens.shared.time.PrettyDateFormatter;
import com.pelmenstar.projktSens.weather.app.R;

import org.jetbrains.annotations.NotNull;

public final class ResourcesPrettyDateFormatter extends PrettyDateFormatter {
    private final String todayString;
    private final String yesterdayString;
    private final String[] monthsWithDay;
    private final String[] months;

    public ResourcesPrettyDateFormatter(@NotNull Resources resources) {
        todayString = resources.getString(R.string.today);
        yesterdayString = resources.getString(R.string.yesterday);
        monthsWithDay = resources.getStringArray(R.array.monthsWithDay);
        months = resources.getStringArray(R.array.months);
    }

    @NotNull
    @Override
    protected String getTodayString() {
        return todayString;
    }

    @NotNull
    @Override
    protected String getYesterdayString() {
        return yesterdayString;
    }

    @NotNull
    @Override
    protected String getMonthString(int month) {
        return months[month - 1];
    }

    @NotNull
    @Override
    protected String getMonthWithDayString(int month) {
        return monthsWithDay[month - 1];
    }
}
