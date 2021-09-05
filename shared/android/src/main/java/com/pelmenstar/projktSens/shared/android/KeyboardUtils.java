package com.pelmenstar.projktSens.shared.android;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import org.jetbrains.annotations.NotNull;

public final class KeyboardUtils {
    private KeyboardUtils() {}

    public static void hideKeyboard(@NotNull Activity activity) {
        InputMethodManager manager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);

        View focused = activity.getCurrentFocus();

        if(focused != null) {
            manager.hideSoftInputFromWindow(focused.getWindowToken(), 0);
        }
    }
}
