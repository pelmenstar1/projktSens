package com.pelmenstar.projktSens.shared.android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.pelmenstar.projktSens.shared.IntPair;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;

public abstract class AbstractPreferencesThroughShared implements Preferences {
    private static final class PackedIntAndValidity {
        public static long create(int value, boolean isValid) {
            return IntPair.create(value, isValid ? 1 : 0);
        }

        public static int getValue(long packed) {
            return IntPair.getFirst(packed);
        }

        public static boolean isValid(long packed) {
            return IntPair.getSecond(packed) > 0;
        }
    }

    @NotNull
    private final String tag = getClass().getName();

    @Nullable
    private SharedPreferences.Editor sessionEditor;
    private final long @NotNull [] sessionModifiedInts;

    private boolean isInitialized;

    @Nullable
    private SharedPreferences preferences;

    public AbstractPreferencesThroughShared() {
        int count = getPreferenceValuesCount();
        if (count <= 0) {
            throw new RuntimeException("getPreferenceValuesCount() returned value <= 0");
        }

        sessionModifiedInts = new long[count];
    }

    @Override
    public final void initialize(@NotNull Context context) {
        if (!isInitialized) {
            isInitialized = true;

            preferences = context.getSharedPreferences(getPreferencesName(), Context.MODE_PRIVATE);

            checkIfCorrupted();
        }
    }

    @SuppressLint("CommitPrefEdits")
    @Override
    public final void beginModifying() {
        if (sessionEditor != null) {
            Log.e(tag, "endModifying() wasn't called");
        } else {
            Arrays.fill(sessionModifiedInts, 0);

            sessionEditor = preferences().edit();
        }
    }

    @Override
    public final void endModifying() {
        if (sessionEditor == null) {
            Log.e(tag, "startModifying() wasn't called");
            return;
        }

        sessionEditor.apply();
        sessionEditor = null;
    }

    @NotNull
    protected final SharedPreferences preferences() {
        return Objects.requireNonNull(preferences);
    }

    protected final int getInt(@NotNull String key, int id, int defaultValue) {
        if (sessionEditor != null) {
            long packed = sessionModifiedInts[id];
            if(PackedIntAndValidity.isValid(packed)) {
                return PackedIntAndValidity.getValue(packed);
            }
        }

        return preferences().getInt(key, defaultValue);
    }

    public final void putInt(@NotNull String key, int id, int value) {
        if (sessionEditor != null) {
            sessionEditor.putInt(key, value);
            sessionModifiedInts[id] = PackedIntAndValidity.create(value, true);
        } else {
            preferences().edit().putInt(key, value).apply();
        }
    }

    protected final boolean getBoolean(@NotNull String key, int id, boolean defaultValue) {
        if (sessionEditor != null) {
            long packed = sessionModifiedInts[id];
            if(PackedIntAndValidity.isValid(packed)) {
                return PackedIntAndValidity.getValue(packed) > 0;
            }
        }

        return preferences().getBoolean(key, defaultValue);
    }

    protected final void putBoolean(@NotNull String key, int id, boolean value) {
        if (sessionEditor != null) {
            sessionEditor.putBoolean(key, value);
            sessionModifiedInts[id] = PackedIntAndValidity.create(value ? 1 : 0, true);
        } else {
            preferences().edit().putBoolean(key, value).apply();
        }
    }

    protected abstract int getPreferenceValuesCount();

    protected abstract void checkIfCorrupted();

    @NotNull
    protected abstract String getPreferencesName();
}
