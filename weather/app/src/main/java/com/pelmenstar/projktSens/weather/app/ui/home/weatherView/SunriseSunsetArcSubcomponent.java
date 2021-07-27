package com.pelmenstar.projktSens.weather.app.ui.home.weatherView;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.RectF;

import androidx.core.content.res.ResourcesCompat;

import com.pelmenstar.projktSens.shared.PointL;
import com.pelmenstar.projktSens.shared.SizeL;
import com.pelmenstar.projktSens.shared.StringUtils;
import com.pelmenstar.projktSens.shared.android.CanvasUtils;
import com.pelmenstar.projktSens.shared.android.TextUtils;
import com.pelmenstar.projktSens.shared.time.ShortTime;
import com.pelmenstar.projktSens.shared.time.TimeConstants;
import com.pelmenstar.projktSens.shared.time.TimeInt;
import com.pelmenstar.projktSens.weather.app.R;

import org.jetbrains.annotations.NotNull;

public final class SunriseSunsetArcSubcomponent extends ComplexWeatherView.Subcomponent {
    private int sunrise = ShortTime.NONE;
    private int sunset = ShortTime.NONE;
    private int time = ShortTime.NONE;

    private float overArcAngle;

    private final Paint dashedArcPaint;
    private final Paint overArcPaint;
    private final Paint textPaint;

    private final float primaryPadding;

    private long sunrisePos;
    private long sunsetPos;

    private final char[] sunriseText;
    private final char[] sunsetText;

    private long sunriseTextSize;
    private long sunsetTextSize;

    private final RectF arcBounds = new RectF();

    public SunriseSunsetArcSubcomponent(@NotNull Context context) {
        sunriseText = new char[5];
        sunsetText = new char[5];

        // Then writeTime() should write hour and minutes, no :
        sunriseText[2] = ':';
        sunsetText[2] = ':';

        Resources res = context.getResources();
        Resources.Theme theme = context.getTheme();

        int textColor = ResourcesCompat.getColor(res, R.color.sunriseSunsetArc_textColor, theme);
        primaryPadding = res.getDimension(R.dimen.sunriseSunsetArc_primaryPadding);

        dashedArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dashedArcPaint.setColor(ResourcesCompat.getColor(res, R.color.sunriseSunsetArc_dashedArcColor, theme));
        dashedArcPaint.setStrokeWidth(res.getDimension(R.dimen.sunriseSunsetArc_dashedArcThickness));
        dashedArcPaint.setStyle(Paint.Style.STROKE);
        dashedArcPaint.setPathEffect(new DashPathEffect(new float[] { 10f, 5f }, 5f));

        overArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        overArcPaint.setColor(ResourcesCompat.getColor(res, R.color.sunriseSunsetArc_overArcColor, theme));
        overArcPaint.setStrokeWidth(res.getDimension(R.dimen.sunriseSunsetArc_overArcThickness));
        overArcPaint.setStyle(Paint.Style.STROKE);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(textColor);
        textPaint.setTextSize(res.getDimension(R.dimen.sunriseSunsetArc_textSize));

        setVisible(false);
    }

    @TimeInt
    public int getSunrise() {
        return sunrise;
    }

    @TimeInt
    public int getSunset() {
        return sunset;
    }

    public void setSunriseSunset(@TimeInt int sunrise, @TimeInt int sunset) {
        if(!ShortTime.isValid(sunrise)) {
            throw new IllegalArgumentException("sunrise");
        }

        if(!ShortTime.isValid(sunset)) {
            throw new IllegalArgumentException("sunset");
        }

        if(sunrise >= sunset) {
            throw new IllegalArgumentException("sunrise >= sunset");
        }

        this.sunrise = sunrise;
        this.sunset = sunset;

        writeTime(sunrise, sunriseText);
        writeTime(sunset, sunsetText);

        sunriseTextSize = TextUtils.getTextSize(sunriseText, textPaint);
        sunsetTextSize = TextUtils.getTextSize(sunsetText, textPaint);

        updateSunriseSunsetPositions();
        updateArcBounds();

        if(time != ShortTime.NONE) {
            updateOverArcAngle();
        }

        setVisible(true);
    }

    @TimeInt
    public int getTime() {
        return time;
    }

    public void setTime(@TimeInt int time) {
        if(!ShortTime.isValid(time)) {
            throw new IllegalArgumentException("time");
        }

        this.time = time;

        if(sunrise != ShortTime.NONE) {
            updateOverArcAngle();
        }
    }

