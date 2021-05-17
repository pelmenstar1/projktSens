package com.pelmenstar.projktSens.shared.time;

import org.jetbrains.annotations.NotNull;

import java.util.Random;

public final class ShortDateTime {
    public static final long NONE = 0;

    private ShortDateTime() {}

    @ShortDateTimeLong
    public static long of(@ShortDateInt int date, @TimeInt int time) {
        if(!ShortDate.isValid(date)) {
            throw new IllegalArgumentException("date");
        }

        if(!ShortTime.isValid(time)) {
            throw new IllegalArgumentException("time");
        }

        return ofInternal(date, time);
    }

    @ShortDateTimeLong
    public static long of(int year, int month, int day, @TimeInt int time) {
        if(!ShortTime.isValid(time)) {
            throw new IllegalArgumentException("time");
        }

        return ofInternal(ShortDate.of(year, month, day), time);
    }

    private static long ofInternal(@ShortDateInt int date, @TimeInt int time) {
        return (((long)date) << 32) | ((long)time & 0xffffffffL);
    }

    @ShortDateTimeLong
    public static long startOfDay(@ShortDateInt int date) {
        if(!ShortDate.isValid(date)) {
            throw new IllegalArgumentException("date");
        }

        return (long)date << 32;
    }

    @ShortDateTimeLong
    public static long endOfDay(@ShortDateInt int date) {
        if(!ShortDate.isValid(date)) {
            throw new IllegalArgumentException("date");
        }

        return ofInternal(date, TimeConstants.SECONDS_IN_DAY - 1);
    }

    @ShortDateTimeLong
    public static long now() {
        return ofEpochSecond(TimeUtils.currentLocalTimeMillis() / 1000);
    }

    public static boolean isValid(@ShortDateTimeLong long dateTime) {
        return ShortDate.isValid(getDate(dateTime)) && ShortTime.isValid(getTime(dateTime));
    }

    @ShortDateInt
    public static int getDate(@ShortDateTimeLong long dateTime) {
        return (int)(dateTime >> 32);
    }

    @TimeInt
    public static int getTime(@ShortDateTimeLong long dateTime) {
        return (int)dateTime;
    }

    @ShortDateTimeLong
    public static long ofEpochSecond(@EpochSecondsLong long epochSecond) {
        if(epochSecond < 0) {
            throw new IllegalArgumentException("epochSecond");
        }

        long epochDay = epochSecond / TimeConstants.SECONDS_IN_DAY;
        int secsOfDay = (int)(epochSecond - epochDay * TimeConstants.SECONDS_IN_DAY);

        return ofInternal(ShortDate.ofEpochDay(epochDay), secsOfDay);
    }

    @ShortDateTimeLong
    public static long plusSeconds(@ShortDateTimeLong long dateTime, long seconds) {
        return ofEpochSecond(toEpochSecond(dateTime) + seconds);
    }

    @EpochSecondsLong
    public static long toEpochSecond(@ShortDateTimeLong long dateTime) {
        if(!isValid(dateTime)) {
            throw new IllegalArgumentException("dateTime");
        }

        long epochDay = ShortDate.toEpochDay(getDate(dateTime));

        return epochDay * TimeConstants.SECONDS_IN_DAY + getTime(dateTime);
    }

    @ShortDateTimeLong
    public static long random(@NotNull Random source) {
        int date = ShortDate.random(source);
        int time = source.nextInt(TimeConstants.SECONDS_IN_DAY);

        return ofInternal(date, time);
    }

    @NotNull
    public static String toString(@ShortDateTimeLong long dateTime) {
        char[] buffer = new char[20];
        writeToString(dateTime, buffer, 0);

        return new String(buffer, 0, 20);
    }

    public static void writeToString(@ShortDateTimeLong long dateTime, @NotNull char[] buffer, int offset) {
        if(!isValid(dateTime)) {
            throw new IllegalArgumentException("dateTime");
        }

        ShortDate.writeToCharBuffer(getDate(dateTime), buffer, offset);
        buffer[offset + 10] = ' ';
        ShortTime.writeToCharBuffer(getTime(dateTime), buffer, offset + 11);
    }

    public static void append(@ShortDateTimeLong long dateTime, @NotNull StringBuilder sb) {
        if(!isValid(dateTime)) {
            throw new IllegalArgumentException("dateTime");
        }

        ShortDate.append(getDate(dateTime), sb);
        sb.append(' ');
        ShortTime.append(getTime(dateTime), sb);
    }
}
