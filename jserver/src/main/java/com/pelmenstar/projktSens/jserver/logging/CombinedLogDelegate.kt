package com.pelmenstar.projktSens.jserver.logging

class CombinedLogDelegate(private val delegates: Array<out LogDelegate>) : LogDelegate {
    override fun print(level: Int, message: String) {
        delegates.forEach { it.print(level, message) }
    }
}