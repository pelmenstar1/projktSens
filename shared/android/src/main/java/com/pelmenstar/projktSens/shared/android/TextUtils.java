package com.pelmenstar.projktSens.shared.android;

import android.graphics.Paint;
import android.graphics.Rect;

import com.pelmenstar.projktSens.shared.PackedSize;

import org.jetbrains.annotations.NotNull;

public final class TextUtils {
    @NotNull
    private static final Rect textBoundsCache = new Rect();

    private TextUtils() {
    }

    public static long getTextSize(char @NotNull [] text, @NotNull Paint paint) {
        paint.getTextBounds(text, 0, text.length, textBoundsCache);

        return packTextBoundsCache();
    }

    public static long getTextSize(@NotNull String text, @NotNull Paint paint) {
        paint.getTextBounds(text, 0, text.length(), textBoundsCache);

        return packTextBoundsCache();
    }

    private static long packTextBoundsCache() {
        Rect bounds = textBoundsCache;

        return PackedSize.create(bounds.width(), bounds.height());
    }
}
