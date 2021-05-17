@file:Suppress("NOTHING_TO_INLINE")

package com.pelmenstar.projktSens.shared.android

import android.os.Parcel

inline fun Parcel.readNonNullString(): String {
    return readString() ?: throw NullPointerException()
}