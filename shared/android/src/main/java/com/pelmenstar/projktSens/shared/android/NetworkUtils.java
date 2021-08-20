package com.pelmenstar.projktSens.shared.android;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.jetbrains.annotations.NotNull;

public final class NetworkUtils {
    private NetworkUtils() {}

    public static boolean isConnectedToAnyNetwork(@NotNull Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo active = manager.getActiveNetworkInfo();

        return active != null && active.isConnected();
    }
}
