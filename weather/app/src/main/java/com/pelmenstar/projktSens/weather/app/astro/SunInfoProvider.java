package com.pelmenstar.projktSens.weather.app.astro;

import com.pelmenstar.projktSens.shared.geo.Geolocation;
import com.pelmenstar.projktSens.shared.time.TimeInt;

import org.jetbrains.annotations.NotNull;

/**
 * Responsible for providing various information related with sun like sunrise, sunset time
 */
public interface SunInfoProvider {
    long getSunriseSunset(int dayOfYear, @NotNull Geolocation location);
}
