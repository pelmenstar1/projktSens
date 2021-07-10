package com.pelmenstar.projktSens.weather.app.ui.home;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.content.res.ResourcesCompat;

import com.pelmenstar.projktSens.shared.PointL;
import com.pelmenstar.projktSens.shared.StringUtils;
import com.pelmenstar.projktSens.shared.android.CanvasUtils;
import com.pelmenstar.projktSens.shared.time.ShortTime;
import com.pelmenstar.projktSens.shared.time.TimeConstants;
import com.pelmenstar.projktSens.shared.time.TimeInt;
import com.pelmenstar.projktSens.weather.app.PreferredUnits;
import com.pelmenstar.projktSens.weather.app.R;
import com.pelmenstar.projktSens.weather.app.di.AppComponent;
import com.pelmenstar.projktSens.weather.app.di.AppModule;
import com.pelmenstar.projktSens.weather.app.di.DaggerAppComponent;
import com.pelmenstar.projktSens.weather.app.formatters.UnitFormatter;
import com.pelmenstar.projktSens.weather.models.UnitValue;
import com.pelmenstar.projktSens.weather.models.ValueUnit;
import com.pelmenstar.projktSens.weather.models.ValueUnitsPacked;
import com.pelmenstar.projktSens.weather.models.WeatherInfo;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ComplexWeatherView extends View {
    @FunctionalInterface
    public interface OnRetryGetLocationListener {
        void onRetry();
    }

    private static final String TAG = "ComplexWeatherView";

    private static final float TEMP_UNIT_MARGIN_L_DP = 4;
    private static final float WEATHER_BLOCK_MARGIN_T_DP = 5;
    private static final float WEATHER_BLOCK_MARGIN_L_DP = 5;
    private static final float WEATHER_BLOCK_TEXT_MARGIN_T_DP = 5;
    private static final float SUNRISE_SUNSET_SIDE_MARGIN_DP = 5;

    private static final float FAILED_GET_LOCATION_MARGIN_T_DP = 3;
    private static final float FAILED_GET_LOCATION_MARGIN_R_DP = 7;

    private static final float MOON_RATIO_X = 0.438095f;
    private static final float MOON_RATIO_Y = 0.0434782f;
    private static final float SUNRISE_SUNSET_RATIO_Y = 0.6f;

    private static final int STATE_DAY = 0;
    private static final int STATE_NIGHT = 1;

    @NotNull
    private final Drawable dayDrawable;

    @NotNull
    private final Drawable nightDrawable;

    private final Drawable retryDrawable;

    private final Bitmap moon;
    private final Canvas moonCanvas;
    private final float moonDiameter;
    private final Path moonInvisiblePartPath = new Path();

    private int sunrise = ShortTime.NONE;
    private int sunset = ShortTime.NONE;

    private final char[] sunriseText = new char[5];
    private final char[] sunsetText = new char[5];
    private float sunsetX;

    private String tempUnitStr = "";
    private String tempStr = "";
    private String humStr = "";
    private String pressStr = "";

    private final String failedGetLocationStr;
    private final float failedGetLocationWidth;

    private long tempStrPos;
    private long humStrPos;
    private long pressStrPos;
    private long tempUnitStrPos;
    private long failedGetLocationPos;

    private final float weatherBlockMarginTop;
    private final float weatherBlockMarginLeft;
    private final float weatherBlockTextMarginTop;
    private final float tempUnitMarginLeft;
    private final float sunriseSunsetSideMargin;
    private final float failedGetLocationMarginRight;

    private final Paint moonVisiblePartPaint;
    private final Paint tempPaint;
    private final Paint tempUnitPaint;
    private final Paint humPressPaint;
    private final Paint sunriseSunsetPaint;
    private final Paint failedGetLocationPaint;

    private final UnitFormatter unitFormatter;

    private final int dayTextColor;
    private final int nightTextColor;

    private int state;

    private OnRetryGetLocationListener onRetryGetLocationListener;
    private final int retrySize;
    private boolean isLocationLoaded;

    private final Rect textSizeBuffer = new Rect();

    public ComplexWeatherView(@NotNull Context context) {
        this(context, null, 0, 0);
    }

    public ComplexWeatherView(@NotNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0, 0);
    }

    public ComplexWeatherView(@NotNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ComplexWeatherView(@NotNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        AppComponent component = DaggerAppComponent
                .builder()
                .appModule(new AppModule(context))
                .build();
        unitFormatter = component.unitFormatter();

        Resources res = context.getResources();
        Resources.Theme theme = context.getTheme();
        float density = res.getDisplayMetrics().density;

        failedGetLocationStr = res.getString(R.string.failedToGetLocation);

        weatherBlockMarginTop = WEATHER_BLOCK_MARGIN_T_DP * density;
        tempUnitMarginLeft = TEMP_UNIT_MARGIN_L_DP * density;
        weatherBlockTextMarginTop = WEATHER_BLOCK_TEXT_MARGIN_T_DP * density;
        sunriseSunsetSideMargin = SUNRISE_SUNSET_SIDE_MARGIN_DP * density;
        weatherBlockMarginLeft = WEATHER_BLOCK_MARGIN_L_DP * density;
        failedGetLocationMarginRight = FAILED_GET_LOCATION_MARGIN_R_DP * density;

        float tempTextSize = res.getDimensionPixelSize(R.dimen.weatherView_tempTextSize);
        float tempUnitTextSize = res.getDimensionPixelSize(R.dimen.weatherView_tempUnitTextSize);
        float humPressTextSize = res.getDimensionPixelSize(R.dimen.weatherView_humPressTextSize);
        float sunriseSunsetTextSize = res.getDimensionPixelSize(R.dimen.weatherView_sunriseSunsetTextSize);
        float failedGetLocationTextSize = res.getDimensionPixelSize(R.dimen.weatherView_failedGetLocationTextSize);

        Drawable dayDr = ResourcesCompat.getDrawable(res, R.drawable.ic_weather_view_day, theme);
        Drawable nightDr = ResourcesCompat.getDrawable(res, R.drawable.ic_weather_view_night, theme);

        if(dayDr == null || nightDr == null) {
            throw new NullPointerException("dayDrawable or nightDrawable is null");
        }

        dayDrawable = dayDr;
        nightDrawable = nightDr;

        retrySize = res.getDimensionPixelSize(R.dimen.weatherView_retrySize);
        retryDrawable = ResourcesCompat.getDrawable(res, R.drawable.ic_retry, theme);
        if(retryDrawable == null) {
            throw new NullPointerException();
        }

        int moonDiameter = res.getDimensionPixelSize(R.dimen.weatherView_moonDiameter);
        this.moonDiameter = (float)moonDiameter;

        int moonVisibleColor = ResourcesCompat.getColor(res, R.color.moonVisibleColor, theme);
        dayTextColor = ResourcesCompat.getColor(res, R.color.weatherView_textColor_day, theme);
        nightTextColor = ResourcesCompat.getColor(res, R.color.weatherView_textColor_night, theme);

        Typeface textTypeface = loadDefaultTextTypeface(context);

        moon = Bitmap.createBitmap(moonDiameter, moonDiameter, Bitmap.Config.ARGB_8888);
        moonCanvas = new Canvas(moon);

        moonVisiblePartPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        moonVisiblePartPaint.setColor(moonVisibleColor);
        moonVisiblePartPaint.setStyle(Paint.Style.FILL);

        tempPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tempPaint.setTextAlign(Paint.Align.LEFT);
        tempPaint.setTypeface(textTypeface);
        tempPaint.setColor(dayTextColor);
        tempPaint.setTextSize(tempTextSize);

        tempUnitPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tempUnitPaint.setTextAlign(Paint.Align.LEFT);
        tempUnitPaint.setTypeface(textTypeface);
        tempUnitPaint.setColor(dayTextColor);
        tempUnitPaint.setTextSize(tempUnitTextSize);

        humPressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        humPressPaint.setTextAlign(Paint.Align.LEFT);
        humPressPaint.setTypeface(textTypeface);
        humPressPaint.setColor(dayTextColor);
        humPressPaint.setTextSize(humPressTextSize);

        sunriseSunsetPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        sunriseSunsetPaint.setTextAlign(Paint.Align.LEFT);
        sunriseSunsetPaint.setTypeface(textTypeface);
        sunriseSunsetPaint.setColor(dayTextColor);
        sunriseSunsetPaint.setTextSize(sunriseSunsetTextSize);

        failedGetLocationPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        failedGetLocationPaint.setTypeface(textTypeface);
        failedGetLocationPaint.setColor(dayTextColor);
        failedGetLocationPaint.setTextSize(failedGetLocationTextSize);

        failedGetLocationPaint.getTextBounds(
                failedGetLocationStr,
                0,
                failedGetLocationStr.length(),
                textSizeBuffer
        );
        failedGetLocationWidth = textSizeBuffer.width();
        float fgPosY = FAILED_GET_LOCATION_MARGIN_T_DP * density + textSizeBuffer.height();

        failedGetLocationPos = PointL.of(0, fgPosY);
    }

    private void setState(int state) {
        if(this.state != state) {
            this.state = state;

            switch (state) {
                case STATE_DAY: {
                    tempPaint.setColor(dayTextColor);
                    tempUnitPaint.setColor(dayTextColor);
                    humPressPaint.setColor(dayTextColor);
                    sunriseSunsetPaint.setColor(dayTextColor);
                    failedGetLocationPaint.setColor(dayTextColor);
                }
                case STATE_NIGHT: {
                    tempPaint.setColor(nightTextColor);
                    tempUnitPaint.setColor(nightTextColor);
                    humPressPaint.setColor(nightTextColor);
                    sunriseSunsetPaint.setColor(nightTextColor);
                    failedGetLocationPaint.setColor(nightTextColor);
                }
            }

            invalidate();
        }
    }

    @NotNull
    private Typeface loadDefaultTextTypeface(@NotNull Context context) {
        Typeface notosans = ResourcesCompat.getFont(context, R.font.notosans_light);

        if(notosans == null) {
            return Typeface.SANS_SERIF;
        }

        return notosans;
    }

    public void setTime(@TimeInt int time) {
        int alignedSunrise = sunrise == ShortTime.NONE ? 8 * TimeConstants.SECONDS_IN_DAY : sunrise;
        int alignedSunset = sunset == ShortTime.NONE ? 20 * TimeConstants.SECONDS_IN_DAY : sunset;

        if(time >= alignedSunrise && time <= alignedSunset) {
            setState(STATE_DAY);
        } else {
            setState(STATE_NIGHT);
        }
    }

    public void setSunriseSunset(int sunrise, int sunset) {
        if(!ShortTime.isValid(sunrise) || !ShortTime.isValid(sunset)) {
            throw new IllegalArgumentException("sunriseSunset");
        }

        this.sunrise = sunrise;
        this.sunset = sunset;

        writeTime(sunrise, sunriseText);
        writeTime(sunset, sunsetText);

        computeSunriseSunsetPositions();
        invalidate();
    }

    public boolean isLocationLoaded() {
        return isLocationLoaded;
    }

    public void setLocationLoaded(boolean value) {
        isLocationLoaded = value;
        invalidate();
    }

    @Nullable
    public OnRetryGetLocationListener getOnRetryGetLocationListener() {
        return onRetryGetLocationListener;
    }

    public void setOnRetryGetLocationListener(@Nullable OnRetryGetLocationListener listener) {
        onRetryGetLocationListener = listener;
    }

    private void computeSunriseSunsetPositions() {
        sunriseSunsetPaint.getTextBounds(sunsetText, 0, sunsetText.length, textSizeBuffer);
        float sunsetWidth = textSizeBuffer.width();

        sunsetX = (float)getWidth() - sunsetWidth - sunriseSunsetSideMargin;
    }

    private static void writeTime(int time, @NotNull char[] text) {
        int hour = time / 3600;
        time -= hour * 3600;
        int minute = time / 60;

        StringUtils.writeTwoDigits(text, 0, hour);
        text[2] = ':';
        StringUtils.writeTwoDigits(text, 3, minute);
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

        invalidate();
    }

    public void setWeather(@NotNull WeatherInfo value) {
        int prefUnits = PreferredUnits.getUnits();
        int prefTempUnit = ValueUnitsPacked.getTemperatureUnit(prefUnits);
        int prefPressUnit = ValueUnitsPacked.getPressureUnit(prefUnits);

        int valueUnits = value.units;
        int valueTempUnit = ValueUnitsPacked.getTemperatureUnit(valueUnits);
        int valuePressUnit = ValueUnitsPacked.getPressureUnit(valueUnits);

        tempUnitStr = unitFormatter.getUnitString(valueTempUnit);
        tempStr = Float.toString(UnitValue.getValue(value.temperature, valueTempUnit, prefTempUnit));
        humStr = unitFormatter.formatValue(value.humidity, ValueUnit.HUMIDITY);
        pressStr = unitFormatter.formatValue(UnitValue.getValue(value.pressure, valuePressUnit, prefPressUnit), prefPressUnit);

        computeWeatherBlockPositions();
        invalidate();
    }

    private void computeWeatherBlockPositions() {
        tempPaint.getTextBounds(tempStr, 0, tempStr.length(), textSizeBuffer);
        float tempStrWidth = textSizeBuffer.width();
        float tempStrHeight = textSizeBuffer.height();

        tempUnitPaint.getTextBounds(tempUnitStr, 0, tempUnitStr.length(), textSizeBuffer);
        float tempUnitStrHeight = textSizeBuffer.height();

        float humStrY = weatherBlockMarginTop + weatherBlockTextMarginTop + tempStrHeight * 2;

        humPressPaint.getTextBounds(humStr, 0, humStr.length(), textSizeBuffer);
        float humStrHeight = textSizeBuffer.height();

        tempStrPos = PointL.of(weatherBlockMarginLeft, weatherBlockMarginTop + tempStrHeight);
        tempUnitStrPos = PointL.of(weatherBlockMarginLeft + tempStrWidth + tempUnitMarginLeft, weatherBlockMarginTop + tempUnitStrHeight);

        humStrPos = PointL.of(weatherBlockMarginLeft, humStrY);
        pressStrPos = PointL.of(weatherBlockMarginLeft,  weatherBlockMarginTop + weatherBlockTextMarginTop + humStrY + humStrHeight);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        computeSunriseSunsetPositions();

        dayDrawable.setBounds(0, 0, w, h);
        nightDrawable.setBounds(0, 0, w, h);

        float retryX = w - retrySize;

        // top left border
        retryDrawable.setBounds(w - retrySize, 0, w, retrySize);

        float fgLocX = retryX - failedGetLocationWidth - failedGetLocationMarginRight;

        failedGetLocationPos = PointL.withX(failedGetLocationPos, fgLocX);
    }

    @Override
    protected void onDraw(@NotNull Canvas c) {
        super.onDraw(c);

        float width = (float)getWidth();
        float height = (float)getHeight();

        switch (state) {
            case STATE_DAY: {
                dayDrawable.draw(c);

                float sunriseSunsetY = SUNRISE_SUNSET_RATIO_Y * height;

                c.drawText(sunriseText, 0, sunriseText.length, sunriseSunsetSideMargin, sunriseSunsetY, sunriseSunsetPaint);
                c.drawText(sunsetText, 0, sunsetText.length, sunsetX, sunriseSunsetY, sunriseSunsetPaint);

                break;
            }
            case STATE_NIGHT: {
                nightDrawable.draw(c);

                float moonX = MOON_RATIO_X * width;
                float moonY = MOON_RATIO_Y * height;

                c.drawBitmap(moon, moonX, moonY, null);

                break;
            }
        }

        renderWeatherBlock(c);

        if(!isLocationLoaded) {
            CanvasUtils.drawText(c, failedGetLocationStr, failedGetLocationPos, failedGetLocationPaint);
            retryDrawable.draw(c);
        }
    }

    @Override
    public boolean onTouchEvent(@NotNull MotionEvent event) {
        int action = event.getActionMasked();

        if (action == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();

            float retryX = getWidth() - retrySize;
            if(!isLocationLoaded && (x >= retryX && y <= retrySize)) {
                OnRetryGetLocationListener listener = onRetryGetLocationListener;
                if(listener != null) {
                    listener.onRetry();
                }
            }
        }

        return true;
    }

    private void renderWeatherBlock(@NotNull Canvas c) {
        CanvasUtils.drawText(c, tempStr, tempStrPos, tempPaint);
        CanvasUtils.drawText(c, tempUnitStr, tempUnitStrPos, tempUnitPaint);
        CanvasUtils.drawText(c, humStr, humStrPos, humPressPaint);
        CanvasUtils.drawText(c, pressStr, pressStrPos, humPressPaint);
    }
}
