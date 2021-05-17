package com.pelmenstar.projktSens.weather.app;

import android.content.Context;
import android.content.SharedPreferences;

import com.pelmenstar.projktSens.weather.models.ValueUnitsPacked;

import org.jetbrains.annotations.NotNull;

public final class Preferences {
    private static final Preferences INSTANCE = new Preferences();

    // should not be changed
    private static final String KEY_UNITS = "units";

    private static SharedPreferences prefs;

    private static volatile int units;

    private static volatile boolean isInitialized = false;
    private static final Object lock = new Object();

    private Preferences() {
    }

    /**
     * Obtains instance of {@link Preferences}
     *
     * @implNote {@link Preferences} is singleton.
     * This method just obtains instance of {@link SharedPreferences} from given context and saves it to static variable.
     * This method is here just for code readability
     */
    @NotNull
    public static Preferences of(@NotNull Context context) {
        synchronized (lock) {
            if(!isInitialized) {
                isInitialized = true;

                prefs = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
                units = prefs.getInt(KEY_UNITS, ValueUnitsPacked.NONE);

                if (!ValueUnitsPacked.isValid(units)) {
                    writeDefault();
                }
            }
        }

        return INSTANCE;
    }

    // writes default preferences
    private static void writeDefault() {
        units = ValueUnitsPacked.CELSIUS_MM_OF_MERCURY;

        prefs.edit().putInt(KEY_UNITS, ValueUnitsPacked.CELSIUS_MM_OF_MERCURY).apply();
    }

    /**
     * Gets packed units which was saved in {@link SharedPreferences}
     */
    public int getUnits() {
        return units;
    }

    /**
     * Sets packed units to memory and disk.
     * On the next start, {@link Preferences#getUnits()} will return the same packed units
     *
     * @throws IllegalArgumentException if given units is invalid
     */
    public void setUnits(int units) {
        if(!ValueUnitsPacked.isValid(units)) {
            throw new IllegalArgumentException("units");
        }

        Preferences.units = units;
        prefs.edit().putInt(KEY_UNITS, units).apply();
    }
}