package com.pelmenstar.projktSens.jserver;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;

public final class InetUtils {
    private InetUtils() {}

    @Nullable
    public static InetAddress getInetAddress(@NotNull Context context) {
        Context appContext = context.getApplicationContext();
        if(appContext == null) {
            appContext = context;
        }

        WifiManager manager = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = manager.getConnectionInfo();
        if (info != null) {
            int i = info.getIpAddress();
            byte[] data = new byte[]{
                    (byte) i,
                    (byte) (i >> 8),
                    (byte) (i >> 16),
                    (byte) (i >> 24)
            };

            InetAddress address = null;

            try {
                // UnknownHostException is checked exception.
                // It throws when data has invalid size. Genius
                address = InetAddress.getByAddress(data);
            } catch (Exception ignored) {
            }

            // can't be null
            return address;
        }
        return null;
    }
}
