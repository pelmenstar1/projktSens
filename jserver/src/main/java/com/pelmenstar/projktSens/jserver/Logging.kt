package com.pelmenstar.projktSens.jserver

import kotlin.text.StringBuilder

enum class LogLevel(@JvmField val prefix: Char, @JvmField val priority: Int) {
    DEBUG('D', 0),
    INFO('I', 1),
    ERROR('E',2),
}

interface LogDelegate {
    fun print(level: LogLevel, message: String)
}

data class LoggerConfig(
    val delegate: LogDelegate,
    val minLogLevel: LogLevel
)

class Logger(val area: String, val config: LoggerConfig) {
    private val delegate = config.delegate
    private val minLogLevelPriority = config.minLogLevel.priority

    fun info(message: String) {
        logInternal(LogLevel.INFO, message)
    }

    inline fun info(messageBuilder: StringBuilder.() -> Unit) {
        print(LogLevel.INFO, messageBuilder)
    }

    fun debug(message: String) {
        logInternal(LogLevel.DEBUG, message)
    }

    inline fun debug(messageBuilder: StringBuilder.() -> Unit) {
        print(LogLevel.DEBUG, messageBuilder)
    }

    fun error(message: String) {
        logInternal(LogLevel.ERROR, message)
    }

    fun error(th: Throwable) {
        error(th.stackTraceToString())
    }

    fun error(message: String, th: Throwable) {
        print(LogLevel.ERROR) {
            appendLine(message)
            append(th.stackTraceToString())
        }
    }

    private fun formatToFinalMessage(level: LogLevel, inMessage: String): String {
        return buildString(6 + area.length + inMessage.length) {
            appendFinalMessageHeader(level)
            append(inMessage)
        }
    }

    inline fun formatToFinalMessage(
        level: LogLevel,
        builderMutator: StringBuilder.() -> Unit
    ): String {
        return buildString(6 + area.length) {
            appendFinalMessageHeader(level)
            builderMutator()
        }
    }

    fun StringBuilder.appendFinalMessageHeader(level: LogLevel) {
        append(level.prefix)
        append('|')
        append(' ')
        append(area)
        append(' ')
        append('>')
        append(' ')
    }

    private fun logInternal(level: LogLevel, message: String) {
        if(level.priority >= minLogLevelPriority) {
            delegate.print(level, formatToFinalMessage(level, message))
        }
    }

    inline fun print(level: LogLevel, messageBuilder: StringBuilder.() -> Unit) {
        if(level.priority >= config.minLogLevel.priority) {
            config.delegate.print(level, formatToFinalMessage(level, messageBuilder))
        }
    }
}