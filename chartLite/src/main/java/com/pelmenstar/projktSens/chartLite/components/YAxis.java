package com.pelmenstar.projktSens.chartLite.components;

import android.graphics.Paint;

import com.pelmenstar.projktSens.chartLite.Utils;

import org.jetbrains.annotations.NotNull;

public final class YAxis extends AxisBase {
    public static final int POSITION_LEFT = 0;
    public static final int POSITION_RIGHT = 1;
    public static final int POSITION_BOTH = 2;

    private float spacePercentTop = 10f;
    private float spacePercentBottom = 10f;

    private int position = POSITION_BOTH;

    private float xLabelOffset = 0.0f;

    private float minWidth = 0f;
    private float maxWidth = Float.POSITIVE_INFINITY;

    public YAxis() {
        yOffset = 0f;
    }

    public float getMinWidth() {
        return minWidth;
    }

    public void setMinWidth(float minWidth) {
        this.minWidth = minWidth;
    }

    public float getMaxWidth() {
        return maxWidth;
    }

    public void setMaxWidth(float maxWidth) {
        this.maxWidth = maxWidth;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int pos) {
        position = pos;
    }

    public float getLabelXOffset() {
        return xLabelOffset;
    }

    public void setLabelXOffset(float xOffset) {
        xLabelOffset = xOffset;
    }

    public float getSpaceTop() {
        return spacePercentTop;
    }

    public void setSpaceTop(float percent) {
        spacePercentTop = percent;
    }

    public float getSpaceBottom() {
        return spacePercentBottom;
    }

    public void setSpaceBottom(float percent) {
        spacePercentBottom = percent;
    }

    public float getRequiredWidthSpace(@NotNull Paint p) {
        p.setTextSize(textSize);
        p.setTypeface(typeface);
        String str = "";

        for (String label : labels) {
            if (label.length() > str.length()) {
                str = label;
            }
        }

        float width = p.measureText(str, 0, str.length()) + getXOffset() * 2f;

        float minWidth = this.minWidth;
        float maxWidth = this.maxWidth;

        if (minWidth > 0f)
            minWidth = Utils.dpToPx(minWidth);

        if (maxWidth > 0f && maxWidth != Float.POSITIVE_INFINITY)
            maxWidth = Utils.dpToPx(maxWidth);

        width = Math.max(minWidth, Math.min(width, maxWidth > 0f ? maxWidth : width));

        return width;
    }

    public boolean needsOffset() {
        return isEnabled() && isDrawLabelsEnabled();
    }

    @Override
    public void onDataRangeChanged(float dataMin, float dataMax) {
        boolean customAxisMin = isFlagEnabled(FLAG_CUSTOM_AXIS_MIN);
        boolean customAxisMax = isFlagEnabled(FLAG_CUSTOM_AXIS_MAX);

        if (dataMin > dataMax) {
            if (customAxisMax && customAxisMin) {
                float t = dataMin;
                dataMin = dataMax;
                dataMax = t;
            } else if (customAxisMax) {
                dataMin = dataMax < 0f ? dataMax * 1.5f : dataMax * 0.5f;
            } else if (customAxisMin) {
                dataMax = dataMin < 0f ? dataMin * 0.5f : dataMin * 1.5f;
            }
        }

        if (dataMax == dataMin) {
            dataMax++;
            dataMin--;
        }

        float range100 = Math.abs(dataMax - dataMin) * 0.01f;
        // calc extra spacing
        this.min = customAxisMin ? this.min : dataMin - range100 * spacePercentBottom;
        this.max = customAxisMax ? this.max : dataMax + range100 * spacePercentTop;
    }
}
