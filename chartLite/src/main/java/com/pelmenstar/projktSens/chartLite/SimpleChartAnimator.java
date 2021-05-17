package com.pelmenstar.projktSens.chartLite;

import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.pelmenstar.projktSens.shared.MyMath;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Responsible for simple chart animations by axis
 */
public final class SimpleChartAnimator {
    private static final float[] ANIMATOR_VALUES = new float[] { 0f, 1f };

    @Nullable
    private ValueAnimator.AnimatorUpdateListener _xUpdateListener;

    @Nullable
    private ValueAnimator.AnimatorUpdateListener _yUpdateListener;

    private float phaseX = 1f;
    private float phaseY = 1f;

    @NotNull
    private final View view;

    public SimpleChartAnimator(@NotNull View view) {
        this.view = view;
    }

    public float getPhaseX() {
        return phaseX;
    }

    public void setPhaseX(float phaseX) {
        this.phaseX = MyMath.clamp(phaseX, 0f, 1f);
    }

    public float getPhaseY() {
        return phaseY;
    }

    public void setPhaseY(float phaseY) {
        this.phaseY = MyMath.clamp(phaseY, 0f, 1f);
    }

    public void animateX(long duration) {
       animatorAxis(duration, xUpdateListener());
    }

    public void animateY(long duration) {
        animatorAxis(duration, yUpdateListener());
    }

    @NotNull
    private ValueAnimator.AnimatorUpdateListener xUpdateListener() {
        if(_xUpdateListener == null) {
            _xUpdateListener = animator -> {
                phaseX = (Float)animator.getAnimatedValue();
                view.invalidate();
            };
        }

        return _xUpdateListener;
    }

    @NotNull
    private ValueAnimator.AnimatorUpdateListener yUpdateListener() {
        if(_yUpdateListener == null) {
            _yUpdateListener = animator -> {
                phaseY = (Float)animator.getAnimatedValue();
                view.invalidate();
            };
        }

        return _yUpdateListener;
    }

    private static void animatorAxis(long duration, @NotNull ValueAnimator.AnimatorUpdateListener updateListener) {
        ValueAnimator animator = new ValueAnimator();
        animator.setDuration(duration);
        animator.setFloatValues(ANIMATOR_VALUES);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(updateListener);
        animator.start();
    }
}
