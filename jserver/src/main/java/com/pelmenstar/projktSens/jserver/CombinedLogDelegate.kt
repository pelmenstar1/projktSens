package com.pelmenstar.projktSens.jserver

class CombinedLogDelegate(
    private val delegates: Array<LogDelegate>
): LogDelegate {
    override fun print(level: LogLevel, message: String) {
        for(delegate in delegates) {
            delegate.print(level, message)
        }
    }

}