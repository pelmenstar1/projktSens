package com.pelmenstar.projktSens.chartLite.components;

import android.content.Context;

import androidx.annotation.IntDef;

import com.pelmenstar.projktSens.chartLite.Utils;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Class representing the x-axis labels settings. Only use the setter methods to
 * modify it. Do not access public variables directly. Be aware that not all
 * features the XLabels class provides are suitable for the RadarChart.
 *
 * @author Philipp Jahoda
 */
public final class XAxis extends AxisBase {
    public static final int POSITION_TOP = 0;
    public static final int POSITION_BOTTOM = 1;
    public static final int POSITION_BOTH_SIDED = 2;

    private boolean avoidFirstLastClipping = false;

    @Position
    private int position = POSITION_TOP;

    public XAxis(@NotNull Context context) {
        super(context);

        yOffset = Utils.dpToPx(context, 4f);
    }

    @Position
    public int getPosition() {
        return position;
    }

    public void setPosition(@Position int pos) {
        position = pos;
    }

    public void setAvoidFirstLastClipping(boolean enabled) {
        avoidFirstLastClipping = enabled;
    }

    public boolean isAvoidFirstLastClippingEnabled() {
        return avoidFirstLastClipping;
    }

    @Override
    public void onDataRangeChanged(float dataMin, float dataMax) {
        // if custom, use value as is, else use data value
        float min = isFlagEnabled(FLAG_CUSTOM_AXIS_MIN) ? this.min : (dataMin - spaceMin);
        float max = isFlagEnabled(FLAG_CUSTOM_AXIS_MAX) ? this.max : (dataMax + spaceMax);

        if (min == max) {
            max++;
            min--;
        }

        this.min = min;
        this.max = max;
    }

    @Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({POSITION_TOP, POSITION_BOTTOM, POSITION_BOTH_SIDED})
    public @interface Position {
    }
}
