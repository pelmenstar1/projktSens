package com.pelmenstar.projktSens.chartLite.components;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;

import androidx.annotation.ColorInt;

import com.pelmenstar.projktSens.chartLite.Utils;
import com.pelmenstar.projktSens.chartLite.formatter.FloatValueFormatter;
import com.pelmenstar.projktSens.chartLite.formatter.ValueFormatter;
import com.pelmenstar.projktSens.shared.EmptyArray;
import com.pelmenstar.projktSens.shared.MyMath;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Base-class of all axes (previously called labels).
 */
public abstract class AxisBase {
    protected static final int FLAG_GRANULARITY_ENABLED = 1;
    protected static final int FLAG_FORCE_LABELS = 1 << 1;
    protected static final int FLAG_DRAW_GRID_LINES = 1 << 2;
    protected static final int FLAG_DRAW_AXIS_LINE = 1 << 3;
    protected static final int FLAG_DRAW_LABELS = 1 << 4;
    protected static final int FLAG_CUSTOM_AXIS_MIN = 1 << 5;
    protected static final int FLAG_CUSTOM_AXIS_MAX = 1 << 6;
    protected static final int FLAG_ENABLED = 1 << 7;

    public float @NotNull [] entries = EmptyArray.FLOAT;
    public long entriesHash = 0;

    public @NotNull String @Nullable [] labels = EmptyArray.STRING;

    @NotNull
    protected ValueFormatter valueFormatter = FloatValueFormatter.INSTANCE;

    protected float granularity = 1f;
    protected float spaceMin = 0f;
    protected float spaceMax = 0f;
    protected float min = 0f;
    protected float max = 0f;
    protected float xOffset;
    protected float yOffset;

    @Nullable
    protected Typeface typeface = null;
    protected float textSize;
    @ColorInt
    protected int textColor = Color.BLACK;

    private int flags = FLAG_ENABLED | FLAG_DRAW_GRID_LINES | FLAG_DRAW_AXIS_LINE | FLAG_DRAW_LABELS;

    @ColorInt
    private int gridColor = Color.GRAY;
    private float gridLineWidth = 1f;

    @ColorInt
    private int axisLineColor = Color.GRAY;
    private float axisLineWidth = 1f;

    private int labelCount = 6;
    private int minLabels = 2;
    private int maxLabels = 25;

    public AxisBase(@NotNull Context context) {
        Resources res = context.getResources();

        textSize = Utils.dpToPx(res, 10f);
        xOffset = Utils.dpToPx(res, 2f);
        yOffset = Utils.dpToPx(res, 2f);
    }

    public float getXOffset() {
        return xOffset;
    }

    public void setXOffset(float xOffset) {
        this.xOffset = xOffset;
    }

    public float getYOffset() {
        return yOffset;
    }

    public void setYOffset(float yOffset) {
        this.yOffset = yOffset;
    }

    @Nullable
    public Typeface getTypeface() {
        return typeface;
    }

    public void setTypeface(@Nullable Typeface tf) {
        typeface = tf;
    }

    public float getTextSize() {
        return textSize;
    }

    public void setTextSize(float size) {
        if (size > 24f)
            size = 24f;
        if (size < 6f)
            size = 6f;

        textSize = size;
    }

    @ColorInt
    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(@ColorInt int color) {
        textColor = color;
    }

    public boolean isEnabled() {
        return isFlagEnabled(FLAG_ENABLED);
    }

    public void setEnabled(boolean enabled) {
        setFlag(FLAG_ENABLED, enabled);
    }

    public float getMin() {
        return min;
    }

    public float getMax() {
        return max;
    }

    protected boolean isFlagEnabled(int flag) {
        return (flags & flag) != 0;
    }

    protected void setFlag(int flag, boolean state) {
        if (state) {
            flags |= flag;
        } else {
            flags &= ~flag;
        }
    }

    public int getMinLabels() {
        return minLabels;
    }

    public void setMinLabels(int labels) {
        minLabels = labels;
    }

    public int getMaxLabels() {
        return maxLabels;
    }

    public void setMaxLabels(int labels) {
        maxLabels = labels;
    }

    public void setDrawGridLines(boolean enabled) {
        setFlag(FLAG_DRAW_GRID_LINES, enabled);
    }

