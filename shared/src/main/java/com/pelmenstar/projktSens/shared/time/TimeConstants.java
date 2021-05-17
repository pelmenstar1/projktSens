package com.pelmenstar.projktSens.shared.time;

public final class TimeConstants {
    public static final int MILLIS_IN_MINUTE = 60000;
    public static final int MILLIS_IN_HOUR = 60 * MILLIS_IN_MINUTE;
    public static final long MILLIS_IN_DAY = 24 * MILLIS_IN_HOUR;

    public static final int SECONDS_IN_HOUR = 60 * 60;
    public static final int SECONDS_IN_DAY = 24 * SECONDS_IN_HOUR;

    public static final int DAYS_PER_CYCLE = 146097;
    public static final long DAYS_0000_TO_1970 = (DAYS_PER_CYCLE * 5) - (30 * 365 + 7);

    private TimeConstants() {}
}
