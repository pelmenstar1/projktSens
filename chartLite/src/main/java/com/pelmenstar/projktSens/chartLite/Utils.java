package com.pelmenstar.projktSens.chartLite;

import android.content.Context;
import android.content.res.Resources;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;

public final class Utils {
    private static final AtomicInteger initialized = new AtomicInteger();

    private static float density;

    private Utils() {
    }

    static void init(@NotNull Context context) {
        if (!initialized.compareAndSet(0, 1)) {
            return;
        }

        Resources res = context.getResources();
        density = res.getDisplayMetrics().density;
    }

    public static float dpToPx(float dp) {
        return dp * density;
    }

    public static float roundToNextSignificant(float number) {
        if (Float.isInfinite(number) || Float.isNaN(number) || number == 0f) {
            return 0f;
        }

        float d = (float) Math.ceil(Math.log10(Math.abs(number)));
        int pw = 1 - (int) d;
        float magnitude = (float) Math.pow(10, pw);
        int shifted = Math.round(number * magnitude);

        return shifted / magnitude;
    }
}
