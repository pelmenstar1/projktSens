package com.pelmenstar.projktSens.weather.app;

import android.content.Context;
import android.content.SharedPreferences;

import com.pelmenstar.projktSens.serverProtocol.ContractType;
import com.pelmenstar.projktSens.shared.android.AbstractPreferencesThroughShared;
import com.pelmenstar.projktSens.weather.models.ValueUnitsPacked;

import org.jetbrains.annotations.NotNull;

public class AppPreferencesImpl extends AbstractPreferencesThroughShared implements AppPreferences {
    public static final AppPreferencesImpl INSTANCE = new AppPreferencesImpl();
    private static final int DEFAULT_SEVER_ADDRESS_INT = 0;
    private static final int DEFAULT_SERVER_PORT = 10001;
    private static final int DEFAULT_WEATHER_RECEIVE_INTERVAL = 10 * 1000;
    private static final int DEFAULT_CONTRACT_TYPE = ContractType.CONTRACT_RAW;

    // should not be changed
    private static final String KEY_UNITS = "units";
    private static final String KEY_SERVER_HOST = "serverHost";
    private static final String KEY_CONTRACT = "contract";
    private static final String KEY_SERVER_PORT = "PORT";
    private static final String KEY_WEATHER_RECEIVE_INTERVAL = "weatherRcvInterval";
    private static final String KEY_IS_GPS_PERMISSION_DENIED = "isGpsDenied";

    private AppPreferencesImpl() {
    }

    /**
     * Obtains instance of {@link AppPreferences}
     *
     * @implNote {@link AppPreferences} is singleton.
     * This method just obtains instance of {@link SharedPreferences} from given context and saves it to static variable.
     * This method is here just for code readability
     */
    @NotNull
    public static AppPreferencesImpl of(@NotNull Context context) {
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
        return 6;
    }

    @Override
    protected void checkIfCorrupted() {
        SharedPreferences prefs = preferences;
        int units = prefs.getInt(KEY_UNITS, ValueUnitsPacked.NONE);
        int contractType = prefs.getInt(KEY_CONTRACT, -1);
        int serverPort = prefs.getInt(KEY_SERVER_PORT, -1);
        int weatherReceiveInterval = prefs.getInt(KEY_WEATHER_RECEIVE_INTERVAL, -1);

        boolean isGpsDeniedExists = prefs.contains(KEY_IS_GPS_PERMISSION_DENIED);

        if (!ValueUnitsPacked.isValid(units) ||
                !prefs.contains(KEY_SERVER_HOST) ||
                !isGpsDeniedExists ||
                (contractType | serverPort | weatherReceiveInterval) < 0) {
            writeDefault();
        }
    }

    // writes default preferences
    private void writeDefault() {
        preferences.edit()
                .putInt(KEY_UNITS, ValueUnitsPacked.CELSIUS_MM_OF_MERCURY)
                .putInt(KEY_SERVER_HOST, DEFAULT_SEVER_ADDRESS_INT)
                .putInt(KEY_CONTRACT, ContractType.CONTRACT_RAW)
                .putInt(KEY_SERVER_PORT, DEFAULT_SERVER_PORT)
                .putInt(KEY_WEATHER_RECEIVE_INTERVAL, DEFAULT_WEATHER_RECEIVE_INTERVAL)
                .putBoolean(KEY_IS_GPS_PERMISSION_DENIED, false)
                .apply();
    }

    @Override
    @NotNull
    public Object get(int id) {
        switch (id) {
            case UNITS:
                return getUnits();
            case SERVER_HOST_INT:
                return getServerHostInt();
            case CONTRACT:
                return getContractType();
            case SERVER_PORT:
                return getServerPort();
            case WEATHER_RECEIVE_INTERVAL:
                return getWeatherReceiveInterval();
            default:
                throw new IllegalArgumentException("No option found with id " + id);
        }
    }

    @Override
    public int getInt(int id) {
        switch (id) {
            case UNITS:
                return getUnits();
            case SERVER_HOST_INT:
                return getServerHostInt();
            case CONTRACT:
                return getContractType();
            case SERVER_PORT:
                return getServerPort();
            case WEATHER_RECEIVE_INTERVAL:
                return getWeatherReceiveInterval();
            default:
                throw new IllegalArgumentException("No option found with id " + id);
        }
    }

