package com.pelmenstar.projktSens.shared.android

import android.content.Context

interface Preferences {
    fun initialize(context: Context)

    fun getInt(id: Int): Int
    fun getBoolean(id: Int): Boolean

    fun beginModifying()
    fun setInt(id: Int, value: Int)
    fun setBoolean(id: Int, value: Boolean)
    fun endModifying()
}

inline fun<T : Preferences> T.modify(block: T.() -> Unit) {
    beginModifying()
    try {
        block()
    } finally {
        endModifying()
    }
}