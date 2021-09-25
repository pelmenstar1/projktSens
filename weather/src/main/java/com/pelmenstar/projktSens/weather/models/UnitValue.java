package com.pelmenstar.projktSens.weather.models;

import com.pelmenstar.projktSens.shared.IntPair;
import com.pelmenstar.projktSens.shared.serialization.ValidationException;

import org.jetbrains.annotations.NotNull;

/**
 * Unit-value is union of some value and unit represented in long.
 * This class responsible for creating this union and helping to use it.
 */
public final class UnitValue {
    private static final long[] UNIT_RANGES = new long[] {
            IntPair.create(-100, 100), // celsius
            IntPair.create(-148, 212), // fahrenheit
            IntPair.create(173, 373), // kelvin

            IntPair.create(0, 100), // humidity

            IntPair.create(0, 1000), // mm of mercury
            IntPair.create(0, 133322) // pascal
    };

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
     * @param currentUnit newUnit of value
     * @param newUnit        new newUnit
     * @throws IllegalArgumentException if currentUnit or newUnit are invalid
     */
    public static float getValue(float value, int currentUnit, int newUnit) {
        if (ValueUnit.isTemperatureUnit(currentUnit)) {
            if (!ValueUnit.isTemperatureUnit(newUnit)) {
                throw new IllegalArgumentException("newUnit");
            }

            return fromCelsius(toCelsius(value, currentUnit), newUnit);
        } else if (ValueUnit.isPressureUnit(currentUnit)) {
            if (!ValueUnit.isPressureUnit(newUnit)) {
                throw new IllegalArgumentException("newUnit");
            }

            return fromMmOfMercury(toMmOfMercury(value, currentUnit), newUnit);
        } else {
            if (!(newUnit == ValueUnit.HUMIDITY && currentUnit == ValueUnit.HUMIDITY)) {
                throw new IllegalArgumentException("newUnit");
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
        if(!ValueUnit.isValidUnit(unit)) {
            return false;
        }

        long range = UNIT_RANGES[unit];
        float min = IntPair.getFirst(range);
        float max = IntPair.getSecond(range);

        return value >= min && value <= max;
    }

    public static void ensureValid(float value, int unit, @NotNull String valueName) {
        if(!isValid(value, unit)) {
            throw ValidationException.invalidValue(valueName, value);
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
                return (c * (1 / 1.8f)) + 32;
            default:
                throw new IllegalArgumentException("unit");
        }
    }

    private static float toMmOfMercury(float value, int unit) {
        switch (unit) {
            case ValueUnit.MM_OF_MERCURY:
                return value;
            case ValueUnit.PASCAL:
                return value * (1f / 133f);
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
