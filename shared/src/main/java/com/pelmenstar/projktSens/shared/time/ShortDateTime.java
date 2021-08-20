package com.pelmenstar.projktSens.shared.time;

import org.jetbrains.annotations.NotNull;

import java.util.Random;

/**
 * Static class that responsible for creating and manipulating union of date-int and time.
 */
public final class ShortDateTime {
    /**
     * Specifies that datetime-long is invalid
     */
    public static final long NONE = 0;

    private ShortDateTime() {
    }

    /**
     * Creates datetime-long using specified date-int and time
     *
     * @param date valid date-int
     * @param time seconds of day, in range of [0; 86400)
     * @throws IllegalArgumentException if date or time are invalid
     */
    @ShortDateTimeLong
    public static long of(@ShortDateInt int date, @TimeInt int time) {
        if (!ShortDate.isValid(date)) {
            throw new IllegalArgumentException("date");
        }

        if (!ShortTime.isValid(time)) {
            throw new IllegalArgumentException("time");
        }

        return ofInternal(date, time);
    }

    /**
     * Creates datetime-long using year, month, day of month and time.
     *
     * @param year  year, in range of [0; 9999]
     * @param month month, in range of [1; 12]
     * @param day   day of month, in range of [1; *days in month*]
     * @param time  seconds of day, in range of [0; 86400)
     * @throws IllegalArgumentException if year, month, day of month or time are invalid
     */
    @ShortDateTimeLong
    public static long of(int year, int month, int day, @TimeInt int time) {
        if (!ShortTime.isValid(time)) {
            throw new IllegalArgumentException("time");
        }

        return ofInternal(ShortDate.of(year, month, day), time);
    }

    private static long ofInternal(@ShortDateInt int date, @TimeInt int time) {
        return ((long) (date & 0x7FFFFF) << 17) | ((long) time & 0x1FFFF);
    }

    /**
     * Creates datetime-long from date, time is set to 0 (start of day)
     *
     * @param date valid date-int
     * @throws IllegalArgumentException if date is invalid
     */
    @ShortDateTimeLong
    public static long startOfDay(@ShortDateInt int date) {
        if (!ShortDate.isValid(date)) {
            throw new IllegalArgumentException("date");
        }

        return ofInternal(date, 0);
    }

    /**
     * Creates datetime-long from date, time is set to 86399 (end of day)
     *
     * @param date valid date-int
     * @throws IllegalArgumentException if date is invalid
     */
    @ShortDateTimeLong
    public static long endOfDay(@ShortDateInt int date) {
        if (!ShortDate.isValid(date)) {
            throw new IllegalArgumentException("date");
        }

        return ofInternal(date, TimeConstants.SECONDS_IN_DAY - 1);
    }

    /**
     * Returns now datetime-long in current time-zone
     */
    @ShortDateTimeLong
    public static long now() {
        return ofEpochSecond(TimeUtils.currentLocalTimeMillis() / 1000);
    }

    /**
     * Determines whether particular datetime-long is valid
     */
    public static boolean isValid(@ShortDateTimeLong long dateTime) {
        // unused bits should be clear
        if ((dateTime & 0xFFFFFF0000000000L) != 0) {
            return false;
        }

        return ShortDate.isValid(getDate(dateTime)) && ShortTime.isValid(getTime(dateTime));
    }

    /**
     * Gets date-int of datetime-long
     */
    @ShortDateInt
    public static int getDate(@ShortDateTimeLong long dateTime) {
        return (int) (dateTime >> 17);
    }

    /**
     * Gets time of datetime-long
     */
    @TimeInt
    public static int getTime(@ShortDateTimeLong long dateTime) {
        return (int) dateTime & 0x1FFFF;
    }

    /**
     * Converts epoch seconds to datetime-long
     *
     * @throws IllegalArgumentException if epochSeconds is less than 0
     */
    @ShortDateTimeLong
    public static long ofEpochSecond(@EpochSecondsLong long epochSecond) {
        if (epochSecond < 0) {
            throw new IllegalArgumentException("epochSecond");
        }

        int epochDay = (int) (epochSecond / TimeConstants.SECONDS_IN_DAY);
        int secsOfDay = (int) (epochSecond - epochDay * TimeConstants.SECONDS_IN_DAY);

        return ofInternal(ShortDate.ofEpochDay(epochDay), secsOfDay);
    }

