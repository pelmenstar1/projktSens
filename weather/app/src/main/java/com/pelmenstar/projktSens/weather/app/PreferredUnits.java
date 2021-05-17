package com.pelmenstar.projktSens.weather.app;

import com.pelmenstar.projktSens.weather.models.ValueUnitsPacked;

/**
 * Contains user-preferred units of temperature, humidity and so on
 */
public final class PreferredUnits {
    private static volatile int units = ValueUnitsPacked.CELSIUS_MM_OF_MERCURY;

    /**
     * Gets user-preferred units
     */
    public static int getUnits() {
        return units;
    }

    /**
     * Sets user-preferred units
     *
     * @throws IllegalArgumentException if given packed units is invalid
     */
    public static void setUnits(int units) {
        if(!ValueUnitsPacked.isValid(units)) {
            throw new IllegalArgumentException("units");
        }

        PreferredUnits.units = units;
    }
}
