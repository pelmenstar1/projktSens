package com.pelmenstar.projktSens.weather.app;

import com.pelmenstar.projktSens.shared.geo.Geolocation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class GeolocationCache {
    @Nullable
    private static volatile Geolocation lastGeolocation;

    private static final Object lock = new Object();

    @NotNull
    public static Object lock() {
        return lock;
    }

    @Nullable
    public static Geolocation get() {
        synchronized (lock) {
            return lastGeolocation;
        }
    }

    @NotNull
    public static Geolocation getNotNullOrThrow() {
        synchronized (lock) {
            Geolocation loc = lastGeolocation;
            if(loc == null) {
                throw new RuntimeException("Geolocation is not loaded");
            }

            return loc;
        }
    }

    public static void set(@Nullable Geolocation location) {
        synchronized (lock) {
            lastGeolocation = location;
        }
    }
}
