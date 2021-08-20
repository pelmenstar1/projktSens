package com.pelmenstar.projktSens.chartLite;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.os.Build;
import android.util.FloatProperty;
import android.util.Property;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.pelmenstar.projktSens.shared.MyMath;

import org.jetbrains.annotations.NotNull;

/**
 * Responsible for simple chart animations by axis
 */
public final class SimpleChartAnimator {
    private static final float[] ANIMATOR_VALUES = new float[]{0f, 1f};

    @NotNull
    private final View chart;

    private float phaseX = 1f;
    private float phaseY = 1f;

    private static final LinearInterpolator LINEAR_INTERPOLATOR = new LinearInterpolator();

    @NotNull
    public static final Property<SimpleChartAnimator, Float> PHASE_X;

    @NotNull
    public static final Property<SimpleChartAnimator, Float> PHASE_Y;

    @NotNull
    private static final PropertyValuesHolder VALUES_HOLDER;

    static {
        if(Build.VERSION.SDK_INT >= 24) {
            PHASE_X = new FloatProperty<SimpleChartAnimator>("phaseX") {
                @Override
                public void setValue(@NotNull SimpleChartAnimator object, float value) {
                    object.setPhaseX(value);
                }

                @Override
                @NotNull
                public Float get(@NotNull SimpleChartAnimator object) {
                    return object.phaseX;
                }
            };
            PHASE_Y = new FloatProperty<SimpleChartAnimator>("phaseY") {
                @Override
                public void setValue(@NotNull SimpleChartAnimator object, float value) {
                    object.setPhaseY(value);
                }

                @Override
                @NotNull
                public Float get(@NotNull SimpleChartAnimator object) {
                    return object.phaseY;
                }
            };
        } else {
            PHASE_X = new Property<SimpleChartAnimator, Float>(Float.class, "phaseX") {
                @Override
                public void set(@NotNull SimpleChartAnimator object, @NotNull Float value) {
                    object.setPhaseX(value);
                }

                @Override
                @NotNull
                public Float get(@NotNull SimpleChartAnimator object) {
                    return object.phaseX;
                }
            };
            PHASE_Y = new Property<SimpleChartAnimator, Float>(Float.class, "phaseY") {
                @Override
                public void set(@NotNull SimpleChartAnimator object, @NotNull Float value) {
                    object.setPhaseY(value);
                }

                @Override
                @NotNull
                public Float get(@NotNull SimpleChartAnimator object) {
                    return object.phaseY;
                }
            };
        }

        VALUES_HOLDER =  PropertyValuesHolder.ofFloat((Property<?, Float>) null, ANIMATOR_VALUES);
    }

    public SimpleChartAnimator(@NotNull LineChart chart) {
        this.chart = chart;
    }

    private void animateAxis(
            long duration,
            @NotNull Property<SimpleChartAnimator, Float> property
    ) {
        VALUES_HOLDER.setProperty(property);

        ObjectAnimator animator = new ObjectAnimator();
        animator.setTarget(this);
        animator.setProperty(property);
        animator.setDuration(duration);
        animator.setValues(VALUES_HOLDER);
        animator.setInterpolator(LINEAR_INTERPOLATOR);
        animator.start();
    }

    public float getPhaseX() {
        return phaseX;
    }

    public void setPhaseX(float phaseX) {
        this.phaseX = MyMath.clamp(phaseX, 0f, 1f);
        chart.invalidate();
    }

    public float getPhaseY() {
        return phaseY;
    }

    public void setPhaseY(float phaseY) {
        this.phaseY = MyMath.clamp(phaseY, 0f, 1f);
        chart.invalidate();
    }

    public void animateX(long duration) {
        animateAxis(duration, PHASE_X);
    }

    public void animateY(long duration) {
        animateAxis(duration, PHASE_Y);
    }
}
