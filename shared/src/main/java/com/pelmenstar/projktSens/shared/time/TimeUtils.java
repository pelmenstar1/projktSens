package com.pelmenstar.projktSens.shared.time;

import java.util.TimeZone;

/**
 * Represents a class that helps you to work with date and time
 */
public final class TimeUtils {
    private TimeUtils() {}

    // Pretty strange constant but, nevertheless, it's not randomized
    // If you look at days in month of a year, you may notice that minimum is 28, meanwhile, maximum is 31.
    // Delta is 3, which perfectly fit into 2 bits.
    // Than we can write days in month of a year but represented as delta from 28 (first is february, last is december):
    // 31   28   31   30   31   30   31   31   30   31   30   31
    // \/   \/   \/   \/   \/   \/   \/   \/   \/   \/   \/   \/
    // 3    0    3    2    3    2    3    3    2    3    2    3
    // ------------------------------------------------------------
    // Than if we convert these to binary and take it from end, convert it to hex, we get exactly this constant:
    // 11 00 11 10 11 10 11 11 10 11 10 11 = 0xEEFBB3
    private static final int daysInMonthBitTable = 0xEEFBB3;
    private static final short[] firstDayOfMonth = new short[] {
            1,
            32,
            60,
            91,
            121,
            152,
            182,
            213,
            244,
            274,
            305,
            335
    };

    private static final long currentZoneOffsetMillis = TimeZone.getDefault().getOffset(System.currentTimeMillis());

    /**
     * Gets count of days in specified month of specified year
     */
    public static int getDaysInMonth(int year, int month) {
        // if year is leap and month is february
        if (isLeapYear(year) && month == 2) {
            return 29;
        }

        // The nature of daysInMonthBitTable is described above.
        // There are described why expression below looks exactly like that.
        // month - 1: month is in range [1;12], but we need [0;11]
        // (month - 1) << 1: every offset is described in 2 bits, so we need to double our shift
        // (daysInMonthBitTable) >> ((month - 1) << 1): takes day offset of specified month, but
        //

        return 28 + ((daysInMonthBitTable >> ((month - 1) << 1)) & 0x3);
    }

    /**
     * Returns day of year that represents first day of specified month of specified year
     */
    public static int getFirstDayOfMonth(int year, int month) {
        int firstDay = firstDayOfMonth[month - 1];

        if(isLeapYear(year) && month > 2) {
            return firstDay + 1;
        }

        return firstDay;
    }

    /**
     * Determines whether specified year is leap.
     * Leap year is a year that divides by 4 and 400, but not 100
     * (1900 wasn't leap, but 2000 did, because it divides by 400)
     */
    public static boolean isLeapYear(int year) {
        return (year & 3) == 0 && (year % 100 != 0 || year % 400 == 0);
    }

    /**
     * Returns UNIX epoch in milliseconds shifted with current time zone
     */
    public static long currentLocalTimeMillis() {
        return System.currentTimeMillis() + currentZoneOffsetMillis;
    }
}
