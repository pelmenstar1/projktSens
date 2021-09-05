package com.pelmenstar.projktSens.chartLite;

import android.content.Context;
import android.content.res.Resources;

import org.jetbrains.annotations.NotNull;

public final class Utils {
    private Utils() {
    }

    public static float dpToPx(@NotNull Context context, float dp) {
        return dpToPx(context.getResources(), dp);
    }

    public static float dpToPx(@NotNull Resources resources, float dp) {
        return dp * resources.getDisplayMetrics().density;
    }

    public static float roundToNextSignificant(float number) {
        if (Float.isInfinite(number) || Float.isNaN(number) || number == 0f) {
            return 0f;
        }

        double d = Math.ceil(Math.log10(Math.abs(number)));
        double pw = 1.0 - d;
        float magnitude = (float) Math.pow(10.0, pw);
        int shifted = Math.round(number * magnitude);

        return shifted / magnitude;
    }
}
