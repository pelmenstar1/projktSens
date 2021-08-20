package com.pelmenstar.projktSens.chartLite;

import android.graphics.Matrix;
import android.view.MotionEvent;
import android.view.VelocityTracker;

import org.jetbrains.annotations.NotNull;

public final class ChartTouchListener {
    public static final int TOUCH_MODE_NONE = 0;
    public static final int TOUCH_MODE_DRAG = 1;
    private static final int ZOOM_BIT = 1 << 31;
    public static final int TOUCH_MODE_X_ZOOM = 2 | ZOOM_BIT;
    public static final int TOUCH_MODE_Y_ZOOM = 3 | ZOOM_BIT;
    public static final int TOUCH_MODE_XY_ZOOM = 4 | ZOOM_BIT;
    public static final int TOUCH_MODE_POST_ZOOM = 5 | ZOOM_BIT;

    private final Matrix matrix;
    private final Matrix savedMatrix = new Matrix();
    @NotNull
    private final LineChart chart;
    @NotNull
    private final VelocityTracker velocityTracker;
    private final float dragTriggerDist;
    private final float minScalePointerDist;
    private final ViewPortHandler viewPortHandler;
    private int touchMode = TOUCH_MODE_NONE;
    private float touchStartX;
    private float touchStartY;
    private float touchCenterX;
    private float touchCenterY;
    private float savedXDist = 1f;

    //private float decCurrentPointX;
    //private float decCurrentPointY;
    private float savedYDist = 1f;
    private float savedDist = 1f;
    private long decelerationLastTime = 0;
    private float decVelocityX;
    private float decVelocityY;

    public ChartTouchListener(@NotNull LineChart chart) {
        this.chart = chart;
        viewPortHandler = chart.getViewPortHandler();
        matrix = viewPortHandler.getMatrixTouch();
        velocityTracker = VelocityTracker.obtain();

        dragTriggerDist = Utils.dpToPx(3f);
        minScalePointerDist = Utils.dpToPx(3.5f);
    }

    public void onTouch(@NotNull MotionEvent event) {
        velocityTracker.addMovement(event);
        int action = event.getActionMasked();

        if (action == MotionEvent.ACTION_CANCEL) {
            velocityTracker.clear();
        }

        if (!chart.isDragEnabled() && !chart.isScaleEnabled()) {
            return;
        }

        // Handle touch events here...
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                decVelocityX = 0f;
                decVelocityY = 0f;

                saveTouchStart(event.getX(), event.getY());

                break;
            }

            case MotionEvent.ACTION_POINTER_DOWN: {
                if (event.getPointerCount() >= 2) {
                    chart.disableScroll();

                    float ex0 = event.getX();
                    float ey0 = event.getY();

                    float ex1 = event.getX(1);
                    float ey1 = event.getY(1);

                    saveTouchStart(ex0, ey0);

                    savedXDist = Math.abs(ex0 - ex1);
                    savedYDist = Math.abs(ey0 - ey1);
                    savedDist = (float) Math.sqrt(savedXDist * savedXDist + savedYDist * savedYDist);

                    touchCenterX = (ex0 + ex1) * 0.5f;
                    touchCenterY = (ey0 + ey1) * 0.5f;

                    if (savedDist > 10f) {
                        if (chart.isPinchZoomEnabled()) {
                            touchMode = TOUCH_MODE_XY_ZOOM;
                        } else {
                            boolean scaleXEnabled = chart.isScaleXEnabled();

                            if (scaleXEnabled != chart.isScaleYEnabled()) {
                                touchMode = scaleXEnabled ? TOUCH_MODE_X_ZOOM : TOUCH_MODE_Y_ZOOM;
                            } else {
                                touchMode = savedXDist > savedYDist ? TOUCH_MODE_X_ZOOM : TOUCH_MODE_Y_ZOOM;
                            }
                        }
                    }
                }
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                float ex0 = event.getX();
                float ey0 = event.getY();

                if (touchMode == TOUCH_MODE_DRAG) {
                    chart.disableScroll();

                    if (chart.isDragEnabled()) {
                        float dx = 0f;
                        float dy = 0f;

                        if (chart.isDragXEnabled()) {
                            dx = ex0 - touchStartX;
                        }

                        if (chart.isDragYEnabled()) {
                            dy = ey0 - touchStartY;
                        }

                        performDrag(dx, dy);
                    }
                } else if (touchMode == TOUCH_MODE_NONE) {
                    if (chart.isDragEnabled() && !chart.isFullyZoomedOut()) {
                        float distX = Math.abs(ex0 - touchStartX);
                        float distY = Math.abs(ey0 - touchStartY);

                        float dist = (float) Math.sqrt(distX * distX + distY * distY);

                        if (dist > dragTriggerDist) {
                            // Disable dragging in a direction that's disallowed
                            if ((chart.isDragXEnabled() || distY >= distX) && (chart.isDragYEnabled() || distY <= distX)) {
                                touchMode = TOUCH_MODE_DRAG;
                            }
                        }
                    }
                } else if (isZoomTouchMode() && chart.isScaleEnabled() && event.getPointerCount() >= 2) {
                    chart.disableScroll();

                    float distX = Math.abs(ex0 - event.getX(1));
                    float distY = Math.abs(ey0 - event.getY(1));

                    float dist = (float) Math.sqrt(distX * distX + distY * distY);

                    if (dist > minScalePointerDist) {
                        float tx = touchCenterX - viewPortHandler.contentLeft();
                        float ty = touchCenterY - viewPortHandler.contentTop();

                        switch (touchMode) {
                            case TOUCH_MODE_XY_ZOOM: {
                                float scale = dist / savedDist;

                                boolean canZoomMoreX;
                                boolean canZoomMoreY;

                                if (scale < 1) {
                                    canZoomMoreX = viewPortHandler.canZoomOutMoreX();
                                    canZoomMoreY = viewPortHandler.canZoomOutMoreY();
                                } else {
                                    canZoomMoreX = viewPortHandler.canZoomInMoreX();
                                    canZoomMoreY = viewPortHandler.canZoomInMoreY();
                                }

                                if (canZoomMoreX || canZoomMoreY) {
                                    float scaleX = chart.isScaleXEnabled() ? scale : 1f;
                                    float scaleY = chart.isScaleYEnabled() ? scale : 1f;

                                    matrix.set(savedMatrix);
                                    matrix.postScale(scaleX, scaleY, tx, ty);
                                }

                                break;
                            }
                            case TOUCH_MODE_X_ZOOM: {
                                if (chart.isScaleXEnabled()) {
                                    float scaleX = distX / savedXDist; // x-axis scale

                                    boolean canZoomMoreX = scaleX < 1 ? viewPortHandler.canZoomOutMoreX() : viewPortHandler.canZoomInMoreX();

                                    if (canZoomMoreX) {
                                        matrix.set(savedMatrix);
                                        matrix.postScale(scaleX, 1f, tx, ty);
                                    }
                                }
                                break;
                            }
                            case TOUCH_MODE_Y_ZOOM: {
                                if (chart.isScaleYEnabled()) {
                                    float scaleY = distY / savedYDist; // y-axis scale

                                    boolean canZoomMoreY = scaleY < 1 ? viewPortHandler.canZoomOutMoreY() : viewPortHandler.canZoomInMoreY();

                                    if (canZoomMoreY) {
                                        matrix.set(savedMatrix);
                                        matrix.postScale(1f, scaleY, tx, ty);
                                    }
                                }

                                break;
                            }
                        }
                    }
                }

                break;
            }

