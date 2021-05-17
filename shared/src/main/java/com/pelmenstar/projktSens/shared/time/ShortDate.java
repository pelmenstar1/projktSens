package com.pelmenstar.projktSens.shared.time;

import com.pelmenstar.projktSens.shared.MyMath;
import com.pelmenstar.projktSens.shared.StringUtils;

import org.jetbrains.annotations.NotNull;

import java.util.Random;

public final class ShortDate {
    @ShortDateInt
    public static final int NONE = 0;

    public static final int MAX_YEAR = 9999;

    private ShortDate() {}

    @ShortDateInt
    public static int of(int year, int month, int dayOfMonth) {
        if(!isValid(year, month, dayOfMonth)) {
            throw new IllegalArgumentException("illegal date");
        }

        return ofInternal(year, month, dayOfMonth);
    }

    @ShortDateInt
    private static int ofInternal(int year, int month, int dayOfMonth) {
        return (year << 16) | (month << 8) | dayOfMonth;
    }

    public static int getYear(@ShortDateInt int date) {
        return (date >> 16) & 0xffff;
    }

    public static int getMonth(@ShortDateInt int date) {
        return (date >> 8) & 0xff;
    }

    public static int getDayOfMonth(@ShortDateInt int date) {
        return date & 0xff;
    }

    public static int getDayOfYear(@ShortDateInt int date) {
        if(!isValid(date)) {
            throw new IllegalArgumentException("date");
        }

        return TimeUtils.getFirstDayOfMonth(getYear(date), getMonth(date)) + getDayOfMonth(date) - 1;
    }

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

    @ShortDateInt
    public static int firstDayOfWeek(@ShortDateInt int date) {
        if(!isValid(date)) {
            throw new IllegalArgumentException("date");
        }

        return minusDays(date, getDayOfWeekInternal(date) - 1);
    }

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

    public static boolean isValid(@ShortDateInt int date) {
       return isValid(getYear(date), getMonth(date), getDayOfMonth(date));
    }

    private static boolean isValid(int year, int month, int day) {
        if(year < 0 || year > MAX_YEAR) {
            return false;
        }

        if(month < 0 || month > 12) {
            return false;
        }

        return day > 0 && day <= TimeUtils.getDaysInMonth(year, month);
    }

    @ShortDateInt
    public static int now() {
        return ofEpochDay(nowEpochDay());
    }

    public static long nowEpochDay() {
        return TimeUtils.currentLocalTimeMillis() / TimeConstants.MILLIS_IN_DAY;
    }

    @ShortDateInt
    public static int nowAndPlusDays(int days) {
        return ofEpochDay(nowEpochDay() + (long)days);
    }

    @ShortDateInt
    public static int nowAndMinusDays(int days) {
        return ofEpochDay(nowEpochDay() - (long)days);
    }

    @ShortDateInt
    public static int plusDays(@ShortDateInt int date, int days) {
        if(days == 0) {
            return date;
        }

        return ofEpochDay(toEpochDay(date) + (long)days);
    }

    @ShortDateInt
    public static int minusDays(@ShortDateInt int date, int days) {
        if(days == 0) {
            return date;
        }

        return ofEpochDay(toEpochDay(date) - (long)days);
    }

    @ShortDateInt
    public static int diffDays(@ShortDateInt int startDate, @ShortDateInt int endDate) {
        long startEpoch = toEpochDay(startDate);
        long endEpoch = toEpochDay(endDate);

        if(startEpoch > endEpoch) {
            throw new IllegalArgumentException("startDate > endDate");
        }

        return ofEpochDay(endEpoch - startEpoch);
    }

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

    @ShortDateInt
    public static int random(@NotNull Random random) {
        int year = random.nextInt(9999);
        int month = random.nextInt(12) + 1;
        int day = random.nextInt(TimeUtils.getDaysInMonth(year, month)) + 1;

        return ofInternal(year, month, day);
    }

    @NotNull
    public static String toString(@ShortDateInt int date) {
        char[] buffer = new char[10];

        writeToCharBuffer(date, buffer, 0);

        return new String(buffer, 0, 10);
    }

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
