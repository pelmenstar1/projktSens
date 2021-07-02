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
    private static final String KEY_CONTRACT = "contract";
    private static final String KEY_REPO_PORT = "repoPort";
    private static final String KEY_WCI_PORT = "wciPort";
    private static final String KEY_WEATHER_RECEIVE_INTERVAL = "weatherRcvInterval";

    private static SharedPreferences prefs;

    private static volatile int units;
    private static volatile int contractType;
    private static volatile int repoPort;
    private static volatile int wciPort;
    private static volatile int weatherReceiveInterval;

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
                contractType = prefs.getInt(KEY_CONTRACT, -1);
                repoPort = prefs.getInt(KEY_REPO_PORT, -1);
                wciPort = prefs.getInt(KEY_WCI_PORT, -1);
                weatherReceiveInterval = prefs.getInt(KEY_WEATHER_RECEIVE_INTERVAL, -1);

                serverHostStr = prefs.getString(KEY_SERVER_HOST, "");
                try {
                    serverHost = InetAddress.getByName(serverHostStr);
                } catch (Exception ignored) {
                }

                if (!ValueUnitsPacked.isValid(units) ||
                        serverHost == null ||
                        contractType == -1 ||
                        repoPort == -1 ||
                        wciPort == -1 ||
                        weatherReceiveInterval == -1) {
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
        contractType = RepoContractType.CONTRACT_RAW;
        repoPort = 10001;
        wciPort = 10002;
        weatherReceiveInterval = 10 * 1000;

        prefs.edit()
                .putInt(KEY_UNITS, ValueUnitsPacked.CELSIUS_MM_OF_MERCURY)
                .putString(KEY_SERVER_HOST, serverHostStr)
                .putInt(KEY_CONTRACT, contractType)
                .putInt(KEY_REPO_PORT, repoPort)
                .putInt(KEY_WCI_PORT, wciPort)
                .putInt(KEY_WEATHER_RECEIVE_INTERVAL, weatherReceiveInterval)
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

    public int getContractType() {
        return contractType;
    }

    public void setContractType(int contractType) {
        if(!RepoContractType.isValid(contractType)) {
            throw new IllegalArgumentException("contractType");
        }

        Preferences.contractType = contractType;
        prefs.edit().putInt(KEY_CONTRACT, contractType).apply();
    }

    public int getRepoPort() {
        return repoPort;
    }

    public void setRepoPort(int port) {
        repoPort = port;
        prefs.edit().putInt(KEY_REPO_PORT, port).apply();
    }

    public int getWciPort() {
        return wciPort;
    }

    public void setWciPort(int port) {
        wciPort = port;
        prefs.edit().putInt(KEY_WCI_PORT, port).apply();
    }

    public int getWeatherReceiveInterval() {
        return weatherReceiveInterval;
    }

    public void setWeatherReceiveInterval(int interval) {
        weatherReceiveInterval = interval;
        prefs.edit().putInt(KEY_WEATHER_RECEIVE_INTERVAL, interval).apply();
    }
}