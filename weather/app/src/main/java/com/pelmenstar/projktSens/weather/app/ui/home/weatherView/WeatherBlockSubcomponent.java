package com.pelmenstar.projktSens.weather.app.ui.home.weatherView;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;

import androidx.core.content.res.ResourcesCompat;

import com.pelmenstar.projktSens.shared.PointL;
import com.pelmenstar.projktSens.weather.app.AppPreferences;
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

public final class WeatherBlockSubcomponent extends ComplexWeatherView.Subcomponent {
    private static final float TEMP_UNIT_MARGIN_L_DP = 4;
    private static final float TEXT_MARGIN_T_DP = 5;
    private final Paint tempPaint;
    private final Paint tempUnitPaint;
    private final Paint humPressPaint;
    private final int dayTextColor;
    private final int nightTextColor;
    private final float tempUnitMarginLeft;
    private final float textMarginTop;
    private final UnitFormatter unitFormatter;
    private final Rect textSizeBuffer = new Rect();
    private final AppPreferences preferences;
    private String tempUnitStr = "";
    private String tempStr = "";
    private String humStr = "";
    private String pressStr = "";
    private float tempStrY;
    private float humStrY;
    private float pressStrY;
    private long tempUnitStrPos;

    public WeatherBlockSubcomponent(@NotNull Context context) {
        AppComponent appComponent = DaggerAppComponent
                .builder()
                .appModule(new AppModule(context))
                .build();
        unitFormatter = appComponent.unitFormatter();
        preferences = appComponent.preferences();

        Resources res = context.getResources();
        Resources.Theme theme = context.getTheme();

        float density = res.getDisplayMetrics().density;

        tempUnitMarginLeft = TEMP_UNIT_MARGIN_L_DP * density;
        textMarginTop = TEXT_MARGIN_T_DP * density;

        dayTextColor = ResourcesCompat.getColor(res, R.color.weatherView_textColor_day, theme);
        nightTextColor = ResourcesCompat.getColor(res, R.color.weatherView_textColor_night, theme);

        float tempTextSize = res.getDimensionPixelSize(R.dimen.weatherView_tempTextSize);
        float tempUnitTextSize = res.getDimensionPixelSize(R.dimen.weatherView_tempUnitTextSize);
        float humPressTextSize = res.getDimensionPixelSize(R.dimen.weatherView_humPressTextSize);

        Typeface textTypeface = loadTextTypeface(context);

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
    }

    @NotNull
    private static Typeface loadTextTypeface(@NotNull Context context) {
        Typeface notosans = ResourcesCompat.getFont(context, R.font.notosans_light);

        if (notosans == null) {
            return Typeface.SANS_SERIF;
        }

        return notosans;
    }

    public void setWeather(@NotNull WeatherInfo value) {
        int prefUnits = preferences.getUnits();
        int prefTempUnit = ValueUnitsPacked.getTemperatureUnit(prefUnits);
        int prefPressUnit = ValueUnitsPacked.getPressureUnit(prefUnits);

        int valueUnits = value.units;
        int valueTempUnit = ValueUnitsPacked.getTemperatureUnit(valueUnits);
        int valuePressUnit = ValueUnitsPacked.getPressureUnit(valueUnits);

        tempUnitStr = unitFormatter.getUnitString(valueTempUnit);
        tempStr = Float.toString(UnitValue.getValue(value.temperature, valueTempUnit, prefTempUnit));
        humStr = unitFormatter.formatValue(value.humidity, ValueUnit.HUMIDITY);
        pressStr = unitFormatter.formatValue(UnitValue.getValue(value.pressure, valuePressUnit, prefPressUnit), prefPressUnit);

        invalidatePositions();
    }

    private void invalidatePositions() {
        tempPaint.getTextBounds(tempStr, 0, tempStr.length(), textSizeBuffer);
        float tempStrWidth = textSizeBuffer.width();
        float tempStrHeight = textSizeBuffer.height();

        tempUnitPaint.getTextBounds(tempUnitStr, 0, tempUnitStr.length(), textSizeBuffer);
        float tempUnitStrHeight = textSizeBuffer.height();

        humPressPaint.getTextBounds(humStr, 0, humStr.length(), textSizeBuffer);
        float humStrHeight = textSizeBuffer.height();

        tempStrY = tempStrHeight;
        humStrY = textMarginTop + tempStrHeight * 2;

        pressStrY = textMarginTop + humStrY + humStrHeight;
        tempUnitStrPos = PointL.of(tempStrWidth + tempUnitMarginLeft, tempUnitStrHeight);
    }

    @Override
    public void draw(@NotNull Canvas c) {
        float x = getX();
        float y = getY();

        c.drawText(tempStr, x, y + tempStrY, tempPaint);
        c.drawText(tempUnitStr, x + PointL.getX(tempUnitStrPos), y + PointL.getY(tempUnitStrPos), tempUnitPaint);
        c.drawText(humStr, x, y + humStrY, humPressPaint);
        c.drawText(pressStr, x, y + pressStrY, humPressPaint);
    }

    @Override
    protected void onSizeChanged(float width, float height) {
    }

    @Override
    protected void onPositionChanged(float x, float y) {
    }

    @Override
    protected void onDayStateChanged(int newState) {
        switch (newState) {
            case ComplexWeatherView.STATE_DAY: {
                tempPaint.setColor(dayTextColor);
                tempUnitPaint.setColor(dayTextColor);
                humPressPaint.setColor(dayTextColor);

                break;
            }
            case ComplexWeatherView.STATE_NIGHT: {
                tempPaint.setColor(nightTextColor);
                tempUnitPaint.setColor(nightTextColor);
                humPressPaint.setColor(nightTextColor);

                break;
            }
        }
    }

    @Override
    protected void onClick() {
    }
}
