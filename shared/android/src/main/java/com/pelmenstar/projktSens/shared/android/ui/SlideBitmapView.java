package com.pelmenstar.projktSens.shared.android.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RenderNode;
import android.os.Build;
import android.util.AttributeSet;
import android.util.FloatProperty;
import android.util.Property;
import android.view.View;

import androidx.annotation.AttrRes;
import androidx.annotation.RequiresApi;
import androidx.annotation.StyleRes;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SlideBitmapView extends View {
    @NotNull
    public static final Property<SlideBitmapView, Float> OFFSET_X;

    private float offsetX;
    private int slideCoefficient = 1;

    @Nullable
    private Bitmap bitmap;

    private RenderNode renderNode;

    static {
        if (Build.VERSION.SDK_INT >= 24) {
            OFFSET_X = new FloatProperty<SlideBitmapView>("offsetX") {
                @Override
                @NotNull
                public Float get(@NotNull SlideBitmapView object) {
                    return object.offsetX;
                }

                @Override
                public void setValue(@NotNull SlideBitmapView object, float value) {
                    object.setOffsetX(value);
                }
            };
        } else {
            OFFSET_X = new Property<SlideBitmapView, Float>(Float.class, "offsetX") {
                @Override
                @NotNull
                public Float get(@NotNull SlideBitmapView object) {
                    return object.offsetX;
                }

                @Override
                public void set(@NotNull SlideBitmapView object, @NotNull Float value) {
                    object.setOffsetX(value);
                }
            };
        }
    }

    public SlideBitmapView(@NotNull Context context) {
        this(context, null, 0, 0);
    }

    public SlideBitmapView(@NotNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0, 0);
    }

    public SlideBitmapView(@NotNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SlideBitmapView(
            @NotNull Context context,
            @Nullable AttributeSet attrs,
            @AttrRes int defStyleAttr,
            @StyleRes int defStyleRes
    ) {
        super(context, attrs, defStyleAttr, defStyleRes);

        setLayerType(View.LAYER_TYPE_HARDWARE, null);
        if (Build.VERSION.SDK_INT >= 29) {
            renderNode = new RenderNode("slideBitmapView");
        }
    }

    public int getSlideCoefficient() {
        return slideCoefficient;
    }

    public void setSlideCoefficient(int value) {
        if (value <= 0) {
            throw new IllegalArgumentException("value");
        }

        this.slideCoefficient = value;
    }

    public float getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(float offsetX) {
        this.offsetX = offsetX;

        if (Build.VERSION.SDK_INT >= 29) {
            boolean needsToInvalidate = renderNode.setTranslationX(offsetX);
            if (!needsToInvalidate) {
                return;
            }
        }

        invalidate();
    }

    @Nullable
    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(@Nullable Bitmap bitmap) {
        this.bitmap = bitmap;

        if (Build.VERSION.SDK_INT >= 29) {
            invalidateRenderNode();
        }

        invalidate();
    }

    public void onBitmapChanged() {
        if (Build.VERSION.SDK_INT >= 29) {
            invalidateRenderNode();
            invalidate();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (Build.VERSION.SDK_INT >= 29) {
            boolean needsToInvalidate = renderNode.setPosition(0, 0, w * slideCoefficient, h);
            if (needsToInvalidate) {
                invalidateRenderNode();

                invalidate();
            }
        }
    }

    @RequiresApi(29)
    private void invalidateRenderNode() {
        RenderNode node = renderNode;

        Bitmap b = bitmap;
        if (b != null) {
            try {
                Canvas c = node.beginRecording();

                c.drawBitmap(b, 0f, 0f, null);
            } finally {
                node.endRecording();
            }
        }
    }

    @Override
    protected void onDraw(@NotNull Canvas c) {
        if (Build.VERSION.SDK_INT >= 29 && c.isHardwareAccelerated()) {
            c.drawRenderNode(renderNode);
        } else {
            Bitmap b = bitmap;
            if (b != null) {
                c.drawBitmap(b, offsetX, 0f, null);
            }
        }
    }
}
