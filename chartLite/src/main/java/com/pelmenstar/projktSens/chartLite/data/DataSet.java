package com.pelmenstar.projktSens.chartLite.data;

import android.graphics.Color;
import android.graphics.Typeface;

import androidx.annotation.ColorInt;

import com.pelmenstar.projktSens.chartLite.SpecialColors;
import com.pelmenstar.projktSens.chartLite.formatter.FloatValueFormatter;
import com.pelmenstar.projktSens.chartLite.formatter.ValueFormatter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DataSet {
    public static final int FLAG_VISIBLE = 1;
    public static final int FLAG_DRAW_VALUES = 1 << 1;
    public static final int FLAG_DRAW_CIRCLES = 1 << 2;
    @NotNull
    public final String[] labels;
    private final long[] entries;

    @NotNull
    private final ValueFormatter valueFormatter;
    private int flags = FLAG_VISIBLE | FLAG_DRAW_VALUES | FLAG_DRAW_CIRCLES;

    private float xMax = Float.MIN_VALUE;
    private float xMin = Float.MAX_VALUE;

    private float yMax = Float.MIN_VALUE;
    private float yMin = Float.MAX_VALUE;

    @ColorInt
    private int color = Color.BLACK;
    @ColorInt
    private int valueTextColor = Color.BLACK;
    private float valueTextSize = 17f;
    @Nullable
    private Typeface valueTypeface;
    private float lineWidth = 2.5f;
    @ColorInt
    private int circleColor = Color.BLACK;
    private float circleRadius = 8f;

    public DataSet(long @NotNull [] entries, @Nullable ValueFormatter formatter) {
        this.entries = entries;

        if (formatter == null) {
            formatter = FloatValueFormatter.INSTANCE;
        }

        valueFormatter = formatter;
        labels = new String[entries.length];

        for (int i = 0; i < entries.length; i++) {
            long e = entries[i];

            float ex = Entry.getX(e);
            float ey = Entry.getY(e);

            if (ex < xMin) {
                xMin = ex;
            }
            if (ex > xMax) {
                xMax = ex;
            }

            if (ey < yMin) {
                yMin = ey;
            }

            if (ey > yMax) {
                yMax = ey;
            }

            labels[i] = formatter.format(ey);
        }
    }

    public int getFlags() {
        return flags;
    }

    private boolean isFlagEnabled(int flag) {
        return (flags & flag) != 0;
    }

    private void setFlag(int flag, boolean state) {
        if (state) {
            flags |= flag;
        } else {
            flags &= ~flag;
        }
    }

    public float getCircleRadius() {
        return circleRadius;
    }

    public void setCircleRadius(float radius) {
        circleRadius = radius;
    }

    public void setDrawCircles(boolean enabled) {
        setFlag(FLAG_DRAW_CIRCLES, enabled);
    }

    public boolean isDrawCirclesEnabled() {
        return isFlagEnabled(FLAG_DRAW_CIRCLES);
    }

    @ColorInt
    public int getCircleColor() {
        return circleColor;
    }

    public void setCircleColor(@ColorInt int color) {
        circleColor = color;
    }

    public float getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(float width) {
        if (width <= 0f) {
            throw new IllegalArgumentException("width=" + width);
        }

        lineWidth = width;
    }

    public int count() {
        return entries.length;
    }

    @NotNull
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(toSimpleString());

        for (long e : entries) {
            buffer.append(Entry.toString(e)).append(' ');
        }

        return buffer.toString();
    }

    @NotNull
    public String toSimpleString() {
        return "DataSet, label: " + ", entries: " + entries.length;
    }

    public float getYMin() {
        return yMin;
    }

    public float getYMax() {
        return yMax;
    }

    public float getXMin() {
        return xMin;
    }

    public float getXMax() {
        return xMax;
    }

    public int indexOf(long e) {
        for (int i = 0; i < entries.length; i++) {
            if (entries[i] == e) {
                return i;
            }
        }

        return -1;
    }

    public long @NotNull [] getEntries() {
        return entries;
    }

    public long get(int index) {
        return entries[index];
    }

    @ColorInt
    public int getValueTextColor() {
        return valueTextColor;
    }

    public void setValueTextColor(@ColorInt int valueColor) {
        this.valueTextColor = valueColor;
    }

    @ColorInt
    public int getColor() {
        return color;
    }

    public void setColor(@ColorInt int color) {
        this.color = color;
    }

    @NotNull
    public ValueFormatter getValueFormatter() {
        return valueFormatter;
    }

    @Nullable
    public Typeface getValueTypeface() {
        return valueTypeface;
    }

    public void setValueTypeface(@Nullable Typeface tf) {
        valueTypeface = tf;
    }

    public float getValueTextSize() {
        return valueTextSize;
    }

    public void setValueTextSize(float size) {
        valueTextSize = size;
    }

    public void setDrawValues(boolean enabled) {
        setFlag(FLAG_DRAW_VALUES, enabled);
    }

    public boolean isDrawValuesEnabled() {
        return isFlagEnabled(FLAG_DRAW_VALUES);
    }

    public boolean isVisible() {
        return isFlagEnabled(FLAG_VISIBLE);
    }

    public void setVisible(boolean enabled) {
        setFlag(FLAG_VISIBLE, enabled);
    }

    public boolean contains(long e) {
        for (long entry : entries) {
            if (e == entry) {
                return true;
            }
        }

        return false;
    }
}