package com.pelmenstar.projktSens.shared.time;

import com.pelmenstar.projktSens.shared.StringUtils;

import org.jetbrains.annotations.NotNull;

public final class ShortTime {
    public static final int NONE = TimeConstants.SECONDS_IN_DAY;

    private ShortTime() {
    }

    @TimeInt
    public static int create(int hour, int minute, int seconds) {
        if(hour < 0 || hour >= 24) {
            throw new IllegalArgumentException("hour");
        }

        if(minute < 0 || minute >= 60) {
            throw new IllegalArgumentException("minute");
        }

        if(seconds < 0 || seconds >= 60) {
            throw new IllegalArgumentException("seconds");
        }

        return hour * 3600 + minute * 60 + seconds;
    }

    public static boolean isValid(@TimeInt int time) {
        return time >= 0 && time < TimeConstants.SECONDS_IN_DAY;
    }

    @TimeInt
    public static int now() {
        return (int)(TimeUtils.currentLocalTimeMillis() % TimeConstants.MILLIS_IN_DAY) / 1000;
    }

    @NotNull
    public static String toString(@TimeInt int time) {
        char[] buffer = new char[8];
        writeToCharBuffer(time, buffer, 0);

        return new String(buffer, 0, 8);
    }

    public static void writeToCharBuffer(@TimeInt int time, @NotNull char[] buffer, int offset) {
        if(!isValid(time)) {
            throw new IllegalArgumentException("time");
        }

        int hour = time / 3600;
        time -= hour * 3600;
        int minute = time / 60;
        time -= minute * 60;

        StringUtils.writeTwoDigits(buffer, offset, hour);
        buffer[offset + 2] = ':';
        StringUtils.writeTwoDigits(buffer, offset + 3, minute);
        buffer[offset + 5] = ':';
        StringUtils.writeTwoDigits(buffer, offset + 6, time);
    }

    public static void append(@TimeInt int time, @NotNull StringBuilder sb) {
        if(!isValid(time)) {
            throw new IllegalArgumentException("time");
        }

        int hour = time / 3600;
        time -= hour * 3600;
        int minute = time / 60;
        time -= minute * 60;

        StringUtils.appendTwoDigits(hour, sb);
        sb.append(':');
        StringUtils.appendTwoDigits(minute, sb);
        sb.append(':');
        StringUtils.appendTwoDigits(time, sb);
    }
}
