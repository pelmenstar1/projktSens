package com.pelmenstar.projktSens.weather.app.ui.home.weatherView;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.pelmenstar.projktSens.weather.app.R;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class RetryGetLocationSubcomponent extends ComplexWeatherView.Subcomponent {
    @FunctionalInterface
    public interface OnRetryGetLocationListener {
        void onRetry();
    }

    private static final float FAILED_GET_LOCATION_MARGIN_T_DP = 3;
    private static final float FAILED_GET_LOCATION_MARGIN_R_DP = 7;

    private final Drawable retryIcon;

    private OnRetryGetLocationListener onRetryGetLocationListener;

    private final String failedGetLocationStr;
    private final Paint failedGetLocationPaint;
    private float failedGetLocationY;
    private final float failedGetLocationHeight;
    private final float failedGetLocationMarginTop;

    private final float retrySize;

    private final int dayTextColor;
    private final int dayIconTint;

    private final int nightTextColor;
    private final int nightIconTint;

    public RetryGetLocationSubcomponent(@NotNull Context context) {
        Resources res = context.getResources();
        Resources.Theme theme = context.getTheme();
        float density = res.getDisplayMetrics().density;

        retrySize = res.getDimension(R.dimen.weatherView_retrySize);
        retryIcon = ResourcesCompat.getDrawable(res, R.drawable.ic_retry, theme);
        if(retryIcon == null) throw new NullPointerException();

        failedGetLocationStr = res.getString(R.string.failedToGetLocation);

        float failedGetLocationMarginRight = FAILED_GET_LOCATION_MARGIN_R_DP * density;
        failedGetLocationMarginTop = FAILED_GET_LOCATION_MARGIN_T_DP * density;

        dayTextColor = ResourcesCompat.getColor(res, R.color.weatherView_textColor_day, theme);
        dayIconTint = ResourcesCompat.getColor(res, R.color.weatherView_iconTint_day, theme);

        nightTextColor = ResourcesCompat.getColor(res, R.color.weatherView_textColor_night, theme);
        nightIconTint = ResourcesCompat.getColor(res, R.color.weatherView_iconTint_night, theme);

        DrawableCompat.setTint(retryIcon, dayIconTint);

        float failedGetLocationTextSize = res.getDimension(R.dimen.weatherView_failedGetLocationTextSize);

        Rect textSizeBuffer = new Rect();

        failedGetLocationPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        failedGetLocationPaint.setTypeface(loadDefaultTextTypeface(context));
        failedGetLocationPaint.setColor(dayTextColor);
        failedGetLocationPaint.setTextSize(failedGetLocationTextSize);

        failedGetLocationPaint.getTextBounds(
                failedGetLocationStr,
                0,
                failedGetLocationStr.length(),
                textSizeBuffer
        );

        failedGetLocationHeight = textSizeBuffer.height();

        float failedGetLocationWidth = textSizeBuffer.width();

        setWidth(retrySize + failedGetLocationMarginRight + failedGetLocationWidth);
        setHeight(Math.max(failedGetLocationHeight, retrySize));
    }

    @NotNull
    private Typeface loadDefaultTextTypeface(@NotNull Context context) {
        Typeface notosans = ResourcesCompat.getFont(context, R.font.notosans_light);

        if(notosans == null) {
            return Typeface.SANS_SERIF;
        }

        return notosans;
    }

    @Nullable
    public OnRetryGetLocationListener getOnRetryGetLocationListener() {
        return onRetryGetLocationListener;
    }

    public void setOnRetryGetLocationListener(@Nullable OnRetryGetLocationListener onRetryGetLocationListener) {
        this.onRetryGetLocationListener = onRetryGetLocationListener;
    }

    @Override
    public void draw(@NotNull Canvas c) {
        c.drawText(failedGetLocationStr, getX(), failedGetLocationY, failedGetLocationPaint);
        retryIcon.draw(c);
    }

    @Override
    protected void onSizeChanged(float width, float height) {
    }

    @Override
    protected void onPositionChanged(float x, float y) {
        int ix = (int)x;
        int iy = (int)y;
        int width = (int)getWidth();
        int iRetrySize = (int)retrySize;
        int right = ix + width;

        retryIcon.setBounds(right - iRetrySize, iy, right, iy + iRetrySize);
        failedGetLocationY = y + failedGetLocationMarginTop + failedGetLocationHeight;
    }

    @Override
    protected void onDayStateChanged(int newState) {
        if(newState == ComplexWeatherView.STATE_DAY) {
            failedGetLocationPaint.setColor(dayTextColor);
            DrawableCompat.setTint(retryIcon, dayIconTint);
        } else {
            failedGetLocationPaint.setColor(nightTextColor);
            DrawableCompat.setTint(retryIcon, nightIconTint);
        }
    }

    @Override
    protected void onClick() {
        OnRetryGetLocationListener listener = onRetryGetLocationListener;
        if (listener != null) {
            listener.onRetry();
        }
    }
}
