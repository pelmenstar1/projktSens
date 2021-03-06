package com.pelmenstar.projktSens.chartLite;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.pelmenstar.projktSens.chartLite.formatter.ValueFormatter;
import com.pelmenstar.projktSens.shared.MyMath;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

        int d = (int)Math.ceil(Math.log10(Math.abs(number)));
        int pw = 1 - d;
        float magnitude = MyMath.pow10(pw);
        float invMagnitude = MyMath.pow10(-pw);

        int shifted = Math.round(number * magnitude);

        return shifted * invMagnitude;
    }

    public static void drawAxisText(
            float @NotNull [] entries, @NotNull String @Nullable [] labels,
            @NotNull ValueFormatter valueFormatter,
            int index,
            float x, float textY,
            @NotNull Paint paint,
            @NotNull Canvas canvas
    ) {
        if(labels != null) {
            String label = labels[index];
            canvas.drawText(label, 0, label.length(), x, textY, paint);
        } else {
            char[] text = valueFormatter.formatToCharArray(entries[index]);
            canvas.drawText(text, 0, text.length, x, textY, paint);
        }
    }
}
