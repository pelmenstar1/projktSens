package com.pelmenstar.projktSens.chartLite.data;

import android.graphics.Color;
import android.graphics.Typeface;

import androidx.annotation.ColorInt;

import com.pelmenstar.projktSens.chartLite.GradientFill;
import com.pelmenstar.projktSens.chartLite.formatter.FloatValueFormatter;
import com.pelmenstar.projktSens.chartLite.formatter.ValueFormatter;
import com.pelmenstar.projktSens.shared.AppendableToStringBuilder;
import com.pelmenstar.projktSens.shared.MyMath;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;

public final class DataSet extends AppendableToStringBuilder {
    public static final int FLAG_VISIBLE = 1;
    public static final int FLAG_DRAW_VALUES = 1 << 1;
    public static final int FLAG_DRAW_CIRCLES = 1 << 2;

    public final @NotNull String @Nullable [] cachedStringLabels;

    private final long[] entries;
    private final int entriesHash;

    @NotNull
    private final ValueFormatter valueFormatter;
    private int flags = FLAG_VISIBLE | FLAG_DRAW_VALUES | FLAG_DRAW_CIRCLES;

    private final float xMin;
    private final float xMax;

    private final float yMin;
    private final float yMax;

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

    @NotNull
    private GradientFill background = GradientFill.TRANSPARENT;

    public DataSet(long @NotNull [] entries, @Nullable ValueFormatter formatter) {
        this.entries = entries;

        if (formatter == null) {
            formatter = FloatValueFormatter.INSTANCE;
        }

        valueFormatter = formatter;

        boolean formatToStringImplemented = !formatter.supportsFormattingToCharArray();

        if(formatToStringImplemented) {
            cachedStringLabels = new String[entries.length];
        } else {
            cachedStringLabels = null;
        }

        float xMin = Float.MAX_VALUE;
        float xMax = Float.MIN_VALUE;
        float yMin = Float.MAX_VALUE;
        float yMax = Float.MIN_VALUE;
        int entriesHash = 0;

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

            if(formatToStringImplemented) {
                cachedStringLabels[i] = formatter.formatToString(ey);
            }

            entriesHash = entriesHash * 31 + MyMath.hashCodeLong(e);
        }

        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
        this.entriesHash = entriesHash;
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

    public int getEntriesHash() {
        return entriesHash;
    }

    public int getEntryCount() {
        return entries.length;
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

    @NotNull
    public GradientFill getBackground() {
        return background;
    }

    public void setBackground(@NotNull GradientFill background) {
        this.background = background;
    }

    public boolean contains(long e) {
        for (long entry : entries) {
            if (e == entry) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void append(@NotNull StringBuilder sb) {
        sb.append("{entries=[");
        if(entries.length > 0) {
            int maxIdx = entries.length - 1;
            for(int i = 0; i < maxIdx; i++) {
                Entry.append(entries[i], sb);
                sb.append(',');
            }

            Entry.append(entries[maxIdx], sb);
        }
        sb.append("]}");
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        DataSet o = (DataSet) other;

        return flags == o.flags &&
                color == o.color &&
                valueTextColor == o.valueTextColor &&
                valueTextSize == o.valueTextSize &&
                lineWidth == o.lineWidth &&
                circleColor == o.circleColor &&
                circleRadius == o.circleRadius &&
                Arrays.equals(entries, o.entries) &&
                valueFormatter.equals(o.valueFormatter) &&
                Objects.equals(valueTypeface, o.valueTypeface);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(entries);
        result = 31 * result + valueFormatter.hashCode();
        result = 31 * result + flags;
        result = 31 * result + color;
        result = 31 * result + valueTextColor;
        result = 31 * result + Float.floatToIntBits(valueTextSize);
        result = 31 * result + (valueTypeface != null ? valueTypeface.hashCode() : 0);
        result = 31 * result + Float.floatToIntBits(lineWidth);
        result = 31 * result + circleColor;
        result = 31 * result + Float.floatToIntBits(circleRadius);

        return result;
    }
}