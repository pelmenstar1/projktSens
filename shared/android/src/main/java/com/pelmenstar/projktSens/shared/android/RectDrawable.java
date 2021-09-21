package com.pelmenstar.projktSens.shared.android;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

public final class RectDrawable extends Drawable {
    @Nullable
    private final Paint strokePaint;

    @Nullable
    private final Paint fillPaint;

    private RectDrawable(@Nullable Paint strokePaint, @Nullable Paint fillPaint) {
        this.strokePaint = strokePaint;
        this.fillPaint = fillPaint;
    }

    @NotNull
    private static Paint createPaint(@ColorInt int color, @NotNull Paint.Style style) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        paint.setStyle(style);

        return paint;
    }

    @NotNull
    private static Paint createStrokePaint(@ColorInt int color, float strokeWidth) {
        Paint paint = createPaint(color, Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);

        return paint;
    }

    @NotNull
    public static RectDrawable onlyStroke(@ColorInt int color, float strokeWidth) {
        Paint strokePaint = createStrokePaint(color, strokeWidth);

        return new RectDrawable(strokePaint, null);
    }

    @NotNull
    public static RectDrawable onlyFill(@ColorInt int color) {
        Paint paint = createPaint(color, Paint.Style.FILL);

        return new RectDrawable(null, paint);
    }

    @NotNull
    public static RectDrawable strokeAndFill(
            @ColorInt int strokeColor, float strokeWidth,
            @ColorInt int fillColor
    ) {
        Paint strokePaint = createStrokePaint(strokeColor, strokeWidth);
        Paint fillPaint = createPaint(fillColor, Paint.Style.FILL);

        return new RectDrawable(strokePaint, fillPaint);
    }

    @Override
    public void draw(@NonNull Canvas c) {
        Rect rect = getBounds();
        float l = rect.left;
        float t = rect.top;
        float r = rect.right;
        float b = rect.bottom;

        if(fillPaint != null) {
            c.drawRect(l, t, r, b, fillPaint);
        }

        if(strokePaint != null) {
            float strokeWidth = strokePaint.getStrokeWidth();
            float halfStrokeWidth = strokeWidth * 0.5f;

            c.drawRect(
                    l + halfStrokeWidth, t + halfStrokeWidth,
                    r - strokeWidth, b - strokeWidth,
                    strokePaint
            );
        }
    }

    @Override
    public void setAlpha(int alpha) {
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }
}
