package com.pelmenstar.projktSens.weather.app;

import com.pelmenstar.projktSens.shared.android.Preferences;

public interface AppPreferences extends Preferences {
    int UNITS = 0;
    int SERVER_HOST_INT = 1;
    int CONTRACT = 2;
    int SERVER_PORT = 3;
    int WEATHER_RECEIVE_INTERVAL = 4;
    int IS_FIRST_START = 5;
    int KEEP_HOME_SCREEN_ON = 6;

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

    boolean isFirstStart();
    void setFirstStart(boolean value);

    boolean isKeepHomeScreenOn();
    void setKeepHomeScreenOn(boolean value);
}