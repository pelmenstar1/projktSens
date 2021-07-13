package com.pelmenstar.projktSens.shared.android

import android.content.Context

interface Preferences {
    fun initialize(context: Context)

    operator fun get(id: Int): Any
    fun getInt(id: Int): Int
    fun getBoolean(id: Int): Boolean

    operator fun set(id: Int, value: Any)
    fun setInt(id: Int, value: Int)
    fun setBoolean(id: Int, value: Boolean)
}