package com.pelmenstar.projktSens.chartLite.renderer;

import android.graphics.Canvas;

import com.pelmenstar.projktSens.chartLite.Utils;
import com.pelmenstar.projktSens.chartLite.ViewPortHandler;
import com.pelmenstar.projktSens.chartLite.components.YAxis;
import com.pelmenstar.projktSens.chartLite.formatter.ValueFormatter;
import com.pelmenstar.projktSens.shared.EmptyArray;

import org.jetbrains.annotations.NotNull;

public final class YAxisRenderer extends AxisRenderer<YAxis> {
    private float @NotNull [] computedPoints = EmptyArray.FLOAT;
    private int computedPointsAxisEntriesHash = 0;
    private int computedPointsVphHash = 0;

    public YAxisRenderer(@NotNull ViewPortHandler viewPortHandler, @NotNull YAxis yAxis) {
        super(viewPortHandler, yAxis);
    }

    @Override
    public void computePoints() {
        int vphHash = viewPortHandler.stateHashCode();
        int axisHash = axis.entriesHash;

        if (computedPointsAxisEntriesHash == axisHash && computedPointsVphHash == vphHash) {
            return;
        }

        computedPointsAxisEntriesHash = axisHash;
        computedPointsVphHash = vphHash;

        float[] entries = axis.entries;
        int pointsSize = entries.length * 2;
        if (computedPoints.length != pointsSize) {
            computedPoints = new float[pointsSize];
        }

        int pOffset = 1;
        int eOffset = 0;

        while (eOffset < entries.length) {
            computedPoints[pOffset] = entries[eOffset];

            eOffset++;
            pOffset += 2;
        }

        viewPortHandler.valuesToPixels(computedPoints);
    }

    @Override
    public void draw(@NotNull Canvas c) {
        if (!axis.isEnabled()) {
            return;
        }

        int pOffset = 1;
        int eOffset = 0;
        float[] entries = axis.entries;
        String[] labels = axis.labels;
        int pos = axis.getPosition();
        ValueFormatter valueFormatter = axis.getValueFormatter();

        float xOffset = axis.getXOffset();

        boolean drawLabels = axis.isDrawLabelsEnabled();
        boolean drawGrid = axis.isDrawGridLinesEnabled();

        if (drawLabels) {
            labelPaint.setTypeface(axis.getTypeface());
            labelPaint.setTextSize(axis.getTextSize());
            labelPaint.setColor(axis.getTextColor());
        }

        if (drawGrid) {
            gridPaint.setColor(axis.getGridColor());
            gridPaint.setStrokeWidth(axis.getGridLineWidth());
        }

        float contentLeft = viewPortHandler.contentLeft();
        float contentRight = viewPortHandler.contentRight();

        if (axis.isDrawAxisLineEnabled()) {
            float contentTop = viewPortHandler.contentTop();
            float contentBottom = viewPortHandler.contentBottom();

            axisLinePaint.setColor(axis.getAxisLineColor());
            axisLinePaint.setStrokeWidth(axis.getAxisLineWidth());

            if (pos == YAxis.POSITION_LEFT || pos == YAxis.POSITION_BOTH) {
                c.drawLine(contentLeft, contentTop, contentLeft, contentBottom, axisLinePaint);
            }

            if (pos == YAxis.POSITION_RIGHT || pos == YAxis.POSITION_BOTH) {
                c.drawLine(contentRight, contentTop, contentRight, contentBottom, axisLinePaint);
            }
        }

        while (eOffset < entries.length) {
            float y = computedPoints[pOffset];

            if (drawLabels) {
                if (pos == YAxis.POSITION_LEFT || pos == YAxis.POSITION_BOTH) {
                    Utils.drawAxisText(
                            entries, labels,
                            valueFormatter,
                            eOffset,
                            1f, y,
                            labelPaint,
                            c
                    );
                }

                if (pos == YAxis.POSITION_RIGHT || pos == YAxis.POSITION_BOTH) {
                    float x = contentRight + xOffset;

                    Utils.drawAxisText(
                            entries, labels,
                            valueFormatter,
                            eOffset,
                            x, y,
                            labelPaint,
                            c
                    );
                }
            }

            if (drawGrid) {
                c.drawLine(contentLeft, y, contentRight, y, gridPaint);
            }

            eOffset++;
            pOffset += 2;
        }
    }
}
