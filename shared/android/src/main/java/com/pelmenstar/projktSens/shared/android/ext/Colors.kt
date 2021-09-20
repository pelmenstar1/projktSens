package com.pelmenstar.projktSens.shared.android.ext

import androidx.annotation.ColorInt

@ColorInt
inline fun Int.withAlpha(newAlpha: Int): Int {
    return (this and 0x00ffffff) or (newAlpha shl 24)
}