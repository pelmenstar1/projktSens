package com.pelmenstar.projktSens.shared.time;

import com.pelmenstar.projktSens.shared.MyMath;
import com.pelmenstar.projktSens.shared.StringUtils;

import org.jetbrains.annotations.NotNull;

/**
 * Abstract class that responsible for formatting dates in pretty view.
 */
public abstract class PrettyDateFormatter {
    /**
     * Returns approximate count of chars needed to format datetime
     * May return exact or less count of chart
     * (Can return less in case of the date is today or yesterday)
     */
    public final int approximatePrettyDateTimeLength(@ShortDateTimeLong long dateTime) {
        return approximatePrettyDateLength(ShortDateTime.getDate(dateTime)) + 6;
    }

    /**
     * Returns approximate count of chars needed to format date
     * May return exact or less count of chart
     * (Can return less in case of the date is today or yesterday)
     */
    public final int approximatePrettyDateLength(@ShortDateInt int date) {
        int month = ShortDate.getMonth(date);
        int day = ShortDate.getDayOfMonth(date);
        int monthStrLength = getMonthWithDayString(month).length();
        int dayStrLength = MyMath.decimalDigitCount(day);

        return dayStrLength + monthStrLength + /* year */ 4 + /* whitespaces */ 2;
    }

    /**
     * Appends date-time to specified {@link StringBuilder}.
     *
     * @param dateTime date-time long.
     * @param sb       {@link StringBuilder} to append date-time.
     */
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

    /**
     * Appends date to specified {@link StringBuilder}.
     *
     * @param date date int.
     * @param sb   {@link StringBuilder} to append date.
     */
    public final void appendPrettyDate(@ShortDateInt int date, @NotNull StringBuilder sb) {
        int nowEpochDay = ShortDate.nowEpochDay();

        switch (ShortDate.toEpochDay(date) - nowEpochDay) {
            case 0: {
                sb.append(getTodayString());
                return;
            }

            case -1: {
                sb.append(getYesterdayString());
                return;
            }
        }

        int year = ShortDate.getYear(date);
        int month = ShortDate.getMonth(date);
        int day = ShortDate.getDayOfMonth(date);

        sb.append(day);
        sb.append(' ');
        sb.append(getMonthWithDayString(month));

        int now = ShortDate.ofEpochDay(nowEpochDay);
        if (ShortDate.getYear(now) != year) {
            sb.append(' ');
            StringUtils.appendFourDigits(year, sb);
        }
    }

    /**
     * Returns pretty view of specified date, represented in {@link String}.
     *
     * @param date date int.
     * @implNote internally it allocates instance of {@link StringBuilder}.
     */
    @NotNull
    public final String prettyFormat(@ShortDateInt int date) {
        StringBuilder builder = new StringBuilder(approximatePrettyDateLength(date));

        appendPrettyDate(date, builder);

        return builder.toString();
    }

    /**
     * Returns pretty view of year and month, represented in {@link String}.
     *
     * @param year  year, in range of [0; 9999].
     * @param month month, in range of [1; 12].
     */
    @NotNull
    public final String prettyFormat(int year, int month) {
        String monthString = getMonthString(month);
        int monthStrLength = monthString.length();

        int bufferLength = monthStrLength + /* whitespace */ 1 + /* year */ +4;
        char[] buffer = new char[bufferLength];
        monthString.getChars(0, monthStrLength, buffer, 0);
        buffer[monthStrLength] = ' ';
        StringUtils.writeFourDigits(buffer, monthStrLength + 1, year);

        return new String(buffer, 0, bufferLength);
    }

    /**
     * Returns {@link String} that represents today definition.
     */
    @NotNull
    protected abstract String getTodayString();

    /**
     * Returns {@link String} that represents yesterday definition.
     */
    @NotNull
    protected abstract String getYesterdayString();

    /**
     * Returns {@link String} that represents specified month.
     *
     * @param month month, in range [1; 12]
     */
    @NotNull
    protected abstract String getMonthString(int month);

    /**
     * Returns {@link String} that represents specified month which stays after day.
     *
     * @param month month, in range [1; 12]
     */
    @NotNull
    protected abstract String getMonthWithDayString(int month);
}
