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

        float d = (float) Math.ceil(Math.log10(Math.abs(number)));
        int pw = 1 - (int) d;
        float magnitude = (float) Math.pow(10, pw);
        int shifted = Math.round(number * magnitude);

        return shifted / magnitude;
    }
}
