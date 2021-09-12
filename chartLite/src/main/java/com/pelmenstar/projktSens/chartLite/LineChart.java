package com.pelmenstar.projktSens.chartLite;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.AttrRes;
import androidx.annotation.StyleRes;

import com.pelmenstar.projktSens.chartLite.components.XAxis;
import com.pelmenstar.projktSens.chartLite.components.YAxis;
import com.pelmenstar.projktSens.chartLite.data.ChartData;
import com.pelmenstar.projktSens.chartLite.renderer.LineChartRenderer;
import com.pelmenstar.projktSens.chartLite.renderer.XAxisRenderer;
import com.pelmenstar.projktSens.chartLite.renderer.YAxisRenderer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Chart that draws lines, circles, labels... Just line chart
 */
public class LineChart extends View {
    @NotNull
    protected final DataRef dataRef;

    @NotNull
    protected final LineChartRenderer renderer;
    protected final ViewPortHandler viewPortHandler;
    protected final XAxis xAxis;
    protected final YAxis yAxis;
    protected final XAxisRenderer xAxisRenderer;
    protected final YAxisRenderer yAxisRenderer;
    protected final SimpleChartAnimator animator;

    private boolean isAutoAnimated;
    private boolean isOffsetsCalculated;
    private boolean isFirstRender;

    protected float minOffset = 15f;

    public LineChart(@NotNull Context context) {
        this(context, null, 0, 0);
    }

    public LineChart(@NotNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0, 0);
    }

    public LineChart(@NotNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public LineChart(@NotNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        setWillNotDraw(false);

        animator = new SimpleChartAnimator(this);
        dataRef = new DataRef();
        viewPortHandler = new ViewPortHandler();

        renderer = new LineChartRenderer(viewPortHandler, animator, dataRef);

        xAxis = new XAxis(context);
        xAxisRenderer = new XAxisRenderer(viewPortHandler, xAxis);

        yAxis = new YAxis(context);
        yAxisRenderer = new YAxisRenderer(viewPortHandler, yAxis);
    }

    /**
     * Returns whether auto animations is enabled
     */
    public boolean isAutoAnimated() {
        return isAutoAnimated;
    }

    /**
     * Sets state of auto animations in chart
     */
    public void setAutoAnimated(boolean value) {
        isAutoAnimated = value;
    }

    /**
     * Gets structured data of chart, can be null
     */
    @Nullable
    public ChartData getData() {
        return dataRef.value;
    }

    /**
     * Sets structured data for chart
     */
    public void setData(@Nullable ChartData data) {
        dataRef.value = data;
        isOffsetsCalculated = false;

        // let the chart know there is new data
        notifyDataChanged();
    }

    /**
     * Clears data. Chart become empty
     */
    public void clear() {
        dataRef.value = null;
        isOffsetsCalculated = false;

        invalidate();
    }

    /**
     * Determines whether chart is empty
     */
    public boolean isEmpty() {
        return dataRef.value == null || dataRef.value.getEntryCount() == 0;
    }

    /**
     * Gets x-axis
     */
    @NotNull
    public XAxis getXAxis() {
        return xAxis;
    }

    /**
     * Returns {@link ViewPortHandler} that connected with chart
     */
    @NotNull
    public ViewPortHandler getViewPortHandler() {
        return viewPortHandler;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int size = (int)Utils.dpToPx(getContext(), 50f);
        setMeasuredDimension(
                Math.max(getSuggestedMinimumWidth(), resolveSize(size, widthMeasureSpec)),
                Math.max(getSuggestedMinimumHeight(), resolveSize(size, heightMeasureSpec))
        );
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        viewPortHandler.onSizeChanged(w, h);

        notifyDataChanged();
    }

    @Override
    protected void onDraw(@NotNull Canvas c) {
        if (dataRef.value == null) {
            return;
        }

        if (!isOffsetsCalculated) { // if offsets are not calculated
            calculateOffsets();
            isOffsetsCalculated = true;
        }

        xAxisRenderer.draw(c);
        yAxisRenderer.draw(c);
        renderer.draw(c);

        if (isAutoAnimated && isFirstRender) {
            isFirstRender = false;
            animateXY(500);
        }
    }

    private void prepareTransMatrix() {
        viewPortHandler.prepareTransform(xAxis.getMin(), xAxis.getMax(), yAxis.getMin(), yAxis.getMax());

        renderer.computePoints();
        xAxisRenderer.computePoints();
        yAxisRenderer.computePoints();
    }

    private void notifyDataChanged() {
        ChartData data = dataRef.value;
        if (data == null) {
            return;
        }

        xAxis.onDataRangeChanged(data.getXMin(), data.getXMax());
        yAxis.onDataRangeChanged(data.getYMin(), data.getYMax());

        xAxisRenderer.computeAxis();
        yAxisRenderer.computeAxis();

        calculateOffsets();
    }

    void computeChartPoints() {
        renderer.computePoints();
    }

    private void calculateOffsets() {
        float offsetLeft = 0f, offsetRight = 0f, offsetTop = 0f, offsetBottom = 0f;

        // offsets for y-labels
        if (yAxis.needsOffset()) {
            float requiredWidth = yAxis.getRequiredWidthSpace(yAxisRenderer.getLabelPaint());
            int pos = yAxis.getPosition();

            if (pos == YAxis.POSITION_LEFT || pos == YAxis.POSITION_BOTH) {
                offsetLeft = requiredWidth;
            }

            if (pos == YAxis.POSITION_RIGHT || pos == YAxis.POSITION_BOTH) {
                offsetRight = requiredWidth;
            }
        }

        if (xAxis.isEnabled() && xAxis.isDrawLabelsEnabled()) {
            float yOffset = xAxis.getYOffset();
            int pos = xAxis.getPosition();

            if (pos == XAxis.POSITION_TOP || pos == XAxis.POSITION_BOTH_SIDED) {
                offsetTop = yOffset;
            }

            if (pos == XAxis.POSITION_BOTTOM || pos == XAxis.POSITION_BOTH_SIDED) {
                offsetBottom = yOffset;
            }
        }

        float minOffset = Utils.dpToPx(getContext(), this.minOffset);

        viewPortHandler.setOffsets(
                Math.max(minOffset, offsetLeft),
                Math.max(minOffset, offsetTop),
                Math.max(minOffset, offsetRight),
                Math.max(minOffset, offsetBottom));

        prepareTransMatrix();
    }

    /**
     * Returns {@link SimpleChartAnimator} that connected with chart
     */
    @NotNull
    public SimpleChartAnimator getAnimator() {
        return animator;
    }

    /**
     * Animates x-axis
     *
     * @param duration duration of animation in milliseconds
     */
    public void animateX(long duration) {
        animator.animateX(duration);
    }

    /**
     * Animates y-axis
     *
     * @param duration duration of animation in milliseconds
     */
    public void animateY(long duration) {
        animator.animateY(duration);
    }

    public void animateXY(long duration) {
        animator.animatedXY(duration);
    }

    public float getMinOffset() {
        return minOffset;
    }

    public void setMinOffset(float minOffset) {
        this.minOffset = minOffset;
    }

    @Override
    public float getScaleX() {
        return viewPortHandler.getScaleX();
    }

    @Override
    public float getScaleY() {
        return viewPortHandler.getScaleY();
    }

    @NotNull
    public YAxis getYAxis() {
        return yAxis;
    }

    public float getYChartMax() {
        return yAxis.getMax();
    }

    public float getYChartMin() {
        return yAxis.getMin();
    }
}