    public boolean isDrawGridLinesEnabled() {
        return isFlagEnabled(FLAG_DRAW_GRID_LINES);
    }

    public void setDrawAxisLine(boolean enabled) {
        setFlag(FLAG_DRAW_AXIS_LINE, enabled);
    }

    public boolean isDrawAxisLineEnabled() {
        return isFlagEnabled(FLAG_DRAW_AXIS_LINE);
    }

    @ColorInt
    public int getGridColor() {
        return gridColor;
    }

    public void setGridColor(@ColorInt int color) {
        gridColor = color;
    }

    public float getAxisLineWidth() {
        return axisLineWidth;
    }

    public void setAxisLineWidth(float width) {
        axisLineWidth = width;
    }

    public float getGridLineWidth() {
        return gridLineWidth;
    }

    public void setGridLineWidth(float width) {
        gridLineWidth = width;
    }

    @ColorInt
    public int getAxisLineColor() {
        return axisLineColor;
    }

    public void setAxisLineColor(@ColorInt int color) {
        axisLineColor = color;
    }

    public void setDrawLabels(boolean enabled) {
        setFlag(FLAG_DRAW_LABELS, enabled);
    }

    public boolean isDrawLabelsEnabled() {
        return isFlagEnabled(FLAG_DRAW_LABELS);
    }

    public void setLabelCount(int count, boolean force) {
        setLabelCount(count);

        setFlag(FLAG_FORCE_LABELS, force);
    }

    public boolean isForceLabelsEnabled() {
        return isFlagEnabled(FLAG_FORCE_LABELS);
    }

    public int getLabelCount() {
        return labelCount;
    }

    public void setLabelCount(int count) {
        labelCount = MyMath.clamp(count, minLabels, maxLabels);

        flags &= ~FLAG_FORCE_LABELS;
    }

    public boolean isGranularityEnabled() {
        return isFlagEnabled(FLAG_GRANULARITY_ENABLED);
    }

    public void setGranularityEnabled(boolean enabled) {
        setFlag(FLAG_GRANULARITY_ENABLED, enabled);
    }

    public float getGranularity() {
        return granularity;
    }

    public void setGranularity(float granularity) {
        this.granularity = granularity;
        // set this to true if it was disabled, as it makes no sense to call this method with granularity disabled

        flags |= FLAG_GRANULARITY_ENABLED;
    }

    @NotNull
    public ValueFormatter getValueFormatter() {
        return valueFormatter;
    }

    public void setValueFormatter(@NotNull ValueFormatter f) {
        valueFormatter = f;

        if(f.supportsFormattingToCharArray()) {
            labels = null;
        } else {
            if (labels == null || labels.length != entries.length) {
                labels = new String[entries.length];
            }

            for (int i = 0; i < entries.length; i++) {
                labels[i] = f.formatToString(entries[i]);
            }
        }
    }

    public float getAxisMaximum() {
        return max;
    }

    public void setAxisMaximum(float max) {
        this.max = max;
        flags |= FLAG_CUSTOM_AXIS_MAX;
    }

    public float getAxisMinimum() {
        return min;
    }

    public void setAxisMinimum(float min) {
        this.min = min;
        flags |= FLAG_CUSTOM_AXIS_MIN;
    }

    public void resetAxisMaximum() {
        flags &= ~FLAG_CUSTOM_AXIS_MAX;
    }

    public boolean isAxisMaxCustom() {
        return isFlagEnabled(FLAG_CUSTOM_AXIS_MAX);
    }

    public void resetAxisMinimum() {
        flags &= ~FLAG_CUSTOM_AXIS_MIN;
    }

    public boolean isAxisMinCustom() {
        return isFlagEnabled(FLAG_CUSTOM_AXIS_MIN);
    }

    public float getSpaceMin() {
        return spaceMin;
    }

    public void setSpaceMin(float spaceMin) {
        this.spaceMin = spaceMin;
    }

    public float getSpaceMax() {
        return spaceMax;
    }

    public void setSpaceMax(float spaceMax) {
        this.spaceMax = spaceMax;
    }

    public abstract void onDataRangeChanged(float dataMin, float dataMax);
}
