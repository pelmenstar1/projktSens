package com.pelmenstar.projktSens.shared.time;

import com.pelmenstar.projktSens.shared.MyMath;
import com.pelmenstar.projktSens.shared.StringUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;

import java.util.Random;

/**
 * Static class that responsible for creating, manipulating date-int.
 */
public final class ShortDate {
    /**
     * Specifies that date-int contains no value.
     */
    @ShortDateInt
    public static final int NONE = 0;

    /**
     * Max valid year of date-int
     */
    public static final int MAX_YEAR = 9999;

    private ShortDate() {}

    /**
     * Creates date-int using year, month and day of month
     * @param year year, in range of [0; 9999]
     * @param month month, in range of [1; 12]
     * @param dayOfMonth day in range of [1; *days in month*]
     */
    @ShortDateInt
    public static int of(int year, int month, int dayOfMonth) {
        if(!isValid(year, month, dayOfMonth)) {
            throw new IllegalArgumentException("illegal date");
        }

        return ofInternal(year, month, dayOfMonth);
    }

    @ShortDateInt
    @TestOnly
    public static int ofInternal(int year, int month, int dayOfMonth) {
        return (year << 16) | (month << 8) | dayOfMonth;
    }

    /**
     * Returns year of date-int, in range of [0; 9999]
     */
    public static int getYear(@ShortDateInt int date) {
        return (date >> 16) & 0xffff;
    }

    /**
     * Returns month of date-int, in range of [1; 12]
     */
    public static int getMonth(@ShortDateInt int date) {
        return (date >> 8) & 0xff;
    }

    /**
     * Returns day of month of date-int, in range of [1; *days in month*]
     */
    public static int getDayOfMonth(@ShortDateInt int date) {
        return date & 0xff;
    }

    /**
     * Returns day of year of date-int
     *
     * @throws IllegalArgumentException if date is invalid
     */
    public static int getDayOfYear(@ShortDateInt int date) {
        if(!isValid(date)) {
            throw new IllegalArgumentException("date");
        }

        return TimeUtils.getFirstDayOfMonth(getYear(date), getMonth(date)) + getDayOfMonth(date) - 1;
    }

    /**
     * Returns day of week of date-int
     *
     * @throws IllegalArgumentException if date is invalid
     */
    public static int getDayOfWeek(@ShortDateInt int date) {
        if(!isValid(date)) {
            throw new IllegalArgumentException("date");
        }

        return getDayOfWeekInternal(date);
    }

    private static int getDayOfWeekInternal(@ShortDateInt int date) {
        int dow0 = (int) MyMath.floorMod(toEpochDayInternal(date) + 3, 7);

        return dow0 + 1;
    }

    /**
     * Returns first day of current week of date-int.
     *
     * @throws IllegalArgumentException if date is invalid
     */
    @ShortDateInt
    public static int firstDayOfWeek(@ShortDateInt int date) {
        if(!isValid(date)) {
            throw new IllegalArgumentException("date");
        }

        return minusDays(date, getDayOfWeekInternal(date) - 1);
    }

    /**
     * Converts date-int to julian date.
     *
     * @throws IllegalArgumentException if date is invalid
     */
    public static long toJulianDate(@ShortDateInt int date) {
        int year = ShortDate.getYear(date);
        int month = ShortDate.getMonth(date);
        int day = ShortDate.getDayOfMonth(date);

        if(!isValid(year, month, day)) {
            throw new IllegalArgumentException("date");
        }

        long c;

        if (month > 2) {
            month -= 3;
        } else {
            month += 9;
            year--;
        }

        c = year / 100L;
        year -= 100L * c;

        return day + (c * 146097L) / 4 + (year * 1461L) / 4 + (month * 153L + 2) / 5 + 1721119L;
    }

    /**
     * Determines whether date-int is valid
     */
    public static boolean isValid(@ShortDateInt int date) {
       return isValid(getYear(date), getMonth(date), getDayOfMonth(date));
    }

    private static boolean isValid(int year, int month, int day) {
        return (year >= 0 && year <= MAX_YEAR) &&
                (month > 0 && month <= 12) &&
                (day > 0 && day <= TimeUtils.getDaysInMonth(year, month));
    }

    /**
     * Returns today date-int in current time-zone
     */
    @ShortDateInt
    public static int now() {
        return ofEpochDay(nowEpochDay());
    }

    /**
     * Returns today epoch day in current time-zone
     */
    public static long nowEpochDay() {
        return TimeUtils.currentLocalTimeMillis() / TimeConstants.MILLIS_IN_DAY;
    }

    /**
     * Returns today date-int and adds specified days.
     *
     * @throws IllegalArgumentException if expression {@code nowEpochDay() + days} is negative.
     * It can happen in very rare cases.
     */
    @ShortDateInt
    public static int nowAndPlusDays(int days) {
        return ofEpochDay(nowEpochDay() + (long)days);
    }

    /**
     * Returns today date-int and subtracts specified days.
     *
     * @throws IllegalArgumentException if expression {@code nowEpochDay() - days} is negative.
     * It can happen in very rare cases.
     */
    @ShortDateInt
    public static int nowAndMinusDays(int days) {
        return ofEpochDay(nowEpochDay() - (long)days);
    }

