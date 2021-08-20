package com.pelmenstar.projktSens.jserver.logging

import com.pelmenstar.projktSens.shared.StringBuilderWriter
import java.io.PrintWriter

/**
 * Delegates and generalizes functions of [Logger]
 */
interface LogDelegate {
    /**
     * Prints [message] with specified level of logging to some output
     */
    fun print(level: Int, message: String)
}

/**
 * Responsible for configuring [Logger]
 */
class LoggerConfig(val delegate: LogDelegate, val minLogLevel: Int)

/**
 * Responsible for logging messages
 */
class Logger(val area: String, val config: LoggerConfig) {
    /**
     * Prints [message] with [LogLevel.INFO] priority
     */
    infix fun info(message: String) {
        log(LogLevel.INFO, message)
    }

    /**
     * Prints message with [LogLevel.INFO] priority. Message is created using [messageBuilder]
     */
    inline infix fun info(messageBuilder: StringBuilder.() -> Unit) {
        log(LogLevel.INFO, messageBuilder)
    }

    /**
     * Prints [message] with [LogLevel.DEBUG] priority
     */
    infix fun debug(message: String) {
        log(LogLevel.DEBUG, message)
    }

    /**
     * Prints message with [LogLevel.DEBUG] priority. Message is created using [messageBuilder]
     */
    inline infix fun debug(messageBuilder: StringBuilder.() -> Unit) {
        log(LogLevel.DEBUG, messageBuilder)
    }

    /**
     * Prints message with [LogLevel.ERROR] priority
     */
    infix fun error(message: String) {
        log(LogLevel.ERROR, message)
    }

    /**
     * Prints stacktrace of [Throwable] with [LogLevel.ERROR] priority
     */
    infix fun error(th: Throwable) {
        log(LogLevel.ERROR) {
            val sbWriter = StringBuilderWriter(this)
            th.printStackTrace(PrintWriter(sbWriter))
        }
    }

    /**
     * Prints [message] and stacktrace of [Throwable] with [LogLevel.ERROR] priority.
     */
    fun error(message: String, th: Throwable) {
        log(LogLevel.ERROR) {
            appendLine(message)
            val sbWriter = StringBuilderWriter(this)
            th.printStackTrace(PrintWriter(sbWriter))
        }
    }

    /**
     * Don't call it directly. Exposed to public because of some reasons
     */
    fun StringBuilder.appendFinalMessageHeader(level: Int) {
        append(LogLevel.getPrefix(level))
        append('|')
        append(' ')
        append(area)
        append(' ')
        append('>')
        append(' ')
    }

    private fun log(level: Int, message: String) {
        log(level) { append(message) }
    }

    /**
     * Prints message with specified [LogLevel]. Message is created through [messageBuilder]
     */
    inline fun log(level: Int, messageBuilder: StringBuilder.() -> Unit) {
        if (level >= config.minLogLevel) {
            val message = buildString(6 + area.length) {
                appendFinalMessageHeader(level)
                messageBuilder()
            }

            config.delegate.print(level, message)
        }
    }
}