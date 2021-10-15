package com.pelmenstar.projktSens.shared.android;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import androidx.annotation.ColorInt;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class RoundRectDrawable extends Drawable {
    @NotNull
    private final Paint paint;

    private final float radius;

    public RoundRectDrawable(@ColorInt int color, float radius, boolean fill) {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        paint.setStyle(fill ? Paint.Style.FILL : Paint.Style.STROKE);

        this.radius = radius;
    }

    @Override
    public void draw(@NotNull Canvas canvas) {
        Rect bounds = getBounds();

        canvas.drawRoundRect(
                (float)bounds.left, (float)bounds.top,
                (float)bounds.right, (float)bounds.bottom,
                radius, radius,
                paint
        );
    }

    @Override
    public int getAlpha() {
        return paint.getAlpha();
    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
        invalidateSelf();
    }

    @Nullable
    @Override
    public ColorFilter getColorFilter() {
        return paint.getColorFilter();
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        paint.setColorFilter(colorFilter);
        invalidateSelf();
    }

    @Override
    public int getOpacity() {
        switch (paint.getAlpha()) {
            case 0: return PixelFormat.TRANSPARENT;
            case 255: return PixelFormat.OPAQUE;
            default: return PixelFormat.TRANSLUCENT;
        }
    }
}
