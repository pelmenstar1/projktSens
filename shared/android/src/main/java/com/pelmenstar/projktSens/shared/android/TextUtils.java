package com.pelmenstar.projktSens.shared.android;

import android.graphics.Paint;
import android.graphics.Rect;

import com.pelmenstar.projktSens.shared.PackedSize;

import org.jetbrains.annotations.NotNull;

public final class TextUtils {
    private static final Rect textBoundsCache = new Rect();

    private TextUtils() {
    }

    public static long getTextSize(char @NotNull [] text, @NotNull Paint paint) {
        Rect bounds = textBoundsCache;

        paint.getTextBounds(text, 0, text.length, bounds);

        return PackedSize.create(bounds.width(), bounds.height());
    }
}
