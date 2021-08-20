package com.pelmenstar.projktSens.serverProtocol

import com.pelmenstar.projktSens.shared.AppendableToStringBuilder
import com.pelmenstar.projktSens.shared.equalsPattern

/**
 * Contains information to make request to server
 */
class Request(val command: Int, val argument: Any? = null) : AppendableToStringBuilder() {
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
            append(argument)
            append('}')
        }
    }
}