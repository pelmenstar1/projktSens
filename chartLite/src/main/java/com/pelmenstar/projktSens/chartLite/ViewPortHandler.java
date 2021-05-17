package com.pelmenstar.projktSens.chartLite;

import android.graphics.Matrix;
import android.graphics.RectF;

import com.pelmenstar.projktSens.shared.MyMath;

import org.jetbrains.annotations.NotNull;


public final class ViewPortHandler {
    private final Matrix matrixTouch = new Matrix();
    private final Matrix dataMatrix = new Matrix();
    private final Matrix cvtMatrix = new Matrix();
    private final Matrix invCvtMatrix = new Matrix();

    private final RectF content = new RectF();

    private float chartWidth = 0f;
    private float chartHeight = 0f;
    private float minScaleY = 1f;
    private float maxScaleY = Float.MAX_VALUE;

    private float minScaleX = 1f;
    private float maxScaleX = Float.MAX_VALUE;

    private float scaleX = 1f;
    private float scaleY = 1f;

    private float transX = 0f;
    private float transY = 0f;

    public void prepareTransform(float minX, float maxX, float minY, float maxY) {
        float deltaX = Math.abs(maxX - minX);
        float deltaY = Math.abs(maxY - minY);

        float scaleX = content.width() / deltaX;
        float scaleY = content.height() / deltaY;

        dataMatrix.setTranslate(-minX, -minY);
        dataMatrix.postScale(scaleX, -scaleY);
        dataMatrix.postTranslate(content.left, content.bottom);

        updateConvertMatrix();
    }

    private void updateConvertMatrix() {
        cvtMatrix.setConcat(matrixTouch, dataMatrix);

        invCvtMatrix.set(cvtMatrix);
        invCvtMatrix.invert(invCvtMatrix);
    }

    public void valuesToPixels(@NotNull float[] pts) {
        cvtMatrix.mapPoints(pts, 0, pts, 0, pts.length >> 1);
    }

    public void pixelsToValues(@NotNull float[] pixels) {
        invCvtMatrix.mapPoints(pixels, 0, pixels, 0, pixels.length >> 1);
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

    public void refresh(@NotNull Matrix newMatrix) {
        matrixTouch.set(newMatrix);

        limitMatrixValues();
        newMatrix.set(matrixTouch);
    }

    private final float[] matrixBuffer = new float[9];

    private void limitMatrixValues() {
        matrixTouch.getValues(matrixBuffer);

        float curTransX = matrixBuffer[Matrix.MTRANS_X];
        float curScaleX = matrixBuffer[Matrix.MSCALE_X];

        float curTransY = matrixBuffer[Matrix.MTRANS_Y];
        float curScaleY = matrixBuffer[Matrix.MSCALE_Y];

        scaleX = MyMath.clamp(curScaleX, minScaleX, maxScaleX);
        scaleY = MyMath.clamp(curScaleY, minScaleY, maxScaleY);

        float width = content.width();
        float height = content.height();

        float maxTransX = -width * (scaleX - 1f);
        transX = Math.min(Math.max(curTransX, maxTransX), 0f);

        float maxTransY = height * (scaleY - 1f);
        transY = Math.max(Math.min(curTransY, maxTransY), 0f);

        matrixBuffer[Matrix.MTRANS_X] = transX;
        matrixBuffer[Matrix.MSCALE_X] = scaleX;

        matrixBuffer[Matrix.MTRANS_Y] = transY;
        matrixBuffer[Matrix.MSCALE_Y] = scaleY;

        matrixTouch.setValues(matrixBuffer);

        updateConvertMatrix();
    }

    public void setMinimumScaleX(float xScale) {
        minScaleX = Math.max(xScale, 1f);

        limitMatrixValues();
    }

    public void setMaximumScaleX(float xScale) {
        if (xScale == 0f)
            xScale = Float.MAX_VALUE;

        maxScaleX = xScale;

        limitMatrixValues();
    }

    public void setMinMaxScaleX(float minScaleX, float maxScaleX) {
        if (maxScaleX == 0f)
            maxScaleX = Float.MAX_VALUE;

        this.minScaleX = Math.max(minScaleX, 1f);
        this.maxScaleX = maxScaleX;

        limitMatrixValues();
    }

    public void setMinimumScaleY(float yScale) {
        minScaleY = Math.max(yScale, 1f);

        limitMatrixValues();
    }

    public void setMaximumScaleY(float yScale) {
        if (yScale == 0f)
            yScale = Float.MAX_VALUE;

        maxScaleY = yScale;

        limitMatrixValues();
    }

    public void setMinMaxScaleY(float minScaleY, float maxScaleY) {
        if (maxScaleY == 0f)
            maxScaleY = Float.MAX_VALUE;

        this.minScaleY = Math.max(minScaleY, 1f);
        this.maxScaleY = maxScaleY;

        limitMatrixValues();
    }

    public long stateHashCode() {
        long hash = Float.floatToIntBits(scaleX);
        hash = hash * 31 + Float.floatToIntBits(scaleY);
        hash = hash * 31 + Float.floatToIntBits(transX);
        hash = hash * 31 + Float.floatToIntBits(transY);
        hash = hash * 31 + content.hashCode();

        return hash;
    }

    @NotNull
    public Matrix getMatrixTouch() {
        return matrixTouch;
    }

    public boolean isInBoundsX(float x) {
        return isInBoundsLeft(x) && isInBoundsRight(x);
    }

    public boolean isInBoundsY(float y) {
        return isInBoundsTop(y) && isInBoundsBottom(y);
    }

    public boolean isInBounds(float x, float y) {
        return isInBoundsX(x) && isInBoundsY(y);
    }

    public boolean isInBoundsLeft(float x) {
        return x >= content.left;
    }

    public boolean isInBoundsRight(float x) {
        return x <= content.right;
    }

    public boolean isInBoundsTop(float y) {
        return y >= content.bottom;
    }

    public boolean isInBoundsBottom(float y) {
        return y <= content.bottom;
    }

    public float getScaleX() {
        return scaleX;
    }

    public float getScaleY() {
        return scaleY;
    }

    public float getMinScaleX() {
        return minScaleX;
    }

    public float getMaxScaleX() {
        return maxScaleX;
    }

    public float getMinScaleY() {
        return minScaleY;
    }

    public float getMaxScaleY() {
        return maxScaleY;
    }

    public float getTransX() {
        return transX;
    }

    public float getTransY() {
        return transY;
    }

    public boolean isFullyZoomedOut() {
        return isFullyZoomedOutX() && isFullyZoomedOutY();
    }

    public boolean isFullyZoomedOutY() {
        return !(scaleY > minScaleY || minScaleY > 1f);
    }

    public boolean isFullyZoomedOutX() {
        return !(scaleX > minScaleX || minScaleX > 1f);
    }

    public boolean canZoomOutMoreX() {
        return scaleX > minScaleX;
    }

    public boolean canZoomInMoreX() {
        return scaleX < maxScaleX;
    }

    public boolean canZoomOutMoreY() {
        return scaleY > minScaleY;
    }

    public boolean canZoomInMoreY() {
        return scaleY < maxScaleY;
    }
}