    @Override
    public boolean getBoolean(int id) {
        if(id == IS_GPS_PERMISSION_DENIED) {
            return isGpsPermissionDenied();
        }

        throw new IllegalArgumentException("Option " + id + " isn't boolean");
    }

    @Override
    public void set(int id, @NotNull Object value) {
        switch (id) {
            case UNITS:
                setUnits((Integer) value);
                break;
            case SERVER_HOST_INT:
                setServerHostInt((Integer) value);
                break;
            case CONTRACT:
                setContractType((Integer) value);
                break;
            case SERVER_PORT:
                setServerPort((Integer) value);
                break;
            case WEATHER_RECEIVE_INTERVAL:
                setWeatherReceiveInterval((Integer) value);
                break;
            default:
                throw new IllegalArgumentException("No option found with id " + id);
        }
    }

    @Override
    public void setInt(int id, int value) {
        switch (id) {
            case UNITS:
                setUnits(value);
                break;
            case SERVER_HOST_INT:
                setServerHostInt(value);
                break;
            case CONTRACT:
                setContractType(value);
                break;
            case SERVER_PORT:
                setServerPort(value);
                break;
            case WEATHER_RECEIVE_INTERVAL:
                setWeatherReceiveInterval(value);
                break;
            default:
                throw new IllegalArgumentException("No option found with id " + id);
        }
    }

    @Override
    public void setBoolean(int id, boolean value) {
        if(id == IS_GPS_PERMISSION_DENIED) {
            setGpsPermissionDenied(value);
        }

        throw new IllegalArgumentException("Option " + id + " isn't boolean");
    }

    /**
     * Gets packed units which was saved in {@link SharedPreferences}
     */
    public int getUnits() {
        return safeGetInt(KEY_UNITS, UNITS, ValueUnitsPacked.CELSIUS_MM_OF_MERCURY);
    }

    public void setUnits(int units) {
        if(!ValueUnitsPacked.isValid(units)) {
            throw new IllegalArgumentException("units");
        }

        safePutInt(KEY_UNITS, UNITS, units);
    }

    @Override
    public int getServerHostInt() {
        return safeGetInt(KEY_SERVER_HOST, SERVER_HOST_INT, DEFAULT_SEVER_ADDRESS_INT);
    }

    @Override
    public void setServerHostInt(int value) {
        safePutInt(KEY_SERVER_HOST, SERVER_HOST_INT, value);
    }

    public int getContractType() {
        return safeGetInt(KEY_CONTRACT, CONTRACT, DEFAULT_CONTRACT_TYPE);
    }

    public void setContractType(int contractType) {
        if(!ContractType.isValid(contractType)) {
            throw new IllegalArgumentException("contractType");
        }

        safePutInt(KEY_CONTRACT, CONTRACT, contractType);
    }

    public int getServerPort() {
        return safeGetInt(KEY_SERVER_PORT, SERVER_PORT, DEFAULT_SERVER_PORT);
    }

    public void setServerPort(int port) {
        safePutInt(KEY_SERVER_PORT, SERVER_PORT, port);
    }

    public int getWeatherReceiveInterval() {
        return safeGetInt(KEY_WEATHER_RECEIVE_INTERVAL, WEATHER_RECEIVE_INTERVAL, DEFAULT_WEATHER_RECEIVE_INTERVAL);
    }

    public void setWeatherReceiveInterval(int interval) {
        safePutInt(KEY_WEATHER_RECEIVE_INTERVAL, WEATHER_RECEIVE_INTERVAL, interval);
    }

    @Override
    public boolean isGpsPermissionDenied() {
        return safeGetBoolean(KEY_IS_GPS_PERMISSION_DENIED, IS_GPS_PERMISSION_DENIED, false);
    }

    @Override
    public void setGpsPermissionDenied(boolean value) {
        safePutBoolean(KEY_IS_GPS_PERMISSION_DENIED, IS_GPS_PERMISSION_DENIED, value);
    }
}
