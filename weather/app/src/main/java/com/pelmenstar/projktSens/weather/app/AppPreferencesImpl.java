package com.pelmenstar.projktSens.weather.app;

import android.content.Context;
import android.content.SharedPreferences;

import com.pelmenstar.projktSens.serverProtocol.repo.RepoContractType;
import com.pelmenstar.projktSens.shared.InetAddressUtils;
import com.pelmenstar.projktSens.weather.models.ValueUnitsPacked;

import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;

public class AppPreferencesImpl implements AppPreferences {
    public static final AppPreferencesImpl INSTANCE = new AppPreferencesImpl();
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
    private static final String KEY_IS_GPS_PERMISSION_DENIED = "isGpsDenied";

    private static SharedPreferences prefs;

    private static volatile boolean isInitialized = false;
    private static final Object lock = new Object();

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
    public void initialize(@NotNull Context context) {
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
                boolean isValidHost = false;
                if(serverHostStr != null) {
                    isValidHost = InetAddressUtils.isValidNumericalIpv4(serverHostStr);
                }
                boolean isGpsDeniedExists = prefs.contains(KEY_IS_GPS_PERMISSION_DENIED);

                if (!ValueUnitsPacked.isValid(units) ||
                        !isValidHost ||
                        !isGpsDeniedExists ||
                        (contractType | repoPort | wciPort | weatherReceiveInterval) < 0) {
                    writeDefault();
                }
            }
        }
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
                .putBoolean(KEY_IS_GPS_PERMISSION_DENIED, false)
                .apply();
    }

    @Override
    @NotNull
    public Object get(int id) {
        switch (id) {
            case UNITS:
                return getUnits();
            case SERVER_HOST:
                return getServerHostString();
            case CONTRACT:
                return getContractType();
            case REPO_PORT:
                return getRepoPort();
            case WCI_PORT:
                return getWciPort();
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
            case SERVER_HOST:
                throw new IllegalArgumentException("SERVER_HOST isn't int");
            case CONTRACT:
                return getContractType();
            case REPO_PORT:
                return getRepoPort();
            case WCI_PORT:
                return getWciPort();
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
            case SERVER_HOST:
                setServerHostString((String) value);
                break;
            case CONTRACT:
                setContractType((Integer) value);
                break;
            case REPO_PORT:
                setRepoPort((Integer) value);
                break;
            case WCI_PORT:
                setWciPort((Integer) value);
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
            case SERVER_HOST:
                throw new IllegalArgumentException("SERVER_HOST isn't int");
            case CONTRACT:
                setContractType(value);
                break;
            case REPO_PORT:
                setRepoPort(value);
                break;
            case WCI_PORT:
                setWciPort(value);
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
        return prefs.getInt(KEY_UNITS, ValueUnitsPacked.CELSIUS_MM_OF_MERCURY);
    }

    public void setUnits(int units) {
        if(!ValueUnitsPacked.isValid(units)) {
            throw new IllegalArgumentException("units");
        }

        prefs.edit().putInt(KEY_UNITS, units).apply();
    }

    @NotNull
    public InetAddress getServerHost() {
        return InetAddressUtils.parseNumericalIpv4OrThrow(getServerHostString());
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

    @Override
    public boolean isGpsPermissionDenied() {
        return prefs.getBoolean(KEY_IS_GPS_PERMISSION_DENIED, false);
    }

    @Override
    public void setGpsPermissionDenied(boolean value) {
        prefs.edit().putBoolean(KEY_IS_GPS_PERMISSION_DENIED, value).apply();
    }
}
