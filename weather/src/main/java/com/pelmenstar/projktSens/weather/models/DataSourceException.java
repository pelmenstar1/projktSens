package com.pelmenstar.projktSens.weather.models;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The exception that may be thrown in {@link WeatherDataSource}
 */
public final class DataSourceException extends Exception {
    public DataSourceException() {}

    public DataSourceException(@NotNull String msg) {
        super(msg);
    }

    public DataSourceException(@Nullable Exception e) {
        super(e);
    }

    public DataSourceException(@NotNull String msg, @Nullable Exception e) {
        super(msg, e);
    }
}
