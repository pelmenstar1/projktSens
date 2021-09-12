package com.pelmenstar.projktSens.chartLite.renderer;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.pelmenstar.projktSens.chartLite.Utils;
import com.pelmenstar.projktSens.chartLite.ViewPortHandler;
import com.pelmenstar.projktSens.chartLite.components.XAxis;
import com.pelmenstar.projktSens.chartLite.formatter.ValueFormatter;
import com.pelmenstar.projktSens.shared.EmptyArray;

import org.jetbrains.annotations.NotNull;

public final class XAxisRenderer extends AxisRenderer<XAxis> {
    private final Paint.FontMetrics labelFontMetrics = new Paint.FontMetrics();
    private float[] computedPoints = EmptyArray.FLOAT;
    private long computedPointsAxisEntriesHash = 0;
    private long computedPointsVphHash = 0;

    public XAxisRenderer(@NotNull ViewPortHandler viewPortHandler, @NotNull XAxis xAxis) {
        super(viewPortHandler, xAxis);

        labelPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    public void computePoints() {
        long vphHash = viewPortHandler.stateHashCode();
        long axisHash = axis.entriesHash;

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

        int eOffset = 0;
        int pOffset = 0;

        while (eOffset < entries.length) {
            computedPoints[pOffset] = entries[eOffset];

            pOffset += 2;
            eOffset++;
        }

        viewPortHandler.valuesToPixels(computedPoints);
    }

    @Override
    public void draw(@NotNull Canvas c) {
        if (!axis.isEnabled()) {
            return;
        }

        int pOffset = 0;
        int eOffset = 0;
        float[] entries = axis.entries;
        String[] labels = axis.labels;
        float yOffset = axis.getYOffset();
        ValueFormatter valueFormatter = axis.getValueFormatter();

        int pos = axis.getPosition();

        float contentTop = viewPortHandler.contentTop();
        float contentBottom = viewPortHandler.contentBottom();

        if (axis.isDrawAxisLineEnabled()) {
            axisLinePaint.setColor(axis.getAxisLineColor());
            axisLinePaint.setStrokeWidth(axis.getAxisLineWidth());

            float contentLeft = viewPortHandler.contentLeft();
            float contentRight = viewPortHandler.contentRight();

            if (pos == XAxis.POSITION_TOP || pos == XAxis.POSITION_BOTH_SIDED) {
                c.drawLine(contentLeft, contentTop, contentRight, contentTop, axisLinePaint);
            }

            if (pos == XAxis.POSITION_BOTTOM || pos == XAxis.POSITION_BOTH_SIDED) {
                c.drawLine(contentLeft, contentBottom, contentRight, contentBottom, axisLinePaint);
            }
        }

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

        while (eOffset < entries.length) {
            float x = computedPoints[pOffset];

            if (drawLabels) {
                labelPaint.getFontMetrics(labelFontMetrics);

                float textYOffset = -labelFontMetrics.ascent;

                if (pos == XAxis.POSITION_TOP || pos == XAxis.POSITION_BOTH_SIDED) {
                    float textY = textYOffset + contentTop - yOffset;

                    Utils.drawAxisText(
                            entries, labels,
                            valueFormatter,
                            eOffset,
                            x, textY,
                            labelPaint,
                            c
                    );
                }

                if (pos == XAxis.POSITION_BOTTOM || pos == XAxis.POSITION_BOTH_SIDED) {
                    float textY = textYOffset + contentBottom + yOffset;

                    Utils.drawAxisText(
                            entries, labels,
                            valueFormatter,
                            eOffset,
                            x, textY,
                            labelPaint,
                            c
                    );
                }
            }

            if (drawGrid) {
                c.drawLine(x, contentTop, x, contentBottom, gridPaint);
            }

            eOffset++;
            pOffset += 2;
        }
    }
}
