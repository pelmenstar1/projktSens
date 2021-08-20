package com.pelmenstar.projktSens.chartLite;

import android.content.Context;
import android.content.res.Resources;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;

public final class Utils {
    private static final AtomicInteger initialized = new AtomicInteger();

    private static float density;

    private static int minFlingVelocity;
    private static int maxFlingVelocity;

    private Utils() {
    }

    static void init(@NotNull Context context) {
        if (!initialized.compareAndSet(0, 1)) {
            return;
        }

        ViewConfiguration conf = ViewConfiguration.get(context);
        minFlingVelocity = conf.getScaledMinimumFlingVelocity() / 1000;
        maxFlingVelocity = conf.getScaledMaximumFlingVelocity() / 1000;

        Resources res = context.getResources();
        density = res.getDisplayMetrics().density;
    }

    public static float dpToPx(float dp) {
        return dp * density;
    }

    public static float roundToNextSignificant(float number) {
        //return (Math.round(number) * 10f) / 10f;
        if (Float.isInfinite(number) || Float.isNaN(number) || number == 0f) {
            return 0f;
        }

        float d = (float) Math.ceil(Math.log10(Math.abs(number)));
        int pw = 1 - (int) d;
        float magnitude = (float) Math.pow(10, pw);
        int shifted = Math.round(number * magnitude);

        return shifted / magnitude;
    }

    public static void velocityTrackerClearIfNecessary(@NotNull MotionEvent event, @NotNull VelocityTracker tracker) {
        tracker.computeCurrentVelocity(1, maxFlingVelocity);
        int upIndex = event.getActionIndex();
        int id1 = event.getPointerId(upIndex);
        float x1 = tracker.getXVelocity(id1);
        float y1 = tracker.getYVelocity(id1);

        for (int i = 0, count = event.getPointerCount(); i < count; i++) {
            if (i == upIndex)
                continue;

            int id2 = event.getPointerId(i);
            float x = x1 * tracker.getXVelocity(id2);
            float y = y1 * tracker.getYVelocity(id2);

            if (x + y < 0) {
                tracker.clear();
                break;
            }
        }
    }

    public static int getMinimumFlingVelocity() {
        return minFlingVelocity;
    }

    public static int getMaximumFlingVelocity() {
        return maxFlingVelocity;
    }
}
