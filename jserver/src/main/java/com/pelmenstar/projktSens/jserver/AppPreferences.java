package com.pelmenstar.projktSens.jserver;

import android.content.Context;
import android.content.SharedPreferences;

import com.pelmenstar.projktSens.serverProtocol.repo.RepoContractType;
import com.pelmenstar.projktSens.shared.android.Preferences;

import org.jetbrains.annotations.NotNull;

public final class AppPreferences implements Preferences {
    public static final AppPreferences INSTANCE = new AppPreferences();

    private static boolean isInitialized = false;
    private static final Object lock = new Object();
    private static SharedPreferences prefs;

    private static final int DEFAULT_REPO_PORT = 10001;
    private static final int DEFAULT_SERVER_CONTRACT = RepoContractType.CONTRACT_RAW;
    private static final int DEFAULT_WEATHER_SEND_INTERVAL = 10 * 1000;

    private static final String KEY_REPO_PORT = "repoPort";
    private static final String KEY_SERVER_CONTRACT = "serverContract";
    private static final String KEY_WEATHER_SEND_INTERVAL = "weatherSendInterval";

    public static final int REPO_PORT = 0;
    public static final int WCI_PORT = 1;
    public static final int SERVER_CONTRACT = 2;
    public static final int WEATHER_SEND_INTERVAL = 3;

    private AppPreferences() {
    }

    public static AppPreferences of(@NotNull Context context) {
        INSTANCE.initialize(context);
        return INSTANCE;
    }

    @Override
    public void initialize(@NotNull Context context) {
        synchronized (lock) {
            if(!isInitialized) {
                isInitialized = true;

                context = context.getApplicationContext();

                prefs = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
                int repoPort = prefs.getInt(KEY_REPO_PORT, -1);
                int serverContract = prefs.getInt(KEY_SERVER_CONTRACT, -1);
                int weatherSendInterval = prefs.getInt(KEY_WEATHER_SEND_INTERVAL, -1);

                if((repoPort | serverContract | weatherSendInterval) < 0) {
                    writeDefault();
                }
            }
        }
    }

    private static void writeDefault() {
        prefs.edit()
                .putInt(KEY_REPO_PORT, DEFAULT_REPO_PORT)
                .putInt(KEY_SERVER_CONTRACT, DEFAULT_SERVER_CONTRACT)
                .putInt(KEY_WEATHER_SEND_INTERVAL, DEFAULT_WEATHER_SEND_INTERVAL)
                .apply();
    }

    @NotNull
    @Override
    public Object get(int id) {
        switch (id) {
            case REPO_PORT:
                return getRepoPort();
            case SERVER_CONTRACT:
                return getServerContract();
            case WEATHER_SEND_INTERVAL:
                return getWeatherSendInterval();
            default:
                throw new IllegalArgumentException("No option found with id " + id);
        }
    }

    @Override
    public int getInt(int id) {
        switch (id) {
            case REPO_PORT:
                return getRepoPort();
            case SERVER_CONTRACT:
                return getServerContract();
            case WEATHER_SEND_INTERVAL:
                return getWeatherSendInterval();
            default:
                throw new IllegalArgumentException("No option found with id " + id);
        }
    }

    @Override
    public boolean getBoolean(int id) {
        throw new IllegalArgumentException("Option " + id + " isn't boolean");
    }

    @Override
    public void set(int id, @NotNull Object value) {
        switch (id) {
            case REPO_PORT:
                setRepoPort((Integer)value);
                break;
            case SERVER_CONTRACT:
                setServerContract((Integer)value);
                break;
            case WEATHER_SEND_INTERVAL:
                setWeatherSendInterval((Integer)value);
                break;
            default:
                throw new IllegalArgumentException("No option found with id " + id);
        }
    }

    @Override
    public void setInt(int id, int value) {
        switch (id) {
            case REPO_PORT:
                setRepoPort(value);
                break;
            case SERVER_CONTRACT:
                setServerContract(value);
                break;
            case WEATHER_SEND_INTERVAL:
                setWeatherSendInterval(value);
                break;
            default:
                throw new IllegalArgumentException("No option found with id " + id);
        }
    }

    @Override
    public void setBoolean(int id, boolean value) {
        throw new IllegalArgumentException("Option " + id + " isn't boolean");
    }

    public int getRepoPort() {
        return prefs.getInt(KEY_REPO_PORT, DEFAULT_REPO_PORT);
    }

    public void setRepoPort(int port) {
        prefs.edit().putInt(KEY_REPO_PORT, port).apply();
    }

    public int getServerContract() {
        return prefs.getInt(KEY_SERVER_CONTRACT, DEFAULT_SERVER_CONTRACT);
    }

    public void setServerContract(int contractType) {
        if(!RepoContractType.isValid(contractType)) {
            throw new IllegalArgumentException("contractType");
        }

        prefs.edit().putInt(KEY_SERVER_CONTRACT, contractType).apply();
    }

    public int getWeatherSendInterval() {
        return prefs.getInt(KEY_WEATHER_SEND_INTERVAL, DEFAULT_WEATHER_SEND_INTERVAL);
    }

    public void setWeatherSendInterval(int interval) {
        prefs.edit().putInt(KEY_WEATHER_SEND_INTERVAL, interval).apply();
    }
}
