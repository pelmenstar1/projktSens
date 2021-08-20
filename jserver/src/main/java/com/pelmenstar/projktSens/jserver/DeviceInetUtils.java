package com.pelmenstar.projktSens.jserver;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.pelmenstar.projktSens.shared.InetAddressUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;

public final class DeviceInetUtils {
    private DeviceInetUtils() {
    }

    @Nullable
    public static InetAddress getInetAddress(@NotNull Context context) {
        Context appContext = context.getApplicationContext();
        if (appContext == null) {
            appContext = context;
        }

        WifiManager manager = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = manager.getConnectionInfo();
        if (info != null) {
            return InetAddressUtils.parseInt(info.getIpAddress());
        }
        return null;
    }
}
