package com.pelmenstar.projktSens.weather.models;

import org.jetbrains.annotations.NotNull;

/**
 * Unit-value is union of some value and unit represented in long.
 * This class responsible for creating this union and helping to use it.
 */
public final class UnitValue {
    /**
     * Creates union of value and related unit.
     * Bit format: <br/>
     * - 64...32 bits # unit <br/>
     * - 32...0 bits # value <br/>
     */
    public static long of(float value, int unit) {
        if (!ValueUnit.isValidUnit(unit)) {
            throw new IllegalArgumentException("unit");
        }

        return ((long) (unit & 0xff) << 32) | ((long) Float.floatToIntBits(value) & 0xffffffffL);
    }

    /**
     * Gets unit of unit-value
     *
     * @implNote returns 64...32 bits of long
     */
    public static int getUnit(long unitValue) {
        return (int) (unitValue >> 32) & 0xff;
    }

    /**
     * Gets value of unit-value
     *
     * @implNote returns 32...0 bits of long
     */
    public static float getAbsoluteValue(long unitValue) {
        return Float.intBitsToFloat((int) unitValue);
    }

    /**
     * Returns value of unit-value in celsius unit.
     */
    public static float getCelsius(long unitValue) {
        return toCelsius(getAbsoluteValue(unitValue), getUnit(unitValue));
    }

    /**
     * Returns value of unit-value in mm of mercury unit.
     */
    public static float getMmOfMercury(long unitValue) {
        return toMmOfMercury(getAbsoluteValue(unitValue), getUnit(unitValue));
    }

    /**
     * Gets value of unit-value
     *
     * @param value       current value
     * @param currentUnit unit of value
     * @param unit        new unit
     * @throws IllegalArgumentException if currentUnit or unit are invalid
     */
    public static float getValue(float value, int currentUnit, int unit) {
        if (ValueUnit.isTemperatureUnit(currentUnit)) {
            if (!ValueUnit.isTemperatureUnit(unit)) {
                throw new IllegalArgumentException("unit");
            }

            return fromCelsius(toCelsius(value, currentUnit), unit);
        } else if (ValueUnit.isPressureUnit(currentUnit)) {
            if (!ValueUnit.isPressureUnit(unit)) {
                throw new IllegalArgumentException("unit");
            }

            return fromMmOfMercury(toMmOfMercury(value, currentUnit), unit);
        } else {
            if (!(unit == ValueUnit.HUMIDITY && currentUnit == ValueUnit.HUMIDITY)) {
                throw new IllegalArgumentException("unit");
            }

            return value;
        }
    }

    /**
     * Gets value from unit-value converted to newUnit
     *
     * @param unitValue unit-value
     * @param newUnit   unit of returned value
     * @throws IllegalArgumentException if newUnit or unit-value are invalid
     */
    public static float getValue(long unitValue, int newUnit) {
        return getValue(UnitValue.getAbsoluteValue(unitValue), UnitValue.getUnit(unitValue), newUnit);
    }

    /**
     * Returns unit-value with newUnit
     *
     * @param unitValue current unit-value
     * @param newUnit   new unit of new unit-value
     */
    public static long withUnit(long unitValue, int newUnit) {
        return of(getValue(unitValue, newUnit), newUnit);
    }

    /**
     * Returns whether unit-value is valid
     */
    public static boolean isValid(long unitValue) {
        int unit = getUnit(unitValue);
        float value = getAbsoluteValue(unitValue);

        return isValid(value, unit);
    }

    /**
     * Returns whether value is valid
     *
     * @param value some value
     * @param unit  unit of value
     */
    public static boolean isValid(float value, int unit) {
        switch (unit) {
            case ValueUnit.CELSIUS:
                return value >= -100 && value <= 100;
            case ValueUnit.FAHRENHEIT:
                return value >= -148 && value <= 212;
            case ValueUnit.KELVIN:
                return value >= 173 && value <= 373;

            case ValueUnit.HUMIDITY:
                return value >= 0 && value <= 100;

            case ValueUnit.MM_OF_MERCURY:
                return value >= 0 && value <= 1000;
            case ValueUnit.PASCAL:
                return value >= 0 && value <= 133322;

            default:
                return false;
        }
    }

    private static float toCelsius(float value, int unit) {
        switch (unit) {
            case ValueUnit.CELSIUS:
                return value;
            case ValueUnit.KELVIN:
                return value - 273;
            case ValueUnit.FAHRENHEIT:
                return (value - 32f) * 1.8f;
            default:
                throw new IllegalArgumentException("unit");
        }
    }

    private static float fromCelsius(float c, int unit) {
        switch (unit) {
            case ValueUnit.CELSIUS:
                return c;
            case ValueUnit.KELVIN:
                return c + 273f;
            case ValueUnit.FAHRENHEIT:
                return (c / 1.8f) + 32;
            default:
                throw new IllegalArgumentException("unit");
        }
    }

    private static float toMmOfMercury(float value, int unit) {
        switch (unit) {
            case ValueUnit.MM_OF_MERCURY:
                return value;
            case ValueUnit.PASCAL:
                return value / 133;
            default:
                throw new IllegalArgumentException("unit");
        }
    }

    private static float fromMmOfMercury(float value, int unit) {
        switch (unit) {
            case ValueUnit.MM_OF_MERCURY:
                return value;
            case ValueUnit.PASCAL:
                return value * 133;
            default:
                throw new IllegalArgumentException("unit");
        }
    }

    /**
     * Returns string representation of unit-value
     */
    @NotNull
    public static String toString(long unitValue) {
        return "{value=" + getAbsoluteValue(unitValue) + ", unit=" + ValueUnit.toString(getUnit(unitValue)) + '}';
    }

    /**
     * Appends string representation of unit-value to {@link StringBuilder}
     *
     * @param unitValue unit-value to append
     * @param sb        {@link StringBuilder} to append string representation
     */
    public static void appendString(long unitValue, @NotNull StringBuilder sb) {
        sb.append("{value=");
        sb.append(getAbsoluteValue(unitValue));
        sb.append(", unit=");
        sb.append(ValueUnit.toString(getUnit(unitValue)));
        sb.append('}');
    }
}
