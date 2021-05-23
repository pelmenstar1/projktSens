package com.pelmenstar.projktSens.weather.app;

import android.content.Context;
import android.content.res.Resources;

import com.pelmenstar.projktSens.shared.android.ui.LinearColorTransition;
import com.pelmenstar.projktSens.shared.android.ui.TransitionView;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Responsible for app initialization
 */
public final class Startup {
    // like AtomicBoolean but 0-false, 1-true
    private static final AtomicInteger initialized = new AtomicInteger();

    private Startup() {}

    /**
     * Initializes some components of the application
     */
    public static void init(@NotNull Context context) {
        // should be called only once
        if(!initialized.compareAndSet(0, 1)) {
            return;
        }

        Resources res = context.getResources();
        int[] transitionColors = res.getIntArray(R.array.defaultTransitionColors);

        TransitionView.setDefaultTransition(LinearColorTransition.multiple(transitionColors));

        Preferences prefs = Preferences.of(context);
        PreferredUnits.setUnits(prefs.getUnits());
    }
}