    private void updateOverArcAngle() {
        // 1. (a / b) * k => (k * a) / b.
        //    multiplication of integer is faster than float.
        // 2. sunset != sunset by condition in setSunriseSunset(), so there can't be division by 0.
        if(getDayState() == ComplexWeatherView.STATE_DAY) {
            overArcAngle = (float) (180 * (time - sunrise)) / (float) (sunset - sunrise);
        } else {
            // (relTime / diff) * 180 => (180 * relTime) / diff

            float diff = (float)(TimeConstants.SECONDS_IN_DAY - (sunset - sunrise));
            int relTime;
            if(time > sunset) {
                relTime = time - sunset;
            } else {
                relTime = (TimeConstants.SECONDS_IN_DAY - sunset) + time;
            }

            relTime *= 180;

            overArcAngle = (float)relTime / diff;
        }
    }

    private void updateSunriseSunsetPositions() {
        float x = getX();
        float y = getY();
        float width = getWidth();
        float height = getHeight();

        float sunriseX;
        float sunriseY;

        float sunsetX;
        float sunsetY;

        float sunriseHeight = SizeL.getHeight(sunriseTextSize);
        float sunsetHeight = SizeL.getHeight(sunsetTextSize);

        float textOnRightX = width - SizeL.getWidth(sunriseTextSize) - primaryPadding;

        if(getDayState() == ComplexWeatherView.STATE_DAY) {
            sunriseX = primaryPadding;
            sunriseY = height - sunriseHeight - primaryPadding;

            sunsetX = textOnRightX;
            sunsetY = height - sunsetHeight - primaryPadding;
        } else {
            sunriseX = textOnRightX;
            sunriseY = sunriseHeight + primaryPadding;

            sunsetX = primaryPadding;
            sunsetY = sunsetHeight + primaryPadding;
        }

        sunrisePos = PointL.of(x + sunriseX, y + sunriseY);
        sunsetPos = PointL.of(x + sunsetX, y + sunsetY);
    }

    private void updateArcBounds() {
        float x = getX();
        float y = getY();
        float height = getHeight();

        float left = x + primaryPadding + SizeL.getWidth(sunriseTextSize) * 0.5f;
        float top;
        float right = (x + getWidth()) - primaryPadding - SizeL.getWidth(sunsetTextSize) * 0.5f;
        float bottom;

        float maxTextHeight = Math.max(SizeL.getHeight(sunriseTextSize), SizeL.getHeight(sunsetTextSize));

        if(getDayState() == ComplexWeatherView.STATE_DAY) {
            top = y + primaryPadding;
            bottom = (y + height) - primaryPadding - maxTextHeight;
        } else {
            top = y + primaryPadding + maxTextHeight;
            bottom = (y + height) - primaryPadding;
        }

        arcBounds.set(left, top, right, bottom);
    }

    private static void writeTime(@TimeInt int time, @NotNull char[] outText) {
        int hour = time / TimeConstants.SECONDS_IN_HOUR;
        time -= hour * TimeConstants.SECONDS_IN_HOUR;
        int minute = time / 60;

        // colon is set in constructor implicitly, so we need only write hour & minute
        StringUtils.writeTwoDigits(outText, 0, hour);
        StringUtils.writeTwoDigits(outText, 3, minute);
    }

    @Override
    public void draw(@NotNull Canvas c) {
        float invOverArcAngle = Math.abs(180f - overArcAngle);

        if(getDayState() == ComplexWeatherView.STATE_DAY) {
            c.drawArc(arcBounds, 0f, -invOverArcAngle, false, dashedArcPaint);
            c.drawArc(arcBounds, 180f, overArcAngle, false, overArcPaint);
        } else {
            c.drawArc(arcBounds, 0f, invOverArcAngle, false, dashedArcPaint);
            c.drawArc(arcBounds, 180f, -overArcAngle, false, overArcPaint);
        }

        CanvasUtils.drawText(c, sunriseText, sunrisePos, textPaint);
        CanvasUtils.drawText(c, sunsetText, sunsetPos, textPaint);
    }

    @Override
    protected void onSizeChanged(float width, float height) {
        updateArcBounds();
        updateSunriseSunsetPositions();
    }

    @Override
    protected void onPositionChanged(float x, float y) {
        updateArcBounds();
        updateSunriseSunsetPositions();
    }

    @Override
    protected void onDayStateChanged(int newState) {
        updateOverArcAngle();
        updateSunriseSunsetPositions();
        updateArcBounds();
    }

    @Override
    protected void onClick() {
    }
}
