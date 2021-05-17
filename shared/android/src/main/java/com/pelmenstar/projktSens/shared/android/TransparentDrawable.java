package com.pelmenstar.projktSens.shared.android;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Transparent drawable
 */
public final class TransparentDrawable extends Drawable {
    @NotNull
    public static final TransparentDrawable INSTANCE = new TransparentDrawable();

    private TransparentDrawable() {}

    @Override
    public void draw(@NotNull Canvas canvas) {
    }

    @Override
    public void setAlpha(int alpha) {
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSPARENT;
    }
}
