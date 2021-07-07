package com.pelmenstar.projktSens.jserver.logging

import com.pelmenstar.projktSens.jserver.LogDelegate
import com.pelmenstar.projktSens.jserver.LogLevel

class CombinedLogDelegate(
    private val delegates: Array<LogDelegate>
): LogDelegate {
    override fun print(level: LogLevel, message: String) {
        for(delegate in delegates) {
            delegate.print(level, message)
        }
    }

}