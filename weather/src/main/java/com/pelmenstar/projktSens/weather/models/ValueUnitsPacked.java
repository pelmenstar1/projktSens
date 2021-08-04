package com.pelmenstar.projktSens.weather.models;

import org.jetbrains.annotations.NotNull;

public final class ValueUnitsPacked {
    public static final int TYPE_TEMPERATURE = 8;
    public static final int TYPE_PRESSURE = 0;

    public static final int NONE = (ValueUnit.NONE & 0xff) << 8 | (ValueUnit.NONE & 0xff);
    public static final int CELSIUS_MM_OF_MERCURY = ValueUnit.CELSIUS << 8 | ValueUnit.MM_OF_MERCURY;

    public static int create(int tempUnit, int pressUnit) {
        if(!ValueUnit.isTemperatureUnit(tempUnit)) {
            throw new IllegalArgumentException("tempUnit");
        }

        if(!ValueUnit.isPressureUnit(pressUnit)) {
            throw new IllegalArgumentException("pressUnit");
        }

        return tempUnit << 8 | pressUnit;
    }

    public static int getTemperatureUnit(int units) {
        return getUnit(units, TYPE_TEMPERATURE);
    }

    public static int getPressureUnit(int units) {
        return getUnit(units, TYPE_PRESSURE);
    }

    public static int getUnit(int units, int type) {
        return (units >> type) & 0xff;
    }

    public static int withUnit(int units, int type, int value) {
        int eraser = ~(0xff << type);

        return (units & eraser) | (value << type);
    }

    public static void append(int units, @NotNull StringBuilder sb) {
        sb.append("{temp=");
        sb.append(ValueUnit.toString(getTemperatureUnit(units)));
        sb.append(", press=");
        sb.append(ValueUnit.toString(getPressureUnit(units)));
        sb.append('}');
    }

    public static boolean isValid(int units) {
        return ValueUnit.isTemperatureUnit(getTemperatureUnit(units)) && ValueUnit.isPressureUnit(getPressureUnit(units));
    }
}
