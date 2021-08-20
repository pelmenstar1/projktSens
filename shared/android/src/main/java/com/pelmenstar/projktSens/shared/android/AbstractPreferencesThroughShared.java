package com.pelmenstar.projktSens.shared.android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.SparseIntArray;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractPreferencesThroughShared implements Preferences {
    private final String tag = getClass().getName();

    @Nullable
    private SharedPreferences.Editor sessionEditor;
    private final SparseIntArray sessionModifiedInts;

    private boolean isInitialized;

    protected SharedPreferences preferences;

    public AbstractPreferencesThroughShared() {
        int count = getPreferenceValuesCount();
        if (count < 0) {
            count = 0;
        }

        sessionModifiedInts = new SparseIntArray(count);
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
            sessionEditor = preferences.edit();
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
        sessionModifiedInts.clear();
    }

    protected final int safeGetInt(@NotNull String key, int id, int defaultValue) {
        if (sessionEditor != null) {
            int keyIndex = sessionModifiedInts.indexOfKey(id);
            if (keyIndex >= 0) {
                return sessionModifiedInts.valueAt(keyIndex);
            }
        }

        return preferences.getInt(key, defaultValue);
    }

    public final void safePutInt(@NotNull String key, int id, int value) {
        if (sessionEditor != null) {
            sessionEditor.putInt(key, value);
            sessionModifiedInts.put(id, value);
        } else {
            preferences.edit().putInt(key, value).apply();
        }
    }

    protected final boolean safeGetBoolean(@NotNull String key, int id, boolean defaultValue) {
        if (sessionEditor != null) {
            int keyIndex = sessionModifiedInts.indexOfKey(id);
            if (keyIndex >= 0) {
                return sessionModifiedInts.valueAt(keyIndex) == 1;
            }
        }

        return preferences.getBoolean(key, defaultValue);
    }


    protected final void safePutBoolean(@NotNull String key, int id, boolean value) {
        if (sessionEditor != null) {
            sessionEditor.putBoolean(key, value);
            sessionModifiedInts.put(id, value ? 1 : 0);
        } else {
            preferences.edit().putBoolean(key, value).apply();
        }
    }

    protected int getPreferenceValuesCount() {
        return -1;
    }

    protected abstract void checkIfCorrupted();

    @NotNull
    protected abstract String getPreferencesName();
}
