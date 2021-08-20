package com.pelmenstar.projktSens.shared.android.ext

import android.os.Bundle
import android.os.Parcelable

private fun throwKeyNotExists(key: String): Nothing {
    throw NullPointerException("Key '$key' is null or not exists")
}

fun Bundle.getIntArrayNotNull(key: String): IntArray {
    return getIntArray(key) ?: throwKeyNotExists(key)
}

fun Bundle.getStringArrayNotNull(key: String): Array<String> {
    return getStringArray(key) ?: throwKeyNotExists(key)
}

fun <T : Parcelable> Bundle.getParcelableNotNull(key: String): T {
    return getParcelable(key) ?: throwKeyNotExists(key)
}