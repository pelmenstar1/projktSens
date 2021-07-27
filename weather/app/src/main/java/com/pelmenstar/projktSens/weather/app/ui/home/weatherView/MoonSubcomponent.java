package com.pelmenstar.projktSens.weather.app.ui.home.weatherView;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.Region;
import android.util.Log;

import androidx.core.content.res.ResourcesCompat;

import com.pelmenstar.projktSens.weather.app.R;

import org.jetbrains.annotations.NotNull;

public final class MoonSubcomponent extends ComplexWeatherView.Subcomponent {
    private static final String TAG = "MoonSubcomponent";

    private final Bitmap moon;
    private final Canvas moonCanvas;
    private final float moonDiameter;
    private final Path moonInvisiblePartPath = new Path();

    private final Paint moonVisiblePartPaint;

    public MoonSubcomponent(@NotNull Context context) {
        Resources res = context.getResources();
        Resources.Theme theme = context.getTheme();

        int moonDiameter = res.getDimensionPixelSize(R.dimen.weatherView_moonDiameter);
        this.moonDiameter = (float)moonDiameter;

        int moonVisibleColor = ResourcesCompat.getColor(res, R.color.moonVisibleColor, theme);

        moon = Bitmap.createBitmap(moonDiameter, moonDiameter, Bitmap.Config.ARGB_8888);
        moonCanvas = new Canvas(moon);

        moonVisiblePartPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        moonVisiblePartPaint.setColor(moonVisibleColor);
        moonVisiblePartPaint.setStyle(Paint.Style.FILL);
    }

    public void setMoonPhase(float phase) {
        moonInvisiblePartPath.rewind();
        moonInvisiblePartPath.addOval(phase * moonDiameter, 0f, moonDiameter + 1, moonDiameter + 1, Path.Direction.CCW);

        Canvas c = moonCanvas;
        c.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        int checkpoint = c.save();
        c.clipPath(moonInvisiblePartPath, Region.Op.DIFFERENCE);
        c.drawOval(0f, 0f, moonDiameter, moonDiameter, moonVisiblePartPaint);
        c.restoreToCount(checkpoint);
    }

    @Override
    public void draw(@NotNull Canvas c) {
        c.drawBitmap(moon, getX(), getY(), null);
    }

    @Override
    protected void onSizeChanged(float width, float height) {
        Log.e(TAG, "onSizeChanged() called on MoonSubcomponent");
    }

    @Override
    protected void onPositionChanged(float x, float y) {
    }

    @Override
    protected void onDayStateChanged(int newState) {
        setVisible(newState == ComplexWeatherView.STATE_NIGHT);
    }

    @Override
    protected void onClick() {
    }
}
