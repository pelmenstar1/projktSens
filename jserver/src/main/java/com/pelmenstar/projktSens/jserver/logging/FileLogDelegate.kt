package com.pelmenstar.projktSens.jserver.logging

import android.content.Context
import com.pelmenstar.projktSens.jserver.LogDelegate
import com.pelmenstar.projktSens.jserver.LogLevel

class FileLogDelegate(
    private val context: Context,
    private val fileName: String
): LogDelegate {
    override fun print(level: LogLevel, message: String) {
        val msgEncoded = message.toByteArray(Charsets.UTF_8)

        context.openFileOutput(fileName, Context.MODE_APPEND).use { out ->
            out.write(msgEncoded)
            out.write(10) // new line
        }
    }
}