    /**
     * Adds specified days to date and returns the last.
     *
     * @throws IllegalArgumentException if days is negative and less than epoch day of date.
     * It can happen in very rare cases.
     */
    @ShortDateInt
    public static int plusDays(@ShortDateInt int date, int days) {
        if(days == 0) {
            return date;
        }

        return ofEpochDay(toEpochDay(date) + (long)days);
    }

    /**
     * Subtracts specified days to date and returns the last.
     *
     * @throws IllegalArgumentException if days is greater than epoch day of date.
     * It can happen in very rare cases.
     */
    @ShortDateInt
    public static int minusDays(@ShortDateInt int date, int days) {
        if(days == 0) {
            return date;
        }

        return ofEpochDay(toEpochDay(date) - (long)days);
    }

    /**
     * Returns difference in days between particular date-ints
     *
     * @throws IllegalArgumentException if startDate is greater than endDate.
     */
    @ShortDateInt
    public static int diffDays(@ShortDateInt int startDate, @ShortDateInt int endDate) {
        long startEpoch = toEpochDay(startDate);
        long endEpoch = toEpochDay(endDate);

        if(startEpoch > endEpoch) {
            throw new IllegalArgumentException("startDate > endDate");
        }

        return ofEpochDay(endEpoch - startEpoch);
    }

    /**
     * Converts epoch day to date-int
     *
     * @throws IllegalArgumentException if epochDay is less than 0
     */
    @ShortDateInt
    public static int ofEpochDay(long epochDay) {
        if(epochDay < 0) {
            throw new IllegalArgumentException("epochDay");
        }

        long zeroDay = epochDay + TimeConstants.DAYS_0000_TO_1970;
        zeroDay -= 60;

        long yearEst = (400 * zeroDay + 591) / TimeConstants.DAYS_PER_CYCLE;
        long doyEst = zeroDay - (365 * yearEst + yearEst / 4 - yearEst / 100 + yearEst / 400);

        if (doyEst < 0) {
            yearEst--;
            doyEst = zeroDay - (365 * yearEst + yearEst / 4 - yearEst / 100 + yearEst / 400);
        }

        int marchDoy0 = (int) doyEst;

        int marchMonth0 = (marchDoy0 * 5 + 2) / 153;
        int month = (marchMonth0 + 2) % 12 + 1;
        int dom = marchDoy0 - (marchMonth0 * 306 + 5) / 10 + 1;
        yearEst += marchMonth0 / 10;

        return ofInternal((int)yearEst, month, dom);
    }

    /**
     * Converts specified date-int to epoch days.
     *
     * @throws IllegalArgumentException if date is invalid
     */
    public static long toEpochDay(@ShortDateInt int date) {
        if(!isValid(date)) {
            throw new IllegalArgumentException("date");
        }

        return toEpochDayInternal(date);
    }

    private static long toEpochDayInternal(@ShortDateInt int date) {
        int year = getYear(date);
        int month = getMonth(date);
        int day = getDayOfMonth(date);

        long total = 0;
        total += 365 * year;
        total += (year + 3) / 4 - (year + 99) / 100 + (year + 399) / 400;

        total += ((367 * month - 362) / 12);
        total += day - 1;

        if (month > 2) {
            total--;
            if (!TimeUtils.isLeapYear(year)) {
                total--;
            }
        }

        return (total - TimeConstants.DAYS_0000_TO_1970);
    }

    /**
     * Returns random date-int
     * @param random takes values from this instance of {@link Random}
     */
    @ShortDateInt
    public static int random(@NotNull Random random) {
        int year = random.nextInt(9999);
        int month = random.nextInt(12) + 1;
        int day = random.nextInt(TimeUtils.getDaysInMonth(year, month)) + 1;

        return ofInternal(year, month, day);
    }

    /**
     * Returns string representation of date-int in format 'YYYY:MM:DD'
     *
     * @throws IllegalArgumentException if date is invalid
     */
    @NotNull
    public static String toString(@ShortDateInt int date) {
        char[] buffer = new char[10];

        writeToCharBuffer(date, buffer, 0);

        return new String(buffer, 0, 10);
    }

    /**
     * Writes date-int to char buffer at specified offset.
     *
     * @throws IllegalArgumentException if date is invalid
     * @throws IndexOutOfBoundsException if offset is less than 0 or greater than {@code buffer.length - 8}
     * @throws NullPointerException if buffer is null
     */
    public static void writeToCharBuffer(@ShortDateInt int date, @NotNull char[] buffer, int offset) {
        if(!isValid(date)) {
            throw new IllegalArgumentException("date");
        }

        StringUtils.writeFourDigits(buffer, offset, getYear(date));
        buffer[offset + 4] = '.';
        StringUtils.writeTwoDigits(buffer, offset + 5, getMonth(date));
        buffer[offset + 7] = '.';
        StringUtils.writeTwoDigits(buffer, offset + 8, getDayOfMonth(date));
    }

    /**
     * Appends string representation of date to specified {@link StringBuilder}.
     *
     * @throws IllegalArgumentException if date is invalid
     */
    public static void append(@ShortDateInt int date, @NotNull StringBuilder sb) {
        if(!isValid(date)) {
            throw new IllegalArgumentException("date");
        }

        StringUtils.appendFourDigits(getYear(date), sb);
        sb.append('.');
        StringUtils.appendTwoDigits(getMonth(date), sb);
        sb.append('.');
        StringUtils.appendTwoDigits(getDayOfMonth(date), sb);
    }
}
