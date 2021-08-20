package com.pelmenstar.projktSens.weather.app.ui.home.weatherView;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import androidx.core.content.res.ResourcesCompat;

import com.pelmenstar.projktSens.weather.app.R;

import org.jetbrains.annotations.NotNull;

public final class WeatherBackgroundSubcomponent extends ComplexWeatherView.Subcomponent {
    private final Drawable dayDrawable;
    private final Drawable nightDrawable;

    public WeatherBackgroundSubcomponent(@NotNull Context context) {
        Resources res = context.getResources();
        Resources.Theme theme = context.getTheme();

        dayDrawable = ResourcesCompat.getDrawable(res, R.drawable.ic_weather_view_day, theme);
        nightDrawable = ResourcesCompat.getDrawable(res, R.drawable.ic_weather_view_night, theme);
    }

    @Override
    public void draw(@NotNull Canvas c) {
        Drawable d;

        if (getDayState() == ComplexWeatherView.STATE_DAY) {
            d = dayDrawable;
        } else {
            d = nightDrawable;
        }

        d.draw(c);
    }

    @Override
    protected void onPositionChanged(float x, float y) {
        int ix = (int) x;
        int iy = (int) y;
        int right = ix + (int) getWidth();
        int bottom = iy + (int) getHeight();

        dayDrawable.setBounds(ix, iy, right, bottom);
        nightDrawable.setBounds(ix, iy, right, bottom);
    }

    @Override
    protected void onSizeChanged(float width, float height) {
        int left = (int) getX();
        int top = (int) getY();
        int right = left + (int) width;
        int bottom = top + (int) height;

        dayDrawable.setBounds(left, top, right, bottom);
        nightDrawable.setBounds(left, top, right, bottom);
    }

    @Override
    protected void onDayStateChanged(int newState) {
    }

    @Override
    protected void onClick() {
    }
}
