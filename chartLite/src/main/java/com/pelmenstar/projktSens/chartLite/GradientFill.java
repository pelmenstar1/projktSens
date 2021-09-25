package com.pelmenstar.projktSens.chartLite;

import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;

import androidx.annotation.ColorInt;
import androidx.annotation.IntDef;

import com.pelmenstar.projktSens.shared.PackedPointF;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public final class GradientFill {
    @Target({ ElementType.FIELD, ElementType.LOCAL_VARIABLE, ElementType.METHOD, ElementType.PARAMETER })
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({VERTICAL, HORIZONTAL})
    public @interface Orientation {
    }

    public static final int VERTICAL = 0;
    public static final int HORIZONTAL = 1;

    private static final float[] ZERO_ONE_POSITIONS = new float[] { 0f, 1f };

    public static final GradientFill TRANSPARENT = new GradientFill(
            Color.TRANSPARENT, Color.TRANSPARENT, VERTICAL
    );

    private final int @NotNull [] colors;
    private final int orientation;

    private final float @NotNull [] positions;
    private final boolean isTransparent;

    @Nullable
    private LinearGradient shader;

    private long shaderP0;
    private long shaderP1;

    public GradientFill(@ColorInt int startColor, @ColorInt int endColor, int orientation) {
        this.colors = new int[] { startColor, endColor };
        this.positions = ZERO_ONE_POSITIONS;
        this.orientation = orientation;

        isTransparent = Color.alpha(startColor | endColor) == 0;
    }

    public GradientFill(int @NotNull [] colors, float @NotNull [] positions, int orientation) {
        this.colors = colors;
        this.positions = positions;
        this.orientation = orientation;

        isTransparent = isAllColorsTransparent(colors);
    }

    private static boolean isAllColorsTransparent(int @NotNull [] colors) {
        int merged = 0;
        for(int color: colors) {
            merged |= color;
        }

        return Color.alpha(merged) == 0;
    }

    public boolean isTransparent() {
        return isTransparent;
    }

    public int @NotNull [] getColors() {
        return colors;
    }

    public float @NotNull [] getPositions() {
        return positions;
    }

    @Orientation
    public int getOrientation() {
        return orientation;
    }

    public void applyToPaint(@NotNull RectF bounds, @NotNull Paint paint) {
        applyToPaint(bounds.left, bounds.top, bounds.right, bounds.bottom, paint);
    }

    public void applyToPaint(float left, float top, float right, float bottom, @NotNull Paint paint) {
        float x0, y0;
        float x1, y1;

        if (orientation == VERTICAL) {
            x0 = x1 = left;
            y0 = top;
            y1 = bottom;
        } else {
            y0 = y1 = top;
            x0 = left;
            x1 = right;
        }

        long oldShaderP0 = shaderP0;
        long oldShaderP1 = shaderP1;

        shaderP0 = PackedPointF.create(x0, y0);
        shaderP1 = PackedPointF.create(x1, y1);

        if(oldShaderP0 != shaderP0 || oldShaderP1 != shaderP1) {
            shader = new LinearGradient(
                    x0, y0, x1, y1,
                    colors, positions,
                    Shader.TileMode.CLAMP
            );
        }

        paint.setShader(shader);
    }
}
