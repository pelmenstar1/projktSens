package com.pelmenstar.projktSens.weather.app.astro;

import com.pelmenstar.projktSens.shared.FloatPair;
import com.pelmenstar.projktSens.shared.IntPair;
import com.pelmenstar.projktSens.shared.geo.Geolocation;
import com.pelmenstar.projktSens.shared.time.TimeConstants;
import com.pelmenstar.projktSens.shared.time.TimeInt;

import org.jetbrains.annotations.NotNull;

import java.util.TimeZone;

/**
 * Default astronomical implementation of {@link SunInfoProvider}
 */
public final class AstroSunInfoProvider implements SunInfoProvider {
    private static final int ZONE_OFFSET = TimeZone.getDefault().getOffset(System.currentTimeMillis()) / 1000;

    static {
        System.loadLibrary("weather-app");
    }

    @Override
    public long getSunriseSunset(int dayOfYear, @NotNull Geolocation location) {
        long range = nGetSunriseSunsetTimeRangeUtc(dayOfYear, packLocation(location));
        int zonedSunrise = IntPair.getFirst(range) + ZONE_OFFSET;
        int zonedSunset = IntPair.getSecond(range) + ZONE_OFFSET;

        return IntPair.create(zonedSunrise, zonedSunset);
    }

    private static long packLocation(@NotNull Geolocation location) {
        return FloatPair.create(location.getLatitude(), location.getLongitude());
    }

    public static native long nGetSunriseSunsetTimeRangeUtc(int dayOfYear, long location);
}
