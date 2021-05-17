package com.pelmenstar.projktSens.weather.app.ui.moon;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.AttrRes;
import androidx.annotation.StyleRes;
import androidx.core.content.res.ResourcesCompat;

import com.pelmenstar.projktSens.weather.app.R;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class MoonView extends View {
    private final Paint visiblePartPaint;

    private final Path invisiblePartPath = new Path();
    private float moonPhase = 0f;

    public MoonView(@NotNull Context context) {
        this(context, null, 0, 0);
    }

    public MoonView(@NotNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0, 0);
    }

    public MoonView(@NotNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MoonView(@NotNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        Resources res = context.getResources();
        Resources.Theme theme = context.getTheme();

        int visiblePartColor = ResourcesCompat.getColor(res, R.color.moonVisibleColor, theme);

        visiblePartPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        visiblePartPaint.setColor(visiblePartColor);
        visiblePartPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(@NotNull Canvas c) {
        float w = (float)getWidth();
        float h = (float)getHeight();

        int checkpoint = c.save();
        c.clipPath(invisiblePartPath, Region.Op.DIFFERENCE);
        c.drawOval(0f, 0f, w, h, visiblePartPaint);
        c.restoreToCount(checkpoint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if(w != 0 && h != 0) {
            float fw = (float) w;
            float fh = (float) h;

            updateInvisiblePart(fw, fh);
            invalidate();
        }
    }

    public float getMoonPhase() {
        return moonPhase;
    }

    public void setMoonPhase(float phase) {
        moonPhase = phase;

        updateInvisiblePart(getWidth(), getHeight());
        invalidate();
    }

    private void updateInvisiblePart(float w, float h) {
        invisiblePartPath.rewind();
        invisiblePartPath.addOval(moonPhase * w, 0f, w, h, Path.Direction.CCW);
    }
}
