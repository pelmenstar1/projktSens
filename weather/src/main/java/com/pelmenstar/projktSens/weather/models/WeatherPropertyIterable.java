package com.pelmenstar.projktSens.weather.models;

import com.pelmenstar.projktSens.shared.time.ShortDateTimeLong;

public interface WeatherPropertyIterable {
    int size();

    int getUnits();

    @ShortDateTimeLong
    long getDateTime();

    float getTemperature();

    float getHumidity();

    float getPressure();

    boolean moveNext();
}
