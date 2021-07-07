package com.pelmenstar.projktSens.jserver

/**
 * Represents various level of logging. When message is logger, it's possible to specify its priority using
 * these levels ([LogLevel.DEBUG] has the lowest priority, [LogLevel.ERROR] has the highest one)
 */
enum class LogLevel(@JvmField val prefix: Char, @JvmField val priority: Int) {
    /**
     * When some debug information needs to be logged
     */
    DEBUG('D', 0),

    /**
     * When more important, than debug, information needs to be logged
     */
    INFO('I', 1),

    /**
     * When error happened
     */
    ERROR('E',2),
}

/**
 * Delegates and generalizes functions of [Logger]
 */
interface LogDelegate {
    /**
     * Prints [message] with specified level of logging to some output
     */
    fun print(level: LogLevel, message: String)
}

/**
 * Responsible for configuring [Logger]
 */
data class LoggerConfig(
    val delegate: LogDelegate,
    val minLogLevel: LogLevel
)

/**
 * Responsible for logging messages
 */
class Logger(val area: String, val config: LoggerConfig) {
    private val delegate = config.delegate
    private val minLogLevelPriority = config.minLogLevel.priority

    /**
     * Prints [message] with [LogLevel.INFO] priority
     */
    fun info(message: String) {
        logInternal(LogLevel.INFO, message)
    }

    /**
     * Prints message with [LogLevel.INFO] priority. Message is created using [messageBuilder]
     */
    inline fun info(messageBuilder: StringBuilder.() -> Unit) {
        print(LogLevel.INFO, messageBuilder)
    }

    /**
     * Prints [message] with [LogLevel.DEBUG] priority
     */
    fun debug(message: String) {
        logInternal(LogLevel.DEBUG, message)
    }

    /**
     * Prints message with [LogLevel.DEBUG] priority. Message is created using [messageBuilder]
     */
    inline fun debug(messageBuilder: StringBuilder.() -> Unit) {
        print(LogLevel.DEBUG, messageBuilder)
    }

    /**
     * Prints message with [LogLevel.ERROR] priority
     */
    fun error(message: String) {
        logInternal(LogLevel.ERROR, message)
    }

    /**
     * Prints stacktrace of [Throwable] with [LogLevel.ERROR] priority
     */
    fun error(th: Throwable) {
        error(th.stackTraceToString())
    }

    /**
     * Prints [message] and stacktrace of [Throwable] with [LogLevel.ERROR] priority.
     */
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
    /**
     * Don't call it directly. Exposed to public because of some reasons
     */
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

    /**
     * Prints message with specified [LogLevel]. Message is created through [messageBuilder]
     */
    inline fun print(level: LogLevel, messageBuilder: StringBuilder.() -> Unit) {
        if(level.priority >= config.minLogLevel.priority) {
            val message = buildString(6 + area.length) {
                appendFinalMessageHeader(level)
                messageBuilder()
            }

            config.delegate.print(level, message)
        }
    }
}