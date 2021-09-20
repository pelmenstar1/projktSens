package com.pelmenstar.projktSens.weather.app.ui.home.weatherView;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.pelmenstar.projktSens.weather.app.R;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RequestLocationSubcomponent extends ComplexWeatherView.Subcomponent {
    private final Drawable icon;
    private RequestLocationPermissionHandler requestLocationPermissionHandler;

    public RequestLocationSubcomponent(@NotNull Context context) {
        Resources res = context.getResources();
        Resources.Theme theme = context.getTheme();

        int iconTint = ResourcesCompat.getColor(res, R.color.weatherView_iconTint, theme);

        icon = ResourcesCompat.getDrawable(res, R.drawable.ic_error, theme);
        if (icon == null) throw new NullPointerException();

        DrawableCompat.setTint(icon, iconTint);
    }

    @Nullable
    public RequestLocationPermissionHandler getRequestLocationPermissionHandler() {
        return requestLocationPermissionHandler;
    }

    public void setRequestLocationPermissionHandler(@Nullable RequestLocationPermissionHandler listener) {
        this.requestLocationPermissionHandler = listener;
    }

    @Override
    public void draw(@NotNull Canvas c) {
        icon.draw(c);
    }

    @Override
    protected void onPositionChanged(float x, float y) {
        int ix = (int) x;
        int iy = (int) y;

        icon.setBounds(ix, iy, ix + (int) getWidth(), iy + (int) getHeight());
    }

    @Override
    protected void onSizeChanged(float width, float height) {
        int x = (int) getX();
        int y = (int) getY();

        icon.setBounds(x, y, x + (int) width, y + (int) height);
    }

    @Override
    protected void onDayStateChanged(int newState) {
    }

    @Override
    protected void onClick() {
        RequestLocationPermissionHandler handler = requestLocationPermissionHandler;
        if (handler != null) {
            handler.request();
        }
    }

    @FunctionalInterface
    public interface RequestLocationPermissionHandler {
        void request();
    }
}
