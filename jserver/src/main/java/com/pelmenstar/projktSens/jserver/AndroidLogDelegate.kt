package com.pelmenstar.projktSens.jserver

import android.util.Log

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