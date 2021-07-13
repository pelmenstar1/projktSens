package com.pelmenstar.projktSens.weather.models.astro;

import com.pelmenstar.projktSens.shared.geo.Geolocation;
import com.pelmenstar.projktSens.shared.time.TimeInt;

import org.jetbrains.annotations.NotNull;

/**
 * Responsible for providing various information related with sun like sunrise, sunset time
 */
public interface SunInfoProvider {
    /**
     * Gets sunrise time. Sunrise time depends on day of year and location
     *
     * @throws IllegalArgumentException if dayOfYear not in [1; 366]
     */
    @TimeInt
    int getSunriseTime(int dayOfYear, @NotNull Geolocation location);

    /**
     * Gets sunset time. Sunset time depends on day of year and location
     *
     * @throws IllegalArgumentException if dayOfYear not in [1; 366]
     */
    @TimeInt
    int getSunsetTime(int dayOfYear, @NotNull Geolocation location);
}
