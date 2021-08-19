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
import android.view.View;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;

import com.pelmenstar.projktSens.shared.FloatPair;
import com.pelmenstar.projktSens.shared.WaitForObject;

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

    private static final WaitForObject<Handler> transThreadHandler = new WaitForObject<>();
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

        this.shape = Shape.COMMON[creationCounter % Shape.COMMON.length];
        creationCounter++;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if(transitionStoppedByDetaching) {
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
        if(oldShapeSize != shapeSize) {
            refreshShapePath();
        }
    }

    private void refreshShapePath() {
        float size = shapeSize;
        if (size > 0) {
            if (Shape.isEquilateral(shape)) {
                Path path = shapePath;
                path.rewind();

                float halfSize = size * 0.5f;

                int angles = Shape.getAnglesInEquilateralShape(shape);

                // Equilateral shape, whose sides are equal, can be drawn using circle.
                // We have to divide circle to equal parts. Count of parts = amount of sides of shape.
                // Then we connect all the points together and we get perfect shape.
                // To determine position of point on circle, we use trigonometry.
                // There is pre-computed table for some shapes which contains pre-computed values of sin, cos.
                if(Shape.trigTableIncludesShape(angles)) {
                    long[] table = Shape.TRIG_TABLE_FOR_EQUILATERAL;
                    byte[] indices = Shape.TRIG_TABLES_INDICES;

                    int startIdx = indices[angles];

                    for(int i = 0; i < angles; i++) {
                        long packed = table[startIdx + i];

                        float sin = FloatPair.getFirst(packed);
                        float cos = FloatPair.getSecond(packed);

                        float px = halfSize * sin;
                        float py = halfSize * cos;

                        if (i == 0) {
                            path.moveTo(px, py);
                        } else {
                            path.lineTo(px, py);
                        }
                    }
                } else {
                    float radPerSide = (2 * (float) Math.PI) / angles;

                    float currentAngle = radPerSide * 0.5f;
                    for (int i = 0; i < angles; i++) {
                        float sin = (float) Math.sin(currentAngle);
                        float cos = (float) Math.cos(currentAngle);

                        float px = (halfSize * sin) + halfSize;
                        float py = (halfSize * cos) + halfSize;

                        if (i == 0) {
                            path.moveTo(px, py);
                        } else {
                            path.lineTo(px, py);
                        }

                        currentAngle += radPerSide;
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
        if(Shape.isEquilateral(oldShape) || oldShape == Shape.RHOMBUS) {
            shapePath.rewind();
        }

        this.shape = shape;
        refreshShapePath();
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
        if(!transitionRunning.compareAndSet(0, 1)) {
            Log.e(TAG, "Transition is already running");
            return;
        }

        startTransitionThreadIfNot();
        postStartTransition();
    }

    public void stopTransition() {
        transitionRunning.set(0);
    }

    private void postStartTransition() {
        Handler handler = transThreadHandler.get();

        Message msg = Message.obtain();
        msg.what = MSG_START_TRANS_ON_CURRENT_THREAD;
        msg.obj = this;

        handler.sendMessage(msg);
    }

    private void startTransitionOnCurrentThread() {
        LinearColorTransition transition;
        synchronized (colorTransitionLock) {
            transition = colorTransition;
            if(transition == null) {
                Log.w(TAG, "Transition object is null, but animation was started");
                return;
            }
        }

        long minTimeBetweenFrames = 1_000_000_000 / transition.getFramesPerColor();
        long lastFrameTime = System.nanoTime();
        while(transitionRunning.get() == 1) {
            long currentTime = System.nanoTime();

            if((currentTime - lastFrameTime) >= minTimeBetweenFrames) {
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
        if(isHandlerThreadStarted.compareAndSet(0, 1)) {
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

        if(Shape.isEquilateral(s) || shape == Shape.RHOMBUS) {
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

        private static final int[] COMMON = new int[] {
                RECT,
                CIRCLE,
                PENTAGON,
                HEXAGON,
                HEPTAGON,
                TRIANGLE,
                OCTAGON,
                RHOMBUS,
        };

        // Generated by script.
        // In 64 bits packed two numbers:
        // 64..32 bits - sin(x) + 1
        // 32..0 bits - cos(x) + 1
        // --------------------------
        // x here is an angle which is needed to draw a shape.
        // First three elements is sin, cos of angles which are needed to draw triangle
        // Then goes sin, cos for pentagon (shape with 5 sides), then goes values for hexagon (6 sides) and so on,
        // until octagon (8 sides)
        private static final long[] TRIG_TABLE_FOR_EQUILATERAL = new long[] {
                0x3fc000003feed9ecL,
                0x3f7fffffL,
                0x3fbfffff3e0930a0L,
                0x3fe78dde3fcb3c8cL,
                0x3f30e4423ff9bc38L,
                0x3f7fffffL,
                0x3f30e4423d487900L,
                0x3fe78dde3ed30dceL,
                0x3feed9ec3fc00000L,
                0x3f7fffff40000000L,
                0x3e0930a43fc00000L,
                0x3e0930a43f000000L,
                0x3f80000000000000L,
                0x3feed9eb3efffffaL,
                0x3ff352f23fb78981L,
                0x3f9c7b903ffcca70L,
                0x3ec0c5f23fe4130eL,
                0x3f800001L,
                0x3ec0c5ea3e5f679cL,
                0x3f9c7b8e3ccd63c0L,
                0x3ff352f13f10ecf8L,
                0x3ff641af3fb0fbc6L,
                0x3fb0fbc53ff641afL,
                0x3f1e08743ff641afL,
                0x3d9be5003fb0fbc4L,
                0x3d9be5183f1e0872L,
                0x3f1e087a3d9be4f8L,
                0x3fb0fbc73d9be518L,
                0x3ff641b03f1e0876L,
        };

        // Represents start index in TRIG_TABLE_FOR_EQUILATERAL for shape.
        // Index in the array represents amount of angles.
        // For instance, if you want to get start index for triangle, you call TRIG_TABLES_INDICES[3]
        private static final byte[] TRIG_TABLES_INDICES = new byte[] {
                -1,-1,-1,0,-1,3,8,14,21,
        };

        private static boolean trigTableIncludesShape(int angles) {
            return angles <= 8;
        }

        public static int createEquilateralShape(int angles) {
            if(angles < 3 || angles == 4) {
                throw new IllegalArgumentException("angles");
            }

            return EQUILATERAL_SHAPE_BIT | angles;
        }

        public static int getAnglesInEquilateralShape(int shape) {
            if(!isEquilateral(shape)) {
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

            transThreadHandler.set(new TransitionThreadHandler(looper));

            Looper.loop();
        }
    }

    private static final class TransitionThreadHandler extends Handler {
        public TransitionThreadHandler(@NotNull Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            if(msg.what == MSG_START_TRANS_ON_CURRENT_THREAD) {
                TransitionView v = (TransitionView) msg.obj;
                v.startTransitionOnCurrentThread();
            }
        }
    }
}
