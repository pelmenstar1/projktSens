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
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;

import com.pelmenstar.projktSens.shared.EmptyArray;
import com.pelmenstar.projktSens.shared.InvokeOnFirstSet;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicInteger;

public final class TransitionView extends View {
    @NotNull
    private static final String TAG = "TransitionView";

    private static int creationCounter = 0;

    private static final int ANIMATION_CYCLE_DURATION = 2000;
    private static final float INV_ANIMATION_CYCLE_DURATION = 1f / ANIMATION_CYCLE_DURATION;

    private static final int MSG_START_TRANS_ON_CURRENT_THREAD = 0;

    private int shape;
    private float shapeSize;

    @NotNull
    private final Paint shapePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    @NotNull
    private final Path shapePath = new Path();

    @Nullable
    private volatile LinearColorTransition colorTransition;

    @NotNull
    private final Object colorTransitionLock = new Object();

    @NotNull
    private final AtomicInteger transitionRunning = new AtomicInteger();
    private boolean transitionStoppedByDetaching = false;

    @NotNull
    private static final InvokeOnFirstSet<Handler> transThreadHandler = new InvokeOnFirstSet<>();

    @NotNull
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
            Path path = shapePath;
            path.rewind();

            float halfSize = size * 0.5f;

            // Equilateral shape, whose sides are equal, can be drawn using circle.
            // We have to divide circle to equal parts. Count of parts = amount of sides of shape.
            // Then we connect all the points together and we get perfect shape.
            // To determine position of point on circle, we use trigonometry.
            int angles = shape;
            int startIndex = Shape.getPositionInSinCosTable(angles);
            int[] tableValues = Shape.sinCosTable.values;

            for (int i = startIndex; i < startIndex + angles * 2; i += 2) {
                float sin = Float.intBitsToFloat(tableValues[i]);
                float cos = Float.intBitsToFloat(tableValues[i + 1]);

                float px = halfSize * (sin + 1);
                float py = halfSize * (cos + 1);

                if (i == startIndex) {
                    path.moveTo(px, py);
                } else {
                    path.lineTo(px, py);
                }
            }

            path.close();
        }
    }

    public int getShape() {
        return shape;
    }

    public void setShape(int shape) {
        if (this.shape != shape) {
            this.shape = shape;
            refreshShapePath();
        }
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

        long startTime = System.currentTimeMillis();
        boolean forward = true;

        while (transitionRunning.get() == 1) {
            long nowTime = System.currentTimeMillis();

            float k = (float)(nowTime - startTime) * INV_ANIMATION_CYCLE_DURATION;

            if(forward) {
                if(k >= 1) {
                    startTime = nowTime;
                    k = 0.99f;
                    forward = false;
                }
            } else {
                k = 1 - k;
                if(k <= 0) {
                    startTime = nowTime;
                    k = 0;
                    forward = true;
                }
            }

            int color = transition.colorAt(k);
            shapePaint.setColor(color);

            postInvalidate();

            try {
                //noinspection BusyWait
                Thread.sleep(13);
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
        c.drawPath(shapePath, shapePaint);
    }

    public static final class Shape {
        public static final int TRIANGLE = 3;
        public static final int PENTAGON = 5;
        public static final int HEXAGON = 6;
        public static final int HEPTAGON = 7;
        public static final int OCTAGON = 8;

        private static final int @NotNull [] COMMON = new int[]{
                PENTAGON,
                HEXAGON,
                HEPTAGON,
                TRIANGLE,
                OCTAGON,
        };

        @NotNull
        private static final FlatSinCosTable sinCosTable = new FlatSinCosTable();

        private static int getPositionInSinCosTable(int angles) {
            int cacheIndex = sinCosTable.getIndex(angles);
            if(cacheIndex >= 0) {
                return cacheIndex;
            }

            float radPerSide = (2 * (float) Math.PI) / angles;

            float currentAngle = radPerSide * 0.5f;
            int startIndex = sinCosTable.allocateNewBlock(angles);
            int[] values = sinCosTable.values;

            for(int i = startIndex; i < startIndex + angles * 2; i += 2) {
                double dAngle = currentAngle;
                float sin = (float) Math.sin(dAngle);
                float cos = (float) Math.cos(dAngle);

                values[i] = Float.floatToRawIntBits(sin);
                values[i + 1] = Float.floatToRawIntBits(cos);

                currentAngle += radPerSide;
            }

            return startIndex;
        }

        public static int createEquilateralShape(int angles) {
            if (angles < 3) {
                throw new IllegalArgumentException("angles");
            }

            return angles;
        }
    }

    private static final class FlatSinCosTable {
        private int @NotNull [] values = EmptyArray.INT;

        public int allocateNewBlock(int angles) {
            int startIndex = values.length + 1;
            int[] newValues = new int[startIndex + angles * 2];
            System.arraycopy(values, 0, newValues, 0, values.length);
            newValues[values.length] = angles;

            values = newValues;

            return startIndex;
        }

        public int getIndex(int angles) {
            int index = 0;
            while(index < values.length) {
                int blockAngles = values[index];

                if(angles == blockAngles) {
                    return index + 1;
                }

                index += blockAngles * 2 + 1;
            }

            return -1;
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
