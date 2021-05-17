package com.pelmenstar.projktSens.shared.android;

import android.animation.ValueAnimator;

import androidx.annotation.RestrictTo;

import org.jetbrains.annotations.NotNull;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public final class _ValueAnimatorJvm {
    private _ValueAnimatorJvm() {}

    public static void setFloatValues(@NotNull ValueAnimator animator, @NotNull float[] values) {
        animator.setFloatValues(values);
    }
}