            //case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                if (touchMode == TOUCH_MODE_DRAG && chart.isDragDecelerationEnabled()) {
                    int pointerId = event.getPointerId(0);
                    velocityTracker.computeCurrentVelocity(1, Utils.getMaximumFlingVelocity());

                    float velocityX = velocityTracker.getXVelocity(pointerId);
                    float velocityY = velocityTracker.getYVelocity(pointerId);

                    if (Math.abs(velocityX) > Utils.getMinimumFlingVelocity() || Math.abs(velocityY) > Utils.getMinimumFlingVelocity()) {
                        decelerationLastTime = System.currentTimeMillis();

                        decVelocityX = velocityX;
                        decVelocityY = velocityY;
                    }
                }

                if (isZoomTouchMode()) {
                    // Range might have changed, which means that Y-axis labels
                    // could have changed in size, affecting Y-axis size.
                    // So we need to recalculate offsets.
                    chart.calculateOffsets();
                }

                touchMode = TOUCH_MODE_NONE;
                chart.enableScroll();

                velocityTracker.clear();

                break;
            }

            case MotionEvent.ACTION_POINTER_UP: {
                Utils.velocityTrackerClearIfNecessary(event, velocityTracker);

                touchMode = TOUCH_MODE_POST_ZOOM;
                break;
            }

            case MotionEvent.ACTION_CANCEL: {
                touchMode = TOUCH_MODE_NONE;
                break;
            }
        }

        chart.getViewPortHandler().refresh(matrix);
        chart.invalidate();
    }

    private void saveTouchStart(float x, float y) {
        savedMatrix.set(matrix);

        touchStartX = x;
        touchStartY = y;
    }

    private void performDrag(float dx, float dy) {
        matrix.set(savedMatrix);

        matrix.postTranslate(dx, dy);
    }

    private boolean isZoomTouchMode() {
        return (touchMode & ZOOM_BIT) != 0;
    }

    public void computeScroll() {
        if (decVelocityX == 0f && decVelocityY == 0f) {
            return; // There's no deceleration in progress
        }

        long currentTime = System.currentTimeMillis();
        float timeInterval = (float) (currentTime - decelerationLastTime);

        float distanceX = decVelocityX * timeInterval;
        float distanceY = decVelocityY * timeInterval;

        //decCurrentPointX += distanceX;
        //decCurrentPointY += distanceY;

        float dragDistanceX = chart.isDragXEnabled() ? distanceX - touchStartX : 0f;
        float dragDistanceY = chart.isDragYEnabled() ? distanceY - touchStartY : 0f;

        performDrag(dragDistanceX, dragDistanceY);

        decelerationLastTime = currentTime;

        if (Math.abs(decVelocityX) <= 0.01 || Math.abs(decVelocityY) <= 0.01) {
            // Range might have changed, which means that Y-axis labels
            // could have changed in size, affecting Y-axis size.
            // So we need to recalculate offsets.
            chart.calculateOffsets();

            decVelocityX = 0f;
            decVelocityY = 0f;
        }

        chart.invalidate();
    }
}
