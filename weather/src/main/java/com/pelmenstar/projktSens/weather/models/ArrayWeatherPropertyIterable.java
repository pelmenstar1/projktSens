package com.pelmenstar.projktSens.weather.models;

import org.jetbrains.annotations.NotNull;

public final class ArrayWeatherPropertyIterable implements WeatherPropertyIterable {
    private final WeatherInfo[] values;
    private int index;
    private WeatherInfo current;

    public ArrayWeatherPropertyIterable(@NotNull WeatherInfo @NotNull [] values) {
        this.values = values;
    }

    @Override
    public int size() {
        return values.length;
    }

    @Override
    public int getUnits() {
        return current.units;
    }

    @Override
    public long getDateTime() {
        return current.dateTime;
    }

    @Override
    public float getTemperature() {
        return current.temperature;
    }

    @Override
    public float getHumidity() {
        return current.humidity;
    }

    @Override
    public float getPressure() {
        return current.pressure;
    }

    @Override
    public boolean moveNext() {
        if (index < values.length) {
            current = values[index++];

            return true;
        } else {
            return false;
        }
    }
}
