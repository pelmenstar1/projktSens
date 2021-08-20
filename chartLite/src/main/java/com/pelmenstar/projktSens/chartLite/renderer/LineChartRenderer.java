package com.pelmenstar.projktSens.chartLite.renderer;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import com.pelmenstar.projktSens.chartLite.DataRef;
import com.pelmenstar.projktSens.chartLite.SimpleChartAnimator;
import com.pelmenstar.projktSens.chartLite.ViewPortHandler;
import com.pelmenstar.projktSens.chartLite.data.ChartData;
import com.pelmenstar.projktSens.chartLite.data.DataSet;
import com.pelmenstar.projktSens.chartLite.data.Entry;
import com.pelmenstar.projktSens.chartLite.formatter.ValueFormatter;
import com.pelmenstar.projktSens.shared.EmptyArray;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class LineChartRenderer {
    private final Paint linePaint;
    private final Paint circlePaint;
    private final Paint labelPaint;

    @NotNull
    private final DataRef dataRef;

    @NotNull
    private final ViewPortHandler viewPortHandler;

    @NotNull
    private final SimpleChartAnimator animator;
    private final RectF clipRect = new RectF();
    private float[] computedPoints = EmptyArray.FLOAT;
    @Nullable
    private ChartData computedPointsData;
    private long computedPointsVphHash = 0;
    private float computedPointsPhaseX;
    private float computedPointsPhaseY;

    public LineChartRenderer(@NotNull ViewPortHandler viewPortHandler, @NotNull SimpleChartAnimator animator, @NotNull DataRef dataRef) {
        this.viewPortHandler = viewPortHandler;
        this.animator = animator;
        this.dataRef = dataRef;

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void computePoints() {
        ChartData data = dataRef.value;
        long vphHash = viewPortHandler.stateHashCode();

        float phaseX = animator.getPhaseX();
        float phaseY = animator.getPhaseY();

        if (data == null || (
                computedPointsData == data &&
                        computedPointsVphHash == vphHash &&
                        computedPointsPhaseX == phaseX &&
                        computedPointsPhaseY == phaseY
        )) {
            return;
        }

        computedPointsData = data;
        computedPointsVphHash = vphHash;
        computedPointsPhaseX = phaseX;
        computedPointsPhaseY = phaseY;

        int totalPointsLength = data.getEntryCount() * 2;
        if (computedPoints.length != totalPointsLength) {
            computedPoints = new float[totalPointsLength];
        }

        float[] points = computedPoints;

        int pOffset = 0;

        for (DataSet dataSet : data.getDataSets()) {
            for (long e : dataSet.getEntries()) {
                points[pOffset] = Entry.getX(e) * phaseX;
                points[pOffset + 1] = Entry.getY(e) * phaseY;

                pOffset += 2;
            }
        }

        viewPortHandler.valuesToPixels(points);
    }

    public void draw(@NotNull Canvas c) {
        ChartData data = dataRef.value;
        if (data == null) {
            return;
        }

        int pOffset = 0;
        float[] points = computedPoints;
        RectF content = viewPortHandler.getContentRect();

        for (DataSet dataSet : data.getDataSets()) {
            if (dataSet.isVisible() && dataSet.count() > 1) {
                linePaint.setColor(dataSet.getColor());
                linePaint.setStrokeWidth(dataSet.getLineWidth());

                circlePaint.setColor(dataSet.getCircleColor());

                labelPaint.setColor(dataSet.getValueTextColor());
                labelPaint.setTextSize(dataSet.getValueTextSize());
                labelPaint.setTypeface(dataSet.getValueTypeface());

                float circleRadius = dataSet.getCircleRadius();

                float valOffset = circleRadius * 1.75f;

                boolean drawValues = dataSet.isDrawValuesEnabled();
                boolean drawCircles = dataSet.isDrawCirclesEnabled();

                if (!drawCircles) {
                    valOffset *= 0.5f;
                }

                String[] labels = dataSet.cachedStringLabels;
                ValueFormatter valueFormatter = dataSet.getValueFormatter();

                long[] entries = dataSet.getEntries();
                int eIndex = 0;

                int maxIdx = entries.length - 1;
                int maxLineIdx = entries.length - 2;

                clipRect.set(content);
                clipRect.left -= circleRadius;
                clipRect.top -= circleRadius;
                clipRect.right += circleRadius;
                clipRect.bottom += circleRadius;

                int save = c.save();
                c.clipRect(clipRect);

                while (eIndex < entries.length) {
                    float x = points[pOffset];
                    float y = points[pOffset + 1];

                    if (eIndex <= maxLineIdx) {
                        float nextX = points[pOffset + 2];
                        float nextY = points[pOffset + 3];

                        c.drawLine(x, y, nextX, nextY, linePaint);
                    }

                    if (drawCircles) {
                        c.drawOval(
                                x - circleRadius,
                                y - circleRadius,
                                x + circleRadius,
                                y + circleRadius,
                                circlePaint
                        );
                    }

                    if (drawValues && eIndex > 0 && eIndex < maxIdx) {
                        float textY = y - valOffset;
                        if(labels != null) {
                            String label = labels[eIndex];

                            c.drawText(label, 0, label.length(), x, textY, labelPaint);
                        } else {
                            char[] buffer = valueFormatter.formatToCharArray(y);

                            c.drawText(buffer, 0, buffer.length, x, textY, labelPaint);
                        }
                    }

                    eIndex++;
                    pOffset += 2;
                }

                c.restoreToCount(save);
            }
        }
    }
}
