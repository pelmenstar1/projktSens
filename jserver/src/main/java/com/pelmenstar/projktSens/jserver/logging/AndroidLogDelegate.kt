package com.pelmenstar.projktSens.jserver.logging

import android.util.Log

/**
 * Implementation of [LogDelegate] which prints message using [Log]
 */
object AndroidLogDelegate : LogDelegate {
    private val ANDROID_LOG_MAP = intArrayOf(
        Log.DEBUG,
        Log.INFO,
        Log.ERROR
    )

    override fun print(level: Int, message: String) {
        Log.println(ANDROID_LOG_MAP[level], "SERVER-LOG", message)
    }
}