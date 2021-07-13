package com.pelmenstar.projktSens.weather.app;

import com.pelmenstar.projktSens.shared.android.Preferences;

import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;

public interface AppPreferences extends Preferences {
    int UNITS = 0;
    int SERVER_HOST = 1;
    int CONTRACT = 2;
    int REPO_PORT = 3;
    int WCI_PORT = 4;
    int WEATHER_RECEIVE_INTERVAL = 5;
    int IS_GPS_PERMISSION_DENIED = 6;

    int getUnits();
    void setUnits(int units);

    @NotNull
    InetAddress getServerHost();

    @NotNull
    String getServerHostString();
    void setServerHostString(@NotNull String host);

    int getContractType();
    void setContractType(int type);

    int getRepoPort();
    void setRepoPort(int port);

    int getWciPort();
    void setWciPort(int port);

    int getWeatherReceiveInterval();
    void setWeatherReceiveInterval(int interval);

    boolean isGpsPermissionDenied();
    void setGpsPermissionDenied(boolean value);
}