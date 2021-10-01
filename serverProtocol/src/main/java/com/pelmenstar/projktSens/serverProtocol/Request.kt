package com.pelmenstar.projktSens.serverProtocol

import com.pelmenstar.projktSens.shared.AppendableToStringBuilder
import com.pelmenstar.projktSens.shared.equalsPattern
import com.pelmenstar.projktSens.shared.serialization.ValidationException
import com.pelmenstar.projktSens.shared.time.ShortDate
import com.pelmenstar.projktSens.shared.time.ShortDateInt
import com.pelmenstar.projktSens.shared.time.ShortDateRange

/**
 * Contains information to make request to server
 */
class Request(val command: Int, val argument: Argument? = null) : AppendableToStringBuilder() {
    constructor(command: Int, value: Int): this(command, Argument.Integer(value))

    sealed class Argument(val type: Int): AppendableToStringBuilder() {
        class Integer(val value: Int) : Argument(TYPE_INTEGER) {
            override fun equals(other: Any?): Boolean {
                return equalsPattern(other) { o ->
                    value == o.value
                }
            }

            override fun hashCode(): Int {
                return value
            }

            override fun append(sb: StringBuilder) {
                sb.append("{type=INTEGER, value=")
                sb.append(value)
                sb.append('}')
            }
        }

        class DateRange(
            @ShortDateInt val start: Int,
            @ShortDateInt val endInclusive: Int
        ) : Argument(TYPE_DATE_RANGE) {
            init {
                require(ShortDate.isValid(start)) { "start" }
                require(ShortDate.isValid(endInclusive)) { "endInclusive" }
            }

            override fun equals(other: Any?): Boolean {
                return equalsPattern(other) { o ->
                    start == o.start && endInclusive == o.endInclusive
                }
            }

            override fun hashCode(): Int {
                var result = start
                result = 31 * result + endInclusive

                return result
            }

            override fun append(sb: StringBuilder) {
                sb.append("{type=DATE_RANGE, value={start=")
                ShortDate.append(start, sb)
                sb.append(", endInclusive=")
                ShortDate.append(endInclusive, sb)
                sb.append("}}")
            }
        }

        companion object {
            const val TYPE_NULL = 0
            const val TYPE_INTEGER = 1
            const val TYPE_DATE_RANGE = 2

            fun typeToString(type: Int): String {
                return when(type) {
                    TYPE_NULL -> "TYPE_NULL"
                    TYPE_INTEGER -> "TYPE_INTEGER"
                    TYPE_DATE_RANGE -> "TYPE_DATE_RANGE"
                    else -> throw IllegalArgumentException("type")
                }
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        return equalsPattern(other) { o ->
            command == o.command && argument == o.argument
        }
    }

    override fun hashCode(): Int {
        var result = command
        if (argument != null) {
            result = 31 * result + argument.hashCode()
        }

        return result
    }

    override fun append(builder: StringBuilder) {
        builder.run {
            append("{command=")
            append(Commands.toString(command))
            append("; argument=")
            if(argument != null) {
                argument.append(this)
            } else {
                append("null")
            }
            append('}')
        }
    }
}