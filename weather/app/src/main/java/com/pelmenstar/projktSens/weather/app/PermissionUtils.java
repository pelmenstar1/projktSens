package com.pelmenstar.projktSens.weather.app;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Process;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import org.jetbrains.annotations.NotNull;

public final class PermissionUtils {
    public static final @NotNull String @NotNull [] LOCATION_PERMISSIONS = new String[] {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private PermissionUtils() {}

    @RequiresApi(23)
    public static boolean isLocationGranted(@NotNull Context context) {
        int pid = Process.myPid();
        int uid = Process.myUid();

        for(String permission: LOCATION_PERMISSIONS) {
            if(context.checkPermission(permission, pid, uid) == PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        }

        return false;
    }

    @RequiresApi(23)
    public static boolean isNeverShowAgainOnLocation(@NotNull Activity activity) {
        for(String permission: LOCATION_PERMISSIONS) {
            if(!activity.shouldShowRequestPermissionRationale(permission)) {
                return true;
            }
        }

        return false;
    }

    @RequiresApi(23)
    public static boolean isNeverShowAgainOnLocation(@NotNull Fragment fragment) {
        for(String permission: LOCATION_PERMISSIONS) {
            if(!fragment.shouldShowRequestPermissionRationale(permission)) {
                return true;
            }
        }

        return false;
    }
}
