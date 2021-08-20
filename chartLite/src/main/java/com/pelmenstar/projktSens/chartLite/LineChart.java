package com.pelmenstar.projktSens.chartLite;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.ViewParent;

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
public class LineChart extends ViewGroup {
    protected static final int FLAG_PINCH_ZOOM_ENABLED = 1;
    protected static final int FLAG_DRAG_X_ENABLED = 1 << 2;
    protected static final int FLAG_DRAG_Y_ENABLED = 1 << 3;
    protected static final int FLAG_SCALE_X_ENABLED = 1 << 4;
    protected static final int FLAG_SCALE_Y_ENABLED = 1 << 5;
    protected static final int FLAG_CLIP_VALUES_TO_CONTENT = 1 << 6;
    protected static final int FLAG_CLIP_DATA_TO_CONTENT = 1 << 7;
    protected static final int FLAG_TOUCH_ENABLED = 1 << 8;
    protected static final int FLAG_DRAG_DECELERATION_ENABLED = 1 << 9;
    protected static final int FLAG_AUTO_ANIMATED = 1 << 10;

    private static final int FLAG_OFFSETS_CALCULATED = 1 << 11;
    private static final int FLAG_FIRST_RENDER = 1 << 12;
    private static final int AUTO_ANIMATE_COND = FLAG_AUTO_ANIMATED | FLAG_FIRST_RENDER;
    @NotNull
    protected final DataRef dataRef;
    protected final ChartTouchListener chartTouchListener;
    @NotNull
    protected final LineChartRenderer renderer;
    protected final ViewPortHandler viewPortHandler;
    protected final XAxis xAxis;
    protected final YAxis yAxis;
    protected final XAxisRenderer xAxisRenderer;
    protected final YAxisRenderer yAxisRenderer;
    protected final SimpleChartAnimator animator;
    protected int flags =
            FLAG_TOUCH_ENABLED |
                    FLAG_DRAG_X_ENABLED | FLAG_DRAG_Y_ENABLED |
                    FLAG_SCALE_X_ENABLED | FLAG_SCALE_Y_ENABLED |
                    FLAG_CLIP_DATA_TO_CONTENT | FLAG_DRAG_DECELERATION_ENABLED | FLAG_FIRST_RENDER;
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
        // setLayerType(View.LAYER_TYPE_HARDWARE, null);

        Utils.init(context);

        animator = new SimpleChartAnimator(this);
        dataRef = new DataRef();
        viewPortHandler = new ViewPortHandler();

        renderer = new LineChartRenderer(viewPortHandler, animator, dataRef);

        xAxis = new XAxis();
        xAxisRenderer = new XAxisRenderer(viewPortHandler, xAxis);

        yAxis = new YAxis();
        yAxisRenderer = new YAxisRenderer(viewPortHandler, yAxis);