    public static long hourAbsDifference(@ShortDateTimeLong long a, @ShortDateTimeLong long b) {
        long aEpoch = toEpochSecond(a);
        long bEpoch = toEpochSecond(b);
        long diff = Math.abs(aEpoch - bEpoch);

        return diff / 3600;
    }

    @ShortDateTimeLong
    public static long absDifference(@ShortDateTimeLong long a, @ShortDateTimeLong long b) {
        long aEpoch = toEpochSecond(a);
        long bEpoch = toEpochSecond(b);

        return ofEpochSecond(Math.abs(aEpoch - bEpoch));
    }

    /**
     * Adds specified seconds to particular datetime-long and returns the last one.
     *
     * @throws IllegalArgumentException If datetime-long is invalid. Also if seconds is negative and is less than epoch seconds
     *                                  of datetime-long. It can happen in very rare cases, so that is not reason to worry about.
     */
    @ShortDateTimeLong
    public static long plusSeconds(@ShortDateTimeLong long dateTime, long seconds) {
        return ofEpochSecond(toEpochSecond(dateTime) + seconds);
    }

    /**
     * Converts particular datetime-long to epoch seconds
     *
     * @throws IllegalArgumentException if datetime-long is invalid
     */
    @EpochSecondsLong
    public static long toEpochSecond(@ShortDateTimeLong long dateTime) {
        int time = getTime(dateTime);
        // validate only time because date will be checked in ShortDate.toEpochDay
        if (!ShortTime.isValid(time)) {
            throw new IllegalArgumentException("dateTime");
        }

        return (long) ShortDate.toEpochDay(getDate(dateTime)) * TimeConstants.SECONDS_IN_DAY + time;
    }

    @EpochSecondsLong
    public static long startOfDayToEpochSecond(@ShortDateInt int date) {
        return (long) ShortDate.toEpochDay(date) * TimeConstants.SECONDS_IN_DAY;
    }

    @EpochSecondsLong
    public static long endOfDayToEpochSecond(@ShortDateInt int date) {
        final int endOfDay = TimeConstants.SECONDS_IN_DAY - 1;
        return (long) ShortDate.toEpochDay(date) * TimeConstants.SECONDS_IN_DAY + endOfDay;
    }


    /**
     * Returns random datetime-long.
     *
     * @param source takes values from this instance of {@link Random}
     */
    @ShortDateTimeLong
    public static long random(@NotNull Random source) {
        int date = ShortDate.random(source);
        int time = source.nextInt(TimeConstants.SECONDS_IN_DAY);

        return ofInternal(date, time);
    }

    /**
     * Gets string representation of particular datetime-long
     *
     * @throws IllegalArgumentException if datetime-long is not valid
     */
    @NotNull
    public static String toString(@ShortDateTimeLong long dateTime) {
        char[] buffer = new char[20];
        writeToString(dateTime, buffer, 0);

        return new String(buffer, 0, 20);
    }

    /**
     * Writes datetime-long to specified char buffer starting from particular offset
     *
     * @throws IllegalArgumentException  if datetime-long isn't valid
     * @throws IndexOutOfBoundsException if offset is less than 0 or greater than {@code buffer.length - 20}.
     */
    public static void writeToString(@ShortDateTimeLong long dateTime, char @NotNull [] buffer, int offset) {
        if (!isValid(dateTime)) {
            throw new IllegalArgumentException("dateTime");
        }

        ShortDate.writeToCharBuffer(getDate(dateTime), buffer, offset);
        buffer[offset + 10] = ' ';
        ShortTime.writeToCharBuffer(getTime(dateTime), buffer, offset + 11);
    }

    /**
     * Appends string representation of datetime-long to {@link StringBuilder}
     *
     * @throws IllegalArgumentException if datetime-long is invalid
     */
    public static void append(@ShortDateTimeLong long dateTime, @NotNull StringBuilder sb) {
        if (!isValid(dateTime)) {
            throw new IllegalArgumentException("dateTime");
        }

        ShortDate.append(getDate(dateTime), sb);
        sb.append(' ');
        ShortTime.append(getTime(dateTime), sb);
    }
}
