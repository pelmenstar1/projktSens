package com.pelmenstar.projktSens.serverProtocol.repo

import com.pelmenstar.projktSens.shared.AppendableToStringBuilder
import com.pelmenstar.projktSens.shared.equalsPattern

/**
 * Contains information to make request to repo-server
 */
class RepoRequest: AppendableToStringBuilder {
    /**
     * Command of request
     */
    val command: Int

    /**
     * Optional binary arguments of request
     */
    val argument: Any?

    constructor(command: Int) {
        this.command = command
        this.argument = null
    }

    constructor(command: Int, arg: Any?) {
        this.command = command
        this.argument = arg
    }

    override fun equals(other: Any?): Boolean {
        return equalsPattern(other) { o ->
            command == o.command && argument == o.argument
        }
    }

    override fun hashCode(): Int {
        var result = command
        result = 31 * result + argument.hashCode()

        return result
    }

    override fun append(builder: StringBuilder) {
        builder.run {
            append("{command=")
            append(RepoCommands.toString(command))
            append("; argument=")
            append(argument)
            append('}')
        }
    }
}