        chartTouchListener = new ChartTouchListener(this);
    }

    private boolean isFlagEnabled(int flag) {
        return (flags & flag) != 0;
    }

    private void setFlag(int flag, boolean state) {
        if (state) {
            flags |= flag;
        } else {
            flags &= ~flag;
        }
    }

    /**
     * Returns whether auto animations is enabled
     */
    public boolean isAutoAnimated() {
        return isFlagEnabled(FLAG_AUTO_ANIMATED);
    }

    /**
     * Sets state of auto animations in chart
     */
    public void setAutoAnimated(boolean value) {
        setFlag(FLAG_AUTO_ANIMATED, value);
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
        flags &= ~FLAG_OFFSETS_CALCULATED;

        // let the chart know there is new data
        notifyDataChanged();
    }

    /**
     * Clears data. Chart become empty
     */
    public void clear() {
        dataRef.value = null;
        flags &= ~FLAG_OFFSETS_CALCULATED;

        invalidate();
    }

    /**
     * Determines whether chart is empty
     */
    public boolean isEmpty() {
        return dataRef.value == null || !dataRef.value.isContainsAnyEntry();
    }

    @Override
    public boolean onInterceptTouchEvent(@NotNull MotionEvent ev) {
        int action = ev.getActionMasked();

        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_UP) {
            ViewParent p = getParent();
            if (p != null) {
                p.requestDisallowInterceptTouchEvent(false);
            }
        }
        return false;
    }

    /**
     * Returns whether drag deceleration is enabled
     */
    public boolean isDragDecelerationEnabled() {
        return isFlagEnabled(FLAG_DRAG_DECELERATION_ENABLED);
    }

    /**
     * Sets state of drag deceleration in chart
     */
    public void setDragDecelerationEnabled(boolean enabled) {
        setFlag(FLAG_DRAG_DECELERATION_ENABLED, enabled);
    }

    /**
     * Gets x-axis
     */
    @NotNull
    public XAxis getXAxis() {
        return xAxis;
    }

    /**
     * Sets state of handling touches (if false, chart will not handle all user touches)
     */
    public void setTouchEnabled(boolean enabled) {
        setFlag(FLAG_TOUCH_ENABLED, enabled);
    }

    public void disableScroll() {
        ViewParent parent = getParent();
        if (parent != null)
            parent.requestDisallowInterceptTouchEvent(true);
    }

    public void enableScroll() {
        ViewParent parent = getParent();
        if (parent != null)
            parent.requestDisallowInterceptTouchEvent(false);

    }

    /**
     * Returns {@link ViewPortHandler} that connected with chart
     */
    @NotNull
    public ViewPortHandler getViewPortHandler() {
        return viewPortHandler;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int size = (int) Utils.dpToPx(50f);
        setMeasuredDimension(
                Math.max(getSuggestedMinimumWidth(),
                        resolveSize(size,
                                widthMeasureSpec)),
                Math.max(getSuggestedMinimumHeight(),
                        resolveSize(size,
                                heightMeasureSpec)));
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

        if ((flags & FLAG_OFFSETS_CALCULATED) == 0) { // if offsets are not calculated
            calculateOffsets();
            flags |= FLAG_OFFSETS_CALCULATED;
        }

        yAxisRenderer.computePoints();
        xAxisRenderer.computePoints();
        renderer.computePoints();

        if (yAxis.isEnabled()) {
            yAxisRenderer.computeAxis();
        }

        if (xAxis.isEnabled()) {
            xAxisRenderer.computeAxis();
        }

        xAxisRenderer.draw(c);
        yAxisRenderer.draw(c);
        renderer.draw(c);

        if ((flags & AUTO_ANIMATE_COND) == AUTO_ANIMATE_COND) {
            flags &= ~FLAG_FIRST_RENDER;
            animateX(500);
        }
    }

    private void prepareTransMatrix() {
        viewPortHandler.prepareTransform(xAxis.getMin(), xAxis.getMax(), yAxis.getMin(), yAxis.getMax());
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

    /**
     * Computes internal offsets
     */
    public void calculateOffsets() {
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

        float minOffset = Utils.dpToPx(this.minOffset);

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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        if (dataRef.value == null || (flags & FLAG_TOUCH_ENABLED) == 0) {
            return false;
        }

        chartTouchListener.onTouch(event);

        return true;
    }

    @Override
    public void computeScroll() {
        chartTouchListener.computeScroll();
    }

    /**
     * Sets scale minimum for each axis
     *
     * @param scaleX min scale of x-axis
     * @param scaleY max scale of y-axis
     */
    public void setScaleMinimum(float scaleX, float scaleY) {
        viewPortHandler.setMinimumScaleX(scaleX);
        viewPortHandler.setMinimumScaleY(scaleY);
    }

    /**
     * Determines whether dragging enabled
     */
    public boolean isDragEnabled() {
        return (flags & (FLAG_DRAG_X_ENABLED | FLAG_DRAG_Y_ENABLED)) != 0;
    }

    /**
     * Sets state of allowing dragging of chart
     */
    public void setDragEnabled(boolean enabled) {
        if (enabled) {
            flags |= (FLAG_DRAG_X_ENABLED | FLAG_DRAG_Y_ENABLED);
        } else {
            flags &= ~(FLAG_DRAG_X_ENABLED | FLAG_DRAG_Y_ENABLED);
        }
    }

    public boolean isDragXEnabled() {
        return isFlagEnabled(FLAG_DRAG_X_ENABLED);
    }

    public void setDragXEnabled(boolean enabled) {
        setFlag(FLAG_DRAG_X_ENABLED, enabled);
    }

    public boolean isDragYEnabled() {
        return isFlagEnabled(FLAG_DRAG_Y_ENABLED);
    }

    public void setDragYEnabled(boolean enabled) {
        setFlag(FLAG_DRAG_Y_ENABLED, enabled);
    }

    public boolean isScaleEnabled() {
        return (flags & (FLAG_SCALE_X_ENABLED | FLAG_SCALE_Y_ENABLED)) != 0;
    }

    public void setScaleEnabled(boolean enabled) {
        if (enabled) {
            flags |= (FLAG_SCALE_X_ENABLED | FLAG_SCALE_Y_ENABLED);
        } else {
            flags &= ~(FLAG_SCALE_X_ENABLED | FLAG_SCALE_Y_ENABLED);
        }
    }

    public boolean isScaleXEnabled() {
        return isFlagEnabled(FLAG_SCALE_X_ENABLED);
    }

    public void setScaleXEnabled(boolean enabled) {
        setFlag(FLAG_SCALE_X_ENABLED, enabled);
    }

    public boolean isScaleYEnabled() {
        return isFlagEnabled(FLAG_SCALE_Y_ENABLED);
    }

    public void setScaleYEnabled(boolean enabled) {
        setFlag(FLAG_SCALE_Y_ENABLED, enabled);
    }

    public void setClipValuesToContent(boolean enabled) {
        setFlag(FLAG_CLIP_VALUES_TO_CONTENT, enabled);
    }

    public void setClipDataToContent(boolean enabled) {
        setFlag(FLAG_CLIP_DATA_TO_CONTENT, enabled);
    }

    public boolean isClipValuesToContentEnabled() {
        return isFlagEnabled(FLAG_CLIP_VALUES_TO_CONTENT);
    }

    public boolean isClipDataToContentEnabled() {
        return isFlagEnabled(FLAG_CLIP_DATA_TO_CONTENT);
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

    public boolean isFullyZoomedOut() {
        return viewPortHandler.isFullyZoomedOut();
    }

    @NotNull
    public YAxis getYAxis() {
        return yAxis;
    }

    public void setPinchZoom(boolean enabled) {
        setFlag(FLAG_PINCH_ZOOM_ENABLED, enabled);
    }

    public boolean isPinchZoomEnabled() {
        return isFlagEnabled(FLAG_PINCH_ZOOM_ENABLED);
    }

    public float getYChartMax() {
        return yAxis.getMax();
    }

    public float getYChartMin() {
        return yAxis.getMin();
    }
}
