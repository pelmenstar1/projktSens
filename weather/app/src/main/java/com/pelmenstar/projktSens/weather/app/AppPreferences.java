package com.pelmenstar.projktSens.weather.app;

import com.pelmenstar.projktSens.shared.android.Preferences;

import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;

public interface AppPreferences extends Preferences {
    int UNITS = 0;
    int SERVER_HOST_INT = 1;
    int CONTRACT = 2;
    int SERVER_PORT = 3;
    int WEATHER_RECEIVE_INTERVAL = 5;
    int IS_GPS_PERMISSION_DENIED = 6;

    int getUnits();
    void setUnits(int units);

    int getServerHostInt();
    void setServerHostInt(int value);

    int getContractType();
    void setContractType(int type);

    int getServerPort();
    void setServerPort(int port);

    int getWeatherReceiveInterval();
    void setWeatherReceiveInterval(int interval);

    boolean isGpsPermissionDenied();
    void setGpsPermissionDenied(boolean value);
}