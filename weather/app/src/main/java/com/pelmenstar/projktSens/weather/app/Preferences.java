package com.pelmenstar.projktSens.weather.app;

import android.content.Context;
import android.content.SharedPreferences;

import com.pelmenstar.projktSens.serverProtocol.repo.RepoContractType;
import com.pelmenstar.projktSens.weather.models.ValueUnitsPacked;

import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;
import java.net.UnknownHostException;

public final class Preferences {
    private static final Preferences INSTANCE = new Preferences();
    private static final String DEFAULT_SEVER_ADDRESS_STRING = "192.168.17.21";
    private static final int DEFAULT_REPO_SERVER_PORT = 10001;
    private static final int DEFAULT_WCI_SERVER_PORT = 10002;
    private static final int DEFAULT_WEATHER_RECEIVE_INTERVAL = 10 * 1000;
    private static final int DEFAULT_REPO_CONTRACT_TYPE = RepoContractType.CONTRACT_RAW;

    // should not be changed
    private static final String KEY_UNITS = "units";
    private static final String KEY_SERVER_HOST = "serverHost";
    private static final String KEY_CONTRACT = "contract";
    private static final String KEY_REPO_PORT = "repoPort";
    private static final String KEY_WCI_PORT = "wciPort";
    private static final String KEY_WEATHER_RECEIVE_INTERVAL = "weatherRcvInterval";

    private static SharedPreferences prefs;

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

                int units = prefs.getInt(KEY_UNITS, ValueUnitsPacked.NONE);
                int contractType = prefs.getInt(KEY_CONTRACT, -1);
                int repoPort = prefs.getInt(KEY_REPO_PORT, -1);
                int wciPort = prefs.getInt(KEY_WCI_PORT, -1);
                int weatherReceiveInterval = prefs.getInt(KEY_WEATHER_RECEIVE_INTERVAL, -1);

                String serverHostStr = prefs.getString(KEY_SERVER_HOST, null);
                InetAddress serverHost = null;

                if(serverHostStr != null) {
                    try {
                        serverHost = InetAddress.getByName(serverHostStr);
                    } catch (Exception ignored) {
                    }
                }

                if (!ValueUnitsPacked.isValid(units) ||
                        serverHost == null ||
                        (contractType | repoPort | wciPort | weatherReceiveInterval) < 0) {
                    writeDefault();
                }
            }
        }

        return INSTANCE;
    }

    // writes default preferences
    private static void writeDefault() {
        prefs.edit()
                .putInt(KEY_UNITS, ValueUnitsPacked.CELSIUS_MM_OF_MERCURY)
                .putString(KEY_SERVER_HOST, DEFAULT_SEVER_ADDRESS_STRING)
                .putInt(KEY_CONTRACT, RepoContractType.CONTRACT_RAW)
                .putInt(KEY_REPO_PORT, DEFAULT_REPO_SERVER_PORT)
                .putInt(KEY_WCI_PORT, DEFAULT_WCI_SERVER_PORT)
                .putInt(KEY_WEATHER_RECEIVE_INTERVAL, DEFAULT_WEATHER_RECEIVE_INTERVAL)
                .apply();
    }

    /**
     * Gets packed units which was saved in {@link SharedPreferences}
     */
    public int getUnits() {
        return prefs.getInt(KEY_UNITS, ValueUnitsPacked.CELSIUS_MM_OF_MERCURY);
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

        prefs.edit().putInt(KEY_UNITS, units).apply();
    }

    @NotNull
    public InetAddress getServerHost() {
        try {
            return InetAddress.getByName(getServerHostString());
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    public String getServerHostString() {
        return prefs.getString(KEY_SERVER_HOST, DEFAULT_SEVER_ADDRESS_STRING);
    }

    public void setServerHostString(@NotNull String hostString) {
        prefs.edit().putString(KEY_SERVER_HOST, hostString).apply();
    }

    public int getContractType() {
        return prefs.getInt(KEY_CONTRACT, DEFAULT_REPO_CONTRACT_TYPE);
    }

    public void setContractType(int contractType) {
        if(!RepoContractType.isValid(contractType)) {
            throw new IllegalArgumentException("contractType");
        }

        prefs.edit().putInt(KEY_CONTRACT, contractType).apply();
    }

    public int getRepoPort() {
        return prefs.getInt(KEY_REPO_PORT, DEFAULT_REPO_SERVER_PORT);
    }

    public void setRepoPort(int port) {
        prefs.edit().putInt(KEY_REPO_PORT, port).apply();
    }

    public int getWciPort() {
        return prefs.getInt(KEY_WCI_PORT, DEFAULT_WCI_SERVER_PORT);
    }

    public void setWciPort(int port) {
        prefs.edit().putInt(KEY_WCI_PORT, port).apply();
    }

    public int getWeatherReceiveInterval() {
        return prefs.getInt(KEY_WEATHER_RECEIVE_INTERVAL, DEFAULT_WEATHER_RECEIVE_INTERVAL);
    }

    public void setWeatherReceiveInterval(int interval) {
        prefs.edit().putInt(KEY_WEATHER_RECEIVE_INTERVAL, interval).apply();
    }
}