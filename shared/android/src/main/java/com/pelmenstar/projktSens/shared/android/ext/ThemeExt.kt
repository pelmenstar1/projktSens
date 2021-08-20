package com.pelmenstar.projktSens.shared.android.ext

import android.content.Context
import android.content.res.Resources
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.annotation.StyleableRes
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

inline fun Context.obtainStyledAttributes(
    @StyleRes resId: Int,
    @AttrRes attr: Int,
    block: (TypedArray) -> Unit
) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    theme.obtainStyledAttributes(resId, attr, block)
}

inline fun Resources.Theme.obtainStyledAttributes(
    @StyleRes resId: Int,
    @AttrRes attr: Int,
    block: (TypedArray) -> Unit
) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    obtainStyledAttributes(resId, intArrayOf(attr), block)
}

inline fun Context.obtainStyledAttributes(
    @StyleRes resId: Int,
    @StyleableRes attrs: IntArray,
    block: (TypedArray) -> Unit
) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    theme.obtainStyledAttributes(resId, attrs, block)
}

/**
 * Return a TypedArray holding the values defined by the style resource id which are listed in attrs.
 * [TypedArray] can be used in [block].
 * [TypedArray] is automatically recycles after executing [block] (even if exception occurred while executing last one)
 */
inline fun Resources.Theme.obtainStyledAttributes(
    @StyleRes resId: Int,
    @StyleableRes attrs: IntArray,
    block: (TypedArray) -> Unit
) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    val a = obtainStyledAttributes(resId, attrs)

    try {
        block(a)
    } finally {
        a.recycle()
    }
}

inline fun Context.obtainStyledAttributes(
    set: AttributeSet,
    @StyleableRes attrs: IntArray,
    @AttrRes defStyleAttr: Int,
    @StyleRes defStyleRes: Int,
    block: (TypedArray) -> Unit
) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    theme.obtainStyledAttributes(set, attrs, defStyleAttr, defStyleRes, block)
}

inline fun Resources.Theme.obtainStyledAttributes(
    set: AttributeSet,
    @StyleableRes attrs: IntArray,
    @AttrRes defStyleAttr: Int,
    @StyleRes defStyleRes: Int,
    block: (TypedArray) -> Unit
) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    val a = obtainStyledAttributes(set, attrs, defStyleAttr, defStyleRes)

    try {
        block(a)
    } finally {
        a.recycle()
    }
}
