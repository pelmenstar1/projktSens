package com.pelmenstar.projktSens.shared.android.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;

import com.pelmenstar.projktSens.shared.FloatPair;
import com.pelmenstar.projktSens.shared.InvokeOnFirstSet;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicInteger;

public final class TransitionView extends View {
    private static final String TAG = "TransitionView";

    private static int creationCounter = 0;

    private static final int MSG_START_TRANS_ON_CURRENT_THREAD = 0;

    private int shape;
    private float shapeSize;
    private final Paint shapePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path shapePath = new Path();

    @Nullable
    private volatile LinearColorTransition colorTransition;

    private final Object colorTransitionLock = new Object();

    private final AtomicInteger transitionRunning = new AtomicInteger();
    private boolean transitionStoppedByDetaching = false;

    private static final InvokeOnFirstSet<Handler> transThreadHandler = new InvokeOnFirstSet<>();
    private static final AtomicInteger isHandlerThreadStarted = new AtomicInteger();

    public TransitionView(@NotNull Context context) {
        this(context, null, 0, 0);
    }

    public TransitionView(@NotNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0, 0);
    }

    public TransitionView(
            @NotNull Context context,
            @Nullable AttributeSet attrs,
            @AttrRes int defStyleAttr
    ) {
        this(context, attrs, defStyleAttr, 0);
    }

    public TransitionView(
            @NotNull Context context,
            @Nullable AttributeSet attrs,
            @AttrRes int defStyleAttr,
            @StyleRes int defStyleRes
    ) {
        super(context, attrs, defStyleAttr, defStyleRes);

        setNextShapeInSequence();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (transitionStoppedByDetaching) {
            transitionStoppedByDetaching = false;
            startTransition();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        transitionStoppedByDetaching = true;
        stopTransition();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        float oldShapeSize = shapeSize;
        shapeSize = Math.max(w, h);
        if (oldShapeSize != shapeSize) {
            refreshShapePath();
        }
    }

    @Override
    public boolean onTouchEvent(@NotNull MotionEvent event) {
        if(event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            performClick();
        }
        return true;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        setNextShapeInSequence();

        return true;
    }

    private void refreshShapePath() {
        float size = shapeSize;
        if (size > 0) {
            if (Shape.isEquilateral(shape)) {
                Path path = shapePath;
                path.rewind();

                float halfSize = size * 0.5f;

                // Equilateral shape, whose sides are equal, can be drawn using circle.
                // We have to divide circle to equal parts. Count of parts = amount of sides of shape.
                // Then we connect all the points together and we get perfect shape.
                // To determine position of point on circle, we use trigonometry.
                int angles = Shape.getAnglesInEquilateralShape(shape);
                long[] sinCosTable = Shape.computeShapeSinCosTable(angles);

                for (int i = 0; i < angles; i++) {
                    long sinCos = sinCosTable[i];

                    float sin = FloatPair.getFirst(sinCos);
                    float cos = FloatPair.getSecond(sinCos);

                    float px = halfSize * (sin + 1);
                    float py = halfSize * (cos + 1);

                    if (i == 0) {
                        path.moveTo(px, py);
                    } else {
                        path.lineTo(px, py);
                    }
                }

                path.close();
            } else if (shape == Shape.RHOMBUS) {
                Path path = shapePath;
                path.rewind();

                float halfSize = size * 0.5f;

                path.moveTo(size, halfSize);
                path.lineTo(halfSize, 0f);
                path.lineTo(0f, halfSize);
                path.lineTo(halfSize, size);
                path.close();
            }
        }
    }

    public int getShape() {
        return shape;
    }

    public void setShape(int shape) {
        int oldShape = this.shape;
        if (Shape.isEquilateral(oldShape) || oldShape == Shape.RHOMBUS) {
            shapePath.rewind();
        }

        this.shape = shape;
        refreshShapePath();
    }

    private void setNextShapeInSequence() {
        setShape(Shape.COMMON[creationCounter % Shape.COMMON.length]);
        creationCounter++;
    }

    @Nullable
    public LinearColorTransition getColorTransition() {
        synchronized (colorTransitionLock) {
            return colorTransition;
        }
    }

    public void setColorTransition(@Nullable LinearColorTransition transition) {
        synchronized (colorTransitionLock) {
            this.colorTransition = transition;
        }
    }

    public void startTransition() {
        if (!transitionRunning.compareAndSet(0, 1)) {
            Log.e(TAG, "Transition is already running");
            return;
        }

        startTransitionThreadIfNot();

        synchronized (transThreadHandler) {
            Handler handler = transThreadHandler.get();
            if (handler == null) {
                transThreadHandler.setCallback(this::postStartTransition);
            } else {
                postStartTransition();
            }
        }
    }

    public void stopTransition() {
        transitionRunning.set(0);
    }

    private void postStartTransition() {
        Handler handler = transThreadHandler.getOrThrowIfNull();

        Message msg = Message.obtain();
        msg.what = MSG_START_TRANS_ON_CURRENT_THREAD;
        msg.obj = this;

        handler.sendMessage(msg);
    }

    private void startTransitionOnCurrentThread() {
        LinearColorTransition transition;
        synchronized (colorTransitionLock) {
            transition = colorTransition;
            if (transition == null) {
                Log.w(TAG, "Transition object is null, but animation was started");
                return;
            }
        }

        long minTimeBetweenFrames = 1_000_000_000 / transition.getFramesPerColor();
        long lastFrameTime = System.nanoTime();
        while (transitionRunning.get() == 1) {
            long currentTime = System.nanoTime();

            if ((currentTime - lastFrameTime) >= minTimeBetweenFrames) {
                int color = transition.nextColor();
                shapePaint.setColor(color);

                postInvalidate();
                lastFrameTime = currentTime;
            }
            try {
                //noinspection BusyWait
                Thread.sleep(1);
            } catch (InterruptedException e) {
                // why to handle this?
            }
        }
    }

    private static void startTransitionThreadIfNot() {
        if (isHandlerThreadStarted.compareAndSet(0, 1)) {
            new TransitionThread().start();
        }
    }

    @Override
    protected void onDraw(@NotNull Canvas c) {
        float size = shapeSize;
        int s = shape;

        switch (s) {
            case Shape.RECT: {
                c.drawRect(0f, 0f, size, size, shapePaint);
                break;
            }
            case Shape.CIRCLE: {
                c.drawOval(0f, 0f, size, size, shapePaint);
                break;
            }
        }

        if (Shape.isEquilateral(s) || shape == Shape.RHOMBUS) {
            c.drawPath(shapePath, shapePaint);
        }
    }

    public static final class Shape {
        private static final int EQUILATERAL_SHAPE_BIT = 1 << 31;

        public static final int RECT = 0;
        public static final int CIRCLE = 1;
        public static final int RHOMBUS = 2;

        public static final int TRIANGLE = EQUILATERAL_SHAPE_BIT | 3;
        public static final int PENTAGON = EQUILATERAL_SHAPE_BIT | 5;
        public static final int HEXAGON = EQUILATERAL_SHAPE_BIT | 6;
        public static final int HEPTAGON = EQUILATERAL_SHAPE_BIT | 7;
        public static final int OCTAGON = EQUILATERAL_SHAPE_BIT | 8;

        private static final int[] COMMON = new int[]{
                RECT,
                CIRCLE,
                PENTAGON,
                HEXAGON,
                HEPTAGON,
                TRIANGLE,
                OCTAGON,
                RHOMBUS,
        };

        private static final SparseArray<long[]> sinCosTableCache = new SparseArray<>();

        public static long @NotNull [] computeShapeSinCosTable(int angles) {
            int cacheIndex = sinCosTableCache.indexOfKey(angles);
            if(cacheIndex >= 0) {
                return sinCosTableCache.valueAt(cacheIndex);
            }

            float radPerSide = (2 * (float) Math.PI) / angles;

            float currentAngle = radPerSide * 0.5f;
            long[] sinCosTable = new long[angles];

            for (int i = 0; i < angles; i++) {
                double dAngle = currentAngle;
                float sin = (float) Math.sin(dAngle);
                float cos = (float) Math.cos(dAngle);

                sinCosTable[i] = FloatPair.of(sin, cos);

                currentAngle += radPerSide;
            }

            sinCosTableCache.put(angles, sinCosTable);

            return sinCosTable;
        }

        public static int createEquilateralShape(int angles) {
            if (angles < 3 || angles == 4) {
                throw new IllegalArgumentException("angles");
            }

            return EQUILATERAL_SHAPE_BIT | angles;
        }

        public static int getAnglesInEquilateralShape(int shape) {
            if (!isEquilateral(shape)) {
                throw new IllegalArgumentException("shape is not equilateral");
            }

            return shape & (~EQUILATERAL_SHAPE_BIT);
        }

        public static boolean isEquilateral(int shape) {
            return (shape & EQUILATERAL_SHAPE_BIT) != 0;
        }
    }

    private static final class TransitionThread extends Thread {
        public TransitionThread() {
            setName("TRANS-THREAD");
        }

        @Override
        public void run() {
            Looper.prepare();
            Looper looper = Looper.myLooper();

            synchronized (transThreadHandler) {
                transThreadHandler.set(new TransitionThreadHandler(looper));
            }

            Looper.loop();
        }
    }

    private static final class TransitionThreadHandler extends Handler {
        public TransitionThreadHandler(@NotNull Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == MSG_START_TRANS_ON_CURRENT_THREAD) {
                TransitionView v = (TransitionView) msg.obj;
                v.startTransitionOnCurrentThread();
            }
        }
    }
}
