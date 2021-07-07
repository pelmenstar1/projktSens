package com.pelmenstar.projktSens.jserver.logging

import android.util.Log
import com.pelmenstar.projktSens.jserver.LogDelegate
import com.pelmenstar.projktSens.jserver.LogLevel

/**
 * Implementation of [LogDelegate] which prints message using [Log]
 */
object AndroidLogDelegate: LogDelegate {
    override fun print(level: LogLevel, message: String) {
        val aPriority: Int = when(level) {
            LogLevel.DEBUG -> Log.DEBUG
            LogLevel.INFO -> Log.INFO
            LogLevel.ERROR -> Log.ERROR
        }

        Log.println(aPriority, "SERVER-LOG", message)
    }
}