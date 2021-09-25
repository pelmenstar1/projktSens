package com.pelmenstar.projktSens.weather.app.ui.home.weatherView;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;

import androidx.core.content.res.ResourcesCompat;

import com.pelmenstar.projktSens.shared.PackedPointF;
import com.pelmenstar.projktSens.shared.PackedSize;
import com.pelmenstar.projktSens.shared.StringUtils;
import com.pelmenstar.projktSens.shared.android.CanvasUtils;
import com.pelmenstar.projktSens.shared.android.TextUtils;
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
    private final Paint tempPaint;
    private final Paint tempUnitPaint;
    private final Paint humPressPaint;
    private final Paint blockPaint;
    private final Paint blockBorderPaint;

    private final float tempMarginBottom;
    private final float tempUnitMarginLeft;
    private final float textMarginTop;
    private final float roundRectRadius;
    private final float borderThickness;

    private final UnitFormatter unitFormatter;
    private final AppPreferences preferences;

    private String tempUnitStr = "";
    private String tempStr = "";
    private String humStr = "";
    private String pressStr = "";

    private long tempStrPos;
    private long humStrPos;
    private long pressStrPos;
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

        tempMarginBottom = res.getDimension(R.dimen.weatherView_block_tempBottomMargin);
        tempUnitMarginLeft = res.getDimension(R.dimen.weatherView_block_tempUnitLeftMargin);
        textMarginTop = res.getDimension(R.dimen.weatherView_block_textTopMargin);
        roundRectRadius = res.getDimension(R.dimen.weatherView_block_roundRectRadius);
        borderThickness = res.getDimension(R.dimen.weatherView_block_roundRectBorderStrokeThickness);

        int textColor = ResourcesCompat.getColor(res, R.color.weatherView_textColor, theme);
        int blockColor = ResourcesCompat.getColor(res, R.color.weatherView_blockColor, theme);
        int colorPrimary = ResourcesCompat.getColor(res, R.color.colorPrimary, theme);

        float tempTextSize = res.getDimension(R.dimen.weatherView_tempTextSize);
        float tempUnitTextSize = res.getDimension(R.dimen.weatherView_tempUnitTextSize);
        float humPressTextSize = res.getDimension(R.dimen.weatherView_humPressTextSize);

        Typeface textTypeface = loadTextTypeface(context);

        tempPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tempPaint.setTextAlign(Paint.Align.LEFT);
        tempPaint.setTypeface(textTypeface);
        tempPaint.setColor(textColor);
        tempPaint.setTextSize(tempTextSize);

        tempUnitPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tempUnitPaint.setTextAlign(Paint.Align.LEFT);
        tempUnitPaint.setTypeface(textTypeface);
        tempUnitPaint.setColor(textColor);
        tempUnitPaint.setTextSize(tempUnitTextSize);

        humPressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        humPressPaint.setTextAlign(Paint.Align.LEFT);
        humPressPaint.setTypeface(textTypeface);
        humPressPaint.setColor(textColor);
        humPressPaint.setTextSize(humPressTextSize);

        blockPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        blockPaint.setColor(blockColor);
        blockPaint.setStyle(Paint.Style.FILL);

        blockBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        blockBorderPaint.setStyle(Paint.Style.STROKE);
        blockBorderPaint.setColor(colorPrimary);
        blockBorderPaint.setStrokeWidth(borderThickness);
        blockBorderPaint.setShadowLayer(2f, 2f, 2f, ResourcesCompat.getColor(res, R.color.colorPrimaryDark, theme));
    }

    @NotNull
    private static Typeface loadTextTypeface(@NotNull Context context) {
        Typeface notosans = ResourcesCompat.getFont(context, R.font.notosans_medium);

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

        float convertedTemp = UnitValue.getValue(value.temperature, valueTempUnit, prefTempUnit);

        tempUnitStr = unitFormatter.getUnitString(prefTempUnit);
        tempStr = StringUtils.toStringRound1(convertedTemp);
        humStr = unitFormatter.formatValue(value.humidity, ValueUnit.HUMIDITY);
        pressStr = unitFormatter.formatValue(
                UnitValue.getValue(value.pressure, valuePressUnit, prefPressUnit),
                prefPressUnit
        );

        invalidatePositions();
    }

    private void invalidatePositions() {
        float width = getWidth();
        float height = getHeight();

        long tempSize = TextUtils.getTextSize(tempStr, tempPaint);
        float tempWidth = PackedSize.getWidth(tempSize);
        float tempHeight = PackedSize.getHeight(tempSize);

        long tempUnitSize = TextUtils.getTextSize(tempUnitStr, tempUnitPaint);
        float tempUnitWidth = PackedSize.getWidth(tempUnitSize);
        float tempUnitHeight = PackedSize.getHeight(tempUnitSize);

        long humSize = TextUtils.getTextSize(humStr, humPressPaint);
        float humWidth = PackedSize.getWidth(humSize);
        float humHeight = PackedSize.getHeight(humSize);

        long pressSize = TextUtils.getTextSize(pressStr, humPressPaint);
        float pressWidth = PackedSize.getWidth(pressSize);
        float pressHeight = PackedSize.getHeight(pressSize);

        float totalTextHeight = tempHeight + humHeight + pressHeight + (2 * textMarginTop);

        float tempWidthWithUnit = tempWidth + tempUnitWidth + tempUnitMarginLeft;

        float tempX = textCenterX(width, tempWidthWithUnit);

        float tempTopY = 0.5f * (height - totalTextHeight);
        float tempY = tempTopY + tempHeight;

        tempStrPos = PackedPointF.create(tempX, tempY);

        tempUnitStrPos = PackedPointF.create(
                tempX + tempWidth + tempUnitMarginLeft,
                tempTopY + tempUnitHeight
        );

        float humY = textMarginTop + tempY + humHeight + tempMarginBottom;
        humStrPos = PackedPointF.create(
                textCenterX(width, humWidth),
                humY
        );

        float pressY = textMarginTop + humY + pressHeight;
        pressStrPos = PackedPointF.create(
                textCenterX(width, pressWidth),
                pressY
        );
    }

    private static float textCenterX(float width, float textWidth) {
        return 0.5f * (width - textWidth);
    }

    @Override
    public void draw(@NotNull Canvas c) {
        float left = getX();
        float top = getY();
        float right = left + getWidth();
        float bottom = top + getHeight();

        long offset = PackedPointF.create(left, top);

        float halfThickness = borderThickness * 0.5f;

        c.drawRoundRect(
                left, top, right, bottom,
                roundRectRadius, roundRectRadius,
                blockPaint
        );

        c.drawRoundRect(
                left + halfThickness, top + halfThickness, right - borderThickness, bottom - borderThickness,
                roundRectRadius, roundRectRadius,
                blockBorderPaint
        );

        CanvasUtils.drawTextWithOffset(c, tempStr, offset, tempStrPos, tempPaint);
        CanvasUtils.drawTextWithOffset(c, tempUnitStr, offset, tempUnitStrPos, tempUnitPaint);
        CanvasUtils.drawTextWithOffset(c, humStr, offset, humStrPos, humPressPaint);
        CanvasUtils.drawTextWithOffset(c, pressStr, offset, pressStrPos, humPressPaint);
    }

    @Override
    protected void onSizeChanged(float width, float height) {
        invalidatePositions();
    }

    @Override
    protected void onPositionChanged(float x, float y) {
    }

    @Override
    protected void onDayStateChanged(int newState) {
    }

    @Override
    protected void onClick() {
    }
}
