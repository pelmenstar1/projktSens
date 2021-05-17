package com.pelmenstar.projktSens.chartLite.renderer;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.pelmenstar.projktSens.chartLite.Utils;
import com.pelmenstar.projktSens.chartLite.ViewPortHandler;
import com.pelmenstar.projktSens.chartLite.components.AxisBase;
import com.pelmenstar.projktSens.chartLite.formatter.ValueFormatter;
import com.pelmenstar.projktSens.shared.EmptyArray;

import org.jetbrains.annotations.NotNull;

/**
 * Baseclass of all axis renderers.
 *
 */
public abstract class AxisRenderer<TAxis extends AxisBase> {
    @NotNull
    protected final ViewPortHandler viewPortHandler;

    @NotNull
    protected final TAxis axis;

    protected final Paint gridPaint;
    protected final Paint axisLinePaint;
    protected final Paint labelPaint;

    public AxisRenderer(@NotNull ViewPortHandler viewPortHandler, @NotNull TAxis axis) {
        this.viewPortHandler = viewPortHandler;
        this.axis = axis;

        labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        axisLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    @NotNull
    public final Paint getLabelPaint() {
        return labelPaint;
    }

    public abstract void computeAxis();

    protected final void computeAxisValues(float min, float max) {
        int labelCount = axis.getLabelCount();

        if (labelCount == 0 || max == min) {
            axis.entries = EmptyArray.FLOAT;
            axis.labels = EmptyArray.STRING;

            return;
        }

        float range = Math.abs(max - min);

        ValueFormatter valueFormatter = axis.getValueFormatter();
        float[] entries = axis.entries;

        // force label count
        if (axis.isForceLabelsEnabled()) {
            float interval = range / (float) (labelCount - 1);

            if (entries.length != labelCount) {
                entries = new float[labelCount];
                axis.entries = entries;
            }

            float v = min;

            if (axis.labels.length != labelCount) {
                axis.labels = new String[labelCount];
            }

            String[] labels = axis.labels;
            long hash = 0;

            for (int i = 0; i < labelCount; i++) {
                entries[i] = v;
                hash = hash * 31 + Float.floatToIntBits(v);

                labels[i] = valueFormatter.format(v);

                v += interval;
            }

            axis.entriesHash = hash;
        } else {
            // Find out how much spacing (in y value space) between axis values
            float rawInterval = range / labelCount;
            float interval = Utils.roundToNextSignificant(rawInterval);

            // If granularity is enabled, then do not allow the interval to go below specified granularity.
            // This is used to avoid repeated values when rounding values for display.
            if (axis.isGranularityEnabled()) {
                interval = Math.max(interval, axis.getGranularity());
            }

            // Normalize interval
            float intervalMagnitude = Utils.roundToNextSignificant((float)Math.pow(10, (int) Math.log10(interval)));
            int intervalSigDigit = (int) (interval / intervalMagnitude);
            if (intervalSigDigit > 5) {
                // Use one order of magnitude higher, to avoid intervals like 0.9 or 90
                // if it's 0.0 after floor(), we use the old value
                float im = (float)Math.floor(10f * intervalMagnitude);

                interval = im == 0f ? interval : im;
            }

            float first;
            float last;

            if (interval == 0f) {
                first = 0f;
                last = 0f;
            } else {
                first = (float) Math.ceil(min / interval) * interval;
                last = (float) Math.floor(max / interval) * interval;
            }

            int n = 0;
            if (last != first) {
                float f = first;

                while(f <= last) {
                    n++;
                    f += interval;
                }
            } else {
                n = 1;
            }

            if (entries.length != n) {
                entries = new float[n];
                axis.entries = entries;
            }

            String[] labels = axis.labels;
            if (labels.length != n) {
                labels = new String[n];
                axis.labels = labels;
            }

            float v = first;
            int i = 0;
            long hash = 0;

            while(i < n) {
                entries[i] = v;
                hash = hash * 31 + Float.floatToIntBits(v);
                labels[i] = valueFormatter.format(v);

                v += interval;
                i++;
            }

            axis.entriesHash = hash;
        }
    }

    /**
     * Draws axis on specified {@link Canvas}
     */
    public abstract void draw(@NotNull Canvas c);

    /**
     * Computes and transforms needed points for rendering
     */
    public abstract void computePoints();
}
