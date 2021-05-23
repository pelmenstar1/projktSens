package com.pelmenstar.projktSens.weather.app;

import android.content.Context;
import android.content.SharedPreferences;

import com.pelmenstar.projktSens.weather.models.ValueUnitsPacked;

import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;
import java.net.UnknownHostException;

public final class Preferences {
    private static final Preferences INSTANCE = new Preferences();
    private static final InetAddress DEFAULT_SERVER_ADDRESS;
    private static final String DEFAULT_SEVER_ADDRESS_STRING = "192.168.17.21";

    static {
        try {
            DEFAULT_SERVER_ADDRESS = InetAddress.getByAddress(new byte[] {
                    (byte)192,
                    (byte)168,
                    17,
                    21
            });
        } catch (UnknownHostException ignored) {
            throw new RuntimeException();
        }
    }

    // should not be changed
    private static final String KEY_UNITS = "units";
    private static final String KEY_SERVER_HOST = "serverHost";

    private static SharedPreferences prefs;

    private static volatile int units;

    private static String serverHostStr;
    private static InetAddress serverHost;

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

                serverHostStr = prefs.getString(KEY_SERVER_HOST, "");
                try {
                    serverHost = InetAddress.getByName(serverHostStr);
                } catch (Exception ignored) {
                }

                if (!ValueUnitsPacked.isValid(units) || serverHost == null) {
                    writeDefault();
                }
            }
        }

        return INSTANCE;
    }

    // writes default preferences
    private static void writeDefault() {
        units = ValueUnitsPacked.CELSIUS_MM_OF_MERCURY;
        serverHost = DEFAULT_SERVER_ADDRESS;
        serverHostStr = DEFAULT_SEVER_ADDRESS_STRING;

        prefs.edit()
                .putInt(KEY_UNITS, ValueUnitsPacked.CELSIUS_MM_OF_MERCURY)
                .putString(KEY_SERVER_HOST, serverHostStr)
                .apply();
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

    @NotNull
    public InetAddress getServerHost() {
        return serverHost;
    }

    @NotNull
    public String getServerHostString() {
        return serverHostStr;
    }

    public void setServerHost(@NotNull InetAddress host, @NotNull String hostString) {
        serverHost = host;
        serverHostStr = hostString;

        prefs.edit().putString(KEY_SERVER_HOST, hostString).apply();
    }
}