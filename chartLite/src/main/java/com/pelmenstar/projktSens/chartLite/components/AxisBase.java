
package com.pelmenstar.projktSens.chartLite.components;

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
 *
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

    private int flags = FLAG_ENABLED | FLAG_DRAW_GRID_LINES | FLAG_DRAW_AXIS_LINE | FLAG_DRAW_LABELS;

    @NotNull
    protected ValueFormatter valueFormatter = FloatValueFormatter.INSTANCE;

    @ColorInt
    private int gridColor = Color.GRAY;
    private float gridLineWidth = 1f;

    @ColorInt
    private int axisLineColor = Color.GRAY;
    private float axisLineWidth = 1f;

    @NotNull
    public float[] entries = EmptyArray.FLOAT;
    public long entriesHash = 0;

    private int labelCount = 6;

    protected float granularity = 1f;
    protected float spaceMin = 0f;
    protected float spaceMax = 0f;

    protected float min = 0f;
    protected float max = 0f;

    private int minLabels = 2;
    private int maxLabels = 25;

    @NotNull
    public String[] labels = EmptyArray.STRING;

    protected float xOffset;
    protected float yOffset;

    @Nullable
    protected Typeface typeface = null;

    protected float textSize;

    @ColorInt
    protected int textColor = Color.BLACK;

    public AxisBase() {
        this.textSize = Utils.dpToPx(10f);
        this.xOffset = Utils.dpToPx(2f);
        this.yOffset = Utils.dpToPx(2f);
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

    public void setTextSize(float size) {
        if (size > 24f)
            size = 24f;
        if (size < 6f)
            size = 6f;

        textSize = size;
    }

    public float getTextSize() {
        return textSize;
    }

    public void setTextColor(@ColorInt int color) {
        textColor = color;
    }

    @ColorInt
    public int getTextColor() {
        return textColor;
    }

    public void setEnabled(boolean enabled) {
        setFlag(FLAG_ENABLED, enabled);
    }

    public boolean isEnabled() {
        return isFlagEnabled(FLAG_ENABLED);
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
        if(state) {
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

    public void setGridColor(@ColorInt int color) {
        gridColor = color;
    }

    @ColorInt
    public int getGridColor() {
        return gridColor;
    }

    public void setAxisLineWidth(float width) {
        axisLineWidth = width;
    }

    public float getAxisLineWidth() {
        return axisLineWidth;
    }

    public void setGridLineWidth(float width) {
        gridLineWidth = width;
    }

    public float getGridLineWidth() {
        return gridLineWidth;
    }

    public void setAxisLineColor(@ColorInt int color) {
        axisLineColor = color;
    }

    @ColorInt
    public int getAxisLineColor() {
        return axisLineColor;
    }

    public void setDrawLabels(boolean enabled) {
        setFlag(FLAG_DRAW_LABELS, enabled);
    }

    public boolean isDrawLabelsEnabled() {
        return isFlagEnabled(FLAG_DRAW_LABELS);
    }

    public void setLabelCount(int count) {
        labelCount = MyMath.clamp(count, minLabels, maxLabels);

        flags &= ~FLAG_FORCE_LABELS;
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

    public void setValueFormatter(@NotNull ValueFormatter f) {
        valueFormatter = f;

        if(labels.length != entries.length) {
            labels = new String[entries.length];
        }

        for (int i = 0; i < entries.length; i++) {
            labels[i] = f.format(entries[i]);
        }
    }


    @NotNull
    public ValueFormatter getValueFormatter() {
        return valueFormatter;
    }

    public float getAxisMaximum() {
        return max;
    }

    public float getAxisMinimum() {
        return min;
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

    public void setAxisMinimum(float min) {
        this.min = min;
        flags |= FLAG_CUSTOM_AXIS_MIN;
    }

    public void setAxisMaximum(float max) {
        this.max = max;
        flags |= FLAG_CUSTOM_AXIS_MAX;
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
