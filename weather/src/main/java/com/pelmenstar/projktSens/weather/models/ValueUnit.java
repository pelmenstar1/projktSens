package com.pelmenstar.projktSens.weather.models;

import org.jetbrains.annotations.NotNull;

/**
 * Contains all valid value units represented in int and helper method to use it
 */
public final class ValueUnit {
    /**
     * This unit is valid, but marks that some variable contains no value
     */
    public static final int NONE = -1;
    public static final int CELSIUS = 0;
    public static final int KELVIN = 1;
    public static final int FAHRENHEIT = 2;

    public static final int HUMIDITY = 3;

    public static final int MM_OF_MERCURY = 4;
    public static final int PASCAL = 5;

    /**
     * Valid temperature units
     */
    @NotNull
    public static final int[] TEMPERATURE_UNITS = new int[] { CELSIUS, KELVIN, FAHRENHEIT };

    /**
     * Valid pressure units
     */
    @NotNull
    public static final int[] PRESSURE_UNITS = new int[] { MM_OF_MERCURY, PASCAL };

    private ValueUnit() {}

    /**
     * Returns whether unit is temperature-related
     */
    public static boolean isTemperatureUnit(int unit) {
        return unit >= CELSIUS && unit <= FAHRENHEIT;
    }

    /**
     * Returns whether unit is pressure-related
     */
    public static boolean isPressureUnit(int unit) {
        return unit == MM_OF_MERCURY || unit == PASCAL;
    }

    /**
     * Returns whether unit is valid
     */
    public static boolean isValidUnit(int unit) {
        return unit >= CELSIUS && unit <= PASCAL;
    }

    /**
     * Returns mnemonic of unit
     *
     * @throws IllegalArgumentException if unit is invalid
     */
    @NotNull
    public static String toString(int unit) {
        switch (unit) {
            case CELSIUS: return "CELSIUS";
            case KELVIN: return "KELVIN";
            case FAHRENHEIT: return "FAHRENHEIT";

            case HUMIDITY: return "HUMIDITY";

            case MM_OF_MERCURY: return "MM_OF_MERCURY";
            case PASCAL: return "PASCAL";
            default: throw new IllegalArgumentException("unit");
        }
    }
}
