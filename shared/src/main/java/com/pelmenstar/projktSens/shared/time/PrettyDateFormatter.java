package com.pelmenstar.projktSens.shared.time;

import com.pelmenstar.projktSens.shared.StringUtils;

import org.jetbrains.annotations.NotNull;

public abstract class PrettyDateFormatter {
    public final void appendPrettyDateTime(@ShortDateTimeLong long dateTime, @NotNull StringBuilder sb) {
        appendPrettyDate(ShortDateTime.getDate(dateTime), sb);
        sb.append(' ');

        int time = ShortDateTime.getTime(dateTime);
        int hour = time / 3600;
        time -= hour * 3600;
        int minute = time / 60;

        StringUtils.appendTwoDigits(hour, sb);
        sb.append(':');
        StringUtils.appendTwoDigits(minute, sb);
    }

    public final void appendPrettyDate(@ShortDateInt int date, @NotNull StringBuilder sb) {
        boolean appendDate = true;
        long nowEpochDay = ShortDate.nowEpochDay();

        switch ((int)(ShortDate.toEpochDay(date) - nowEpochDay)) {
            case 0: {
                sb.append(getTodayString());
                appendDate = false;

                break;
            }

            case -1: {
                sb.append(getYesterdayString());
                appendDate = false;

                break;
            }
        }

        if(appendDate) {
            int year = ShortDate.getYear(date);
            int month = ShortDate.getMonth(date);
            int day = ShortDate.getDayOfMonth(date);

            sb.append(day);
            sb.append(' ');
            sb.append(getMonthWithDayString(month));

            int now = ShortDate.ofEpochDay(nowEpochDay);
            if(ShortDate.getYear(now) != year) {
                sb.append(' ');
                StringUtils.appendFourDigits(year, sb);
            }
        }
    }

    public final void appendDate(int year, int month, @NotNull StringBuilder sb) {
        sb.append(getMonthString(month));
        sb.append(' ');
        StringUtils.appendFourDigits(year, sb);
    }

    @NotNull
    public final String prettyFormat(@ShortDateInt int date) {
        StringBuilder builder = new StringBuilder(32);

        appendPrettyDate(date, builder);

        return builder.toString();
    }

    @NotNull
    public final String prettyFormat(int year, int month) {
        StringBuilder builder = new StringBuilder(32);

        appendDate(year, month, builder);

        return builder.toString();
    }

    @NotNull
    protected abstract String getTodayString();

    @NotNull
    protected abstract String getYesterdayString();

    @NotNull
    protected abstract String getMonthString(int month);

    @NotNull
    protected abstract String getMonthWithDayString(int month);
}
