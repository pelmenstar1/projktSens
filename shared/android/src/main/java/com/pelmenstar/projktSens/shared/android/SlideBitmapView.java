package com.pelmenstar.projktSens.shared.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.AttrRes;
import androidx.annotation.StyleRes;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SlideBitmapView extends View {
    private float offsetX;

    @Nullable
    private Bitmap bitmap;

    public SlideBitmapView(@NotNull Context context) {
        super(context);
    }

    public SlideBitmapView(@NotNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SlideBitmapView(@NotNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SlideBitmapView(
            @NotNull Context context,
            @Nullable AttributeSet attrs,
            @AttrRes int defStyleAttr,
            @StyleRes int defStyleRes
    ) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public float getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(float offsetX) {
        this.offsetX = offsetX;
        invalidate();
    }

    @Nullable
    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(@Nullable Bitmap bitmap) {
        this.bitmap = bitmap;
        invalidate();
    }

    @Override
    protected void onDraw(@NotNull Canvas c) {
        if(bitmap != null) {
            c.drawBitmap(bitmap, offsetX, 0f, null);
        }
    }
}
