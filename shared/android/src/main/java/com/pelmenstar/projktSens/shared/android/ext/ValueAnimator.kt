package com.pelmenstar.projktSens.shared.android.ext

import android.animation.ValueAnimator

/**
 * Do the same as [ValueAnimator.setFloatValues].
 * But header of last one in Java is like `setFloatValues(float... values) ` and because of var-args, Kotlin compiler copies an array if pass to it.
 * This method header in Java is like `setFloatValuesArray(float[] values)` and there is no var-args, so Kotlin compiler don;t copy array
 */
@Suppress("NOTHING_TO_INLINE")
inline fun ValueAnimator.setFloatValuesArray(values: FloatArray) {
    _ValueAnimatorJvm.setFloatValues(this, values)
}