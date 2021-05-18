package com.pelmenstar.projktSens.shared.time;

import com.pelmenstar.projktSens.shared.StringUtils;

import org.jetbrains.annotations.NotNull;

/**
 * Static class that responsible for creating and manipulating seconds of day
 */
public final class ShortTime {
    /**
     * Specifies that variable stores no time
     */
    public static final int NONE = TimeConstants.SECONDS_IN_DAY;

    private ShortTime() {
    }

    /**
     * Creates seconds of day using hour, minute, second
     * @param hour hour, in range of [0; 24)
     * @param minute minute, in range of [0; 60)
     * @param second second, in range of [0; 60)
     *
     * @throws IllegalArgumentException if hour, minute or second are invalid
     */
    @TimeInt
    public static int create(int hour, int minute, int second) {
        if(hour < 0 || hour >= 24) {
            throw new IllegalArgumentException("hour");
        }

        if(minute < 0 || minute >= 60) {
            throw new IllegalArgumentException("minute");
        }

        if(second < 0 || second >= 60) {
            throw new IllegalArgumentException("seconds");
        }

        return hour * 3600 + minute * 60 + second;
    }

    /**
     * Determines whether seconds of day is valid
     */
    public static boolean isValid(@TimeInt int time) {
        return time >= 0 && time < TimeConstants.SECONDS_IN_DAY;
    }

    /**
     * Returns now time in seconds of day.
     */
    @TimeInt
    public static int now() {
        return (int)(TimeUtils.currentLocalTimeMillis() % TimeConstants.MILLIS_IN_DAY) / 1000;
    }

    /**
     * Gets string representation of time-int in format 'HH:MM:SS'
     *
     * @throws IllegalArgumentException if time isn't invalid
     */
    @NotNull
    public static String toString(@TimeInt int time) {
        char[] buffer = new char[8];
        writeToCharBuffer(time, buffer, 0);

        return new String(buffer, 0, 8);
    }

    /**
     * Writes string representation of time to char buffer starting from specified offset
     *
     * @throws IllegalArgumentException if time is invalid
     */
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

    /**
     * Appends string representation of time to particular {@link StringBuilder}
     *
     * @throws IllegalArgumentException if time is not valid
     */
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
