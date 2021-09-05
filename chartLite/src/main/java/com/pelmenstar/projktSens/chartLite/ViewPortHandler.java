package com.pelmenstar.projktSens.chartLite;

import android.graphics.Matrix;
import android.graphics.RectF;

import org.jetbrains.annotations.NotNull;

public final class ViewPortHandler {
    private final Matrix cvtMatrix = new Matrix();

    private final RectF content = new RectF();
    private final float[] matrixBuffer = new float[9];
    private float chartWidth = 0f;
    private float chartHeight = 0f;

    private float scaleX = 1f;
    private float scaleY = 1f;
    private float transX = 0f;
    private float transY = 0f;

    public void prepareTransform(float minX, float maxX, float minY, float maxY) {
        float deltaX = Math.abs(maxX - minX);
        float deltaY = Math.abs(maxY - minY);

        scaleX = content.width() / deltaX;
        scaleY = content.height() / deltaY;

        cvtMatrix.setTranslate(-minX, -minY);
        cvtMatrix.postScale(scaleX, -scaleY);
        cvtMatrix.postTranslate(content.left, content.bottom);

        cvtMatrix.getValues(matrixBuffer);
        transX = matrixBuffer[Matrix.MTRANS_X];
        transY = matrixBuffer[Matrix.MTRANS_Y];
    }

    public void valuesToPixels(float @NotNull [] pts) {
        cvtMatrix.mapPoints(pts);
    }

    public void onSizeChanged(float width, float height) {
        float offsetRight = chartWidth - content.right;
        float offsetBottom = chartHeight - content.bottom;

        chartWidth = width;
        chartHeight = height;

        content.right = chartWidth - offsetRight;
        content.bottom = chartHeight - offsetBottom;
    }

    public void setOffsets(float left, float top, float right, float bottom) {
        content.set(left, top, chartWidth - right, chartHeight - bottom);
    }

    public float contentTop() {
        return content.top;
    }

    public float contentLeft() {
        return content.left;
    }

    public float contentRight() {
        return content.right;
    }

    public float contentBottom() {
        return content.bottom;
    }

    public float contentWidth() {
        return content.width();
    }

    public float contentHeight() {
        return content.height();
    }

    @NotNull
    public RectF getContentRect() {
        return content;
    }

    public float chartHeight() {
        return chartHeight;
    }

    public float chartWidth() {
        return chartWidth;
    }

    public long stateHashCode() {
        long hash = content.hashCode();
        hash = 31 * hash + Float.floatToIntBits(transX);
        hash = 31 * hash + Float.floatToIntBits(transY);
        hash = 31 * hash + Float.floatToIntBits(scaleX);
        hash = 31 * hash + Float.floatToIntBits(scaleY);

        return hash;
    }

    public float getScaleX() {
        return scaleX;
    }

    public float getScaleY() {
        return scaleY;
    }
}
