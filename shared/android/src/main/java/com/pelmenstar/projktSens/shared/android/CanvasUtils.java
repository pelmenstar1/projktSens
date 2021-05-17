package com.pelmenstar.projktSens.shared.android;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.pelmenstar.projktSens.shared.PointL;

import org.jetbrains.annotations.NotNull;

public final class CanvasUtils {
    private CanvasUtils() {
    }

    public static void drawText(@NotNull Canvas c, @NotNull String text, long pos, @NotNull Paint paint) {
        c.drawText(text, 0, text.length(), PointL.getX(pos), PointL.getY(pos), paint);
    }

    public static void drawText(@NotNull Canvas c, @NotNull char[] text, long pos, @NotNull Paint paint) {
        c.drawText(text, 0, text.length, PointL.getX(pos), PointL.getY(pos), paint);
    }
}
