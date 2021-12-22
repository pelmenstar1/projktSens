package com.pelmenstar.projktSens.chartLite.renderer;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import com.pelmenstar.projktSens.chartLite.DataRef;
import com.pelmenstar.projktSens.chartLite.ViewPortHandler;
import com.pelmenstar.projktSens.chartLite.data.ChartData;
import com.pelmenstar.projktSens.chartLite.data.DataSet;
import com.pelmenstar.projktSens.chartLite.data.Entry;
import com.pelmenstar.projktSens.chartLite.formatter.ValueFormatter;
import com.pelmenstar.projktSens.shared.EmptyArray;

import org.jetbrains.annotations.NotNull;

public final class LineChartRenderer {
    @NotNull
    private final Paint linePaint;

    @NotNull
    private final Paint circlePaint;

    @NotNull
    private final Paint labelPaint;

    @NotNull
    private final Paint backgroundPaint;

    @NotNull
    private final Path backgroundPath;

    @NotNull
    private final DataRef dataRef;

    @NotNull
    private final ViewPortHandler viewPortHandler;

    private float @NotNull [] computedPoints = EmptyArray.FLOAT;

    private int computedPointsEntriesHash;
    private int computedPointsVphHash;

    public LineChartRenderer(@NotNull ViewPortHandler viewPortHandler, @NotNull DataRef dataRef) {
        this.viewPortHandler = viewPortHandler;
        this.dataRef = dataRef;

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setTextAlign(Paint.Align.CENTER);

        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        backgroundPath = new Path();
    }

    public void computePoints() {
        ChartData data = dataRef.value;
        if(data == null) {
            return;
        }

        int entriesHash = data.hashCode();
        int vphHash = viewPortHandler.stateHashCode();

        if (computedPointsEntriesHash == entriesHash &&
                computedPointsVphHash == vphHash) {
            return;
        }

        computedPointsEntriesHash = entriesHash;
        computedPointsVphHash = vphHash;

        int totalPointsLength = data.getEntryCount() * 2;
        if (computedPoints.length != totalPointsLength) {
            computedPoints = new float[totalPointsLength];
        }

        float[] points = computedPoints;

        int pOffset = 0;

        for (DataSet dataSet : data.getDataSets()) {
            for (long e : dataSet.getEntries()) {
                points[pOffset] = Entry.getX(e);
                points[pOffset + 1] = Entry.getY(e);

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

                int maxIdx = entries.length - 1;
                int maxLineIdx = entries.length - 2;

                for(int i = 0; i < entries.length; i++) {
                    float x = points[pOffset];
                    float y = points[pOffset + 1];

                    if (i <= maxLineIdx) {
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

                    if (drawValues && i > 0 && i < maxIdx) {
                        float textY = y - valOffset;
                        if(labels != null) {
                            String label = labels[i];

                            c.drawText(label, 0, label.length(), x, textY, labelPaint);
                        } else {
                            char[] buffer = valueFormatter.formatToCharArray(y);

                            c.drawText(buffer, 0, buffer.length, x, textY, labelPaint);
                        }
                    }

                    pOffset += 2;
                }
            }
        }
    }
}
