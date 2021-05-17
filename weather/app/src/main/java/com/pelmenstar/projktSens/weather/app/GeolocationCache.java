package com.pelmenstar.projktSens.weather.app;

import com.pelmenstar.projktSens.shared.geo.Geolocation;

import org.jetbrains.annotations.Nullable;

public final class GeolocationCache {
    @Nullable
    private static volatile Geolocation lastGeolocation;

    @Nullable
    public static Geolocation get() {
        return lastGeolocation;
    }

    public static void set(@Nullable Geolocation location) {
        lastGeolocation = location;
    }
}
