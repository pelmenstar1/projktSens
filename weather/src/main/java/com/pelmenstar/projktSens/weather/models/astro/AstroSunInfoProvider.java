package com.pelmenstar.projktSens.weather.models.astro;

import com.pelmenstar.projktSens.shared.geo.Geolocation;
import com.pelmenstar.projktSens.shared.time.TimeConstants;
import com.pelmenstar.projktSens.shared.time.TimeInt;

import org.jetbrains.annotations.NotNull;

import java.util.TimeZone;

/**
 * Default astronomical implementation of {@link SunInfoProvider}
 */
public final class AstroSunInfoProvider implements SunInfoProvider {
    private static final float D2R = (float) Math.PI / 180f;
    private static final float R2D = 180f / (float) Math.PI;
    private static final float ZENITH = 90.8888f;
    private static final float HOURS_PER_DEGREE = 1f / 15f;

    @Override
    @TimeInt
    public int getSunriseTime(int dayOfYear, @NotNull Geolocation location) {
        return getSunriseSunsetInternal(dayOfYear, location, true);
    }

    @Override
    @TimeInt
    public int getSunsetTime(int dayOfYear, @NotNull Geolocation location) {
        return getSunriseSunsetInternal(dayOfYear, location, false);
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    @TimeInt
    public static int getSunriseSunsetInternal(
            int dayOfYear,
            @NotNull Geolocation location,
            boolean sunrise
    ) {
        if (dayOfYear <= 0 || dayOfYear > 366) {
            throw new IllegalArgumentException("dayOfYear");
        }

        float latRad = location.getLatitude() * D2R;
        float lnHour = location.getLongitude() * HOURS_PER_DEGREE;

        double latRadD = latRad;
        float t;

        if (sunrise) {
            t = (float) dayOfYear + (6.0f - lnHour) * 0.04166666666f;
        } else {
            t = (float) dayOfYear + (18.0f - lnHour) * 0.04166666666f;
        }

        float m = 0.9856f * t - 3.289f;

        float l = m + 1.916f * (float) Math.sin(m * D2R) + 0.02f * (float) Math.sin(m * D2R * 2f) + 282.634f;
        float lRad = l * D2R;
        double lRadD = lRad;

        if (l > 360.0f) {
            l -= 360.0f;
        } else if (l < 0.0f) {
            l += 360.0f;
        }

        float ra = R2D * (float) Math.atan(0.91764f * (float) Math.tan(lRadD));
        if (ra > 360.0f) {
            ra -= 360.0f;
        } else if (ra < 0.0f) {
            ra += 360.0f;
        }

        float lQ = (float) Math.floor(l / 90.0f) * 90.0f;
        float raQ = (float) Math.floor(ra / 90.0f) * 90.0f;

        ra += lQ - raQ;
        ra *= HOURS_PER_DEGREE;

        float sinDec = 0.39782f * (float) Math.sin(lRadD);
        float cosDec = (float) Math.cos(Math.asin(sinDec));
        float cosH = ((float) Math.cos(D2R * ZENITH) - sinDec * (float) Math.sin(latRadD)) / (cosDec * (float) Math.cos(latRadD));

        float h = R2D * (float) Math.acos(cosH);
        if (sunrise) {
            h = 360.0f - h;
        }

        h *= HOURS_PER_DEGREE;

        float hour = h + ra - 0.06571f * t - 6.622f;
        if (hour > 24.0f) {
            hour -= 24.0f;
        } else if (hour < 0.0f) {
            hour += 24.0f;
        }

        int utcTime = (int) ((hour - lnHour) * 3600.0f);
        int zoneOffset = TimeZone.getDefault().getOffset(System.currentTimeMillis()) / 1000;

        return normalizeTime(utcTime + zoneOffset);
    }

    private static int normalizeTime(int time) {
        if (time >= TimeConstants.SECONDS_IN_DAY) {
            return time - TimeConstants.SECONDS_IN_DAY;
        }

        return time;
    }
}
