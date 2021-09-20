package com.pelmenstar.projktSens.shared.android;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.pelmenstar.projktSens.shared.PackedPointF;

import org.jetbrains.annotations.NotNull;

public final class CanvasUtils {
    private CanvasUtils() {
    }

    public static void drawText(@NotNull Canvas c, char @NotNull [] text, long pos, @NotNull Paint paint) {
        c.drawText(text, 0, text.length, PackedPointF.getX(pos), PackedPointF.getY(pos), paint);
    }

    public static void drawTextWithOffset(
            @NotNull Canvas c,
            @NotNull String text,
            long offset, long position,
            @NotNull Paint paint
    ) {
        c.drawText(
                text,
                PackedPointF.getX(offset) + PackedPointF.getX(position),
                PackedPointF.getY(offset) + PackedPointF.getY(position),
                paint
        );
    }
}
