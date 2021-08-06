package com.pelmenstar.projktSens.jserver;

import android.content.Context;
import android.content.SharedPreferences;

import com.pelmenstar.projktSens.serverProtocol.ContractType;
import com.pelmenstar.projktSens.shared.android.AbstractPreferencesThroughShared;
import com.pelmenstar.projktSens.shared.android.Preferences;

import org.jetbrains.annotations.NotNull;

public final class AppPreferences extends AbstractPreferencesThroughShared implements Preferences {
    public static final AppPreferences INSTANCE = new AppPreferences();

    private static final int DEFAULT_SERVER_PORT = 10001;
    private static final int DEFAULT_SERVER_CONTRACT = ContractType.CONTRACT_RAW;
    private static final int DEFAULT_WEATHER_SEND_INTERVAL = 10 * 1000;

    private static final String KEY_SERVER_PORT = "port";
    private static final String KEY_SERVER_CONTRACT = "serverContract";
    private static final String KEY_WEATHER_SEND_INTERVAL = "weatherSendInterval";

    public static final int SERVER_PORT = 0;
    public static final int SERVER_CONTRACT = 2;
    public static final int WEATHER_SEND_INTERVAL = 3;

    private AppPreferences() {
    }

    public static AppPreferences of(@NotNull Context context) {
        INSTANCE.initialize(context);
        return INSTANCE;
    }

    @Override
    @NotNull
    protected String getPreferencesName() {
        return BuildConfig.APPLICATION_ID;
    }

    @Override
    protected int getPreferenceValuesCount() {
        return 3;
    }

    @Override
    protected void checkIfCorrupted() {
        SharedPreferences prefs = preferences;
        int serverPort = prefs.getInt(KEY_SERVER_PORT, -1);
        int serverContract = prefs.getInt(KEY_SERVER_CONTRACT, -1);
        int weatherSendInterval = prefs.getInt(KEY_WEATHER_SEND_INTERVAL, -1);

        if((serverPort | serverContract | weatherSendInterval) < 0) {
            writeDefault();
        }
    }

    private void writeDefault() {
        preferences.edit()
                .putInt(KEY_SERVER_PORT, DEFAULT_SERVER_PORT)
                .putInt(KEY_SERVER_CONTRACT, DEFAULT_SERVER_CONTRACT)
                .putInt(KEY_WEATHER_SEND_INTERVAL, DEFAULT_WEATHER_SEND_INTERVAL)
                .apply();
    }

    @NotNull
    @Override
    public Object get(int id) {
        switch (id) {
            case SERVER_PORT:
                return getServerPort();
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
            case SERVER_PORT:
                return getServerPort();
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
            case SERVER_PORT:
                setServerPort((Integer)value);
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
            case SERVER_PORT:
                setServerPort(value);
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

    public int getServerPort() {
        return safeGetInt(KEY_SERVER_PORT, SERVER_PORT, DEFAULT_SERVER_PORT);
    }

    public void setServerPort(int port) {
        safePutInt(KEY_SERVER_PORT, SERVER_PORT, port);
    }

    public int getServerContract() {
        return safeGetInt(KEY_SERVER_CONTRACT, SERVER_CONTRACT, DEFAULT_SERVER_CONTRACT);
    }

    public void setServerContract(int contractType) {
        if(!ContractType.isValid(contractType)) {
            throw new IllegalArgumentException("contractType");
        }

        safePutInt(KEY_SERVER_CONTRACT, SERVER_CONTRACT, contractType);
    }

    public int getWeatherSendInterval() {
        return safeGetInt(KEY_WEATHER_SEND_INTERVAL, WEATHER_SEND_INTERVAL, DEFAULT_WEATHER_SEND_INTERVAL);
    }

    public void setWeatherSendInterval(int interval) {
        safePutInt(KEY_WEATHER_SEND_INTERVAL, WEATHER_SEND_INTERVAL, interval);
    }
}
