package com.pelmenstar.projktSens.shared.android;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;

import org.jetbrains.annotations.NotNull;

public final class NetworkUtils {
    private NetworkUtils() {}

    public static boolean isConnectedToAnyNetwork(@NotNull Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (Build.VERSION.SDK_INT >= 23) {
            Network active = manager.getActiveNetwork();

            if(active != null) {
                NetworkCapabilities capabilities = manager.getNetworkCapabilities(active);

                if(capabilities != null) {
                    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
                }
            }

            return false;
        } else {
            NetworkInfo active = manager.getActiveNetworkInfo();

            return active != null && active.isConnected();
        }
    }
}
