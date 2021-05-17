package com.pelmenstar.projktSens.serverProtocol.repo

import com.pelmenstar.projktSens.shared.*

/**
 * Contains information to make request to repo-server
 */
class RepoRequest: AppendableToStringBuilder {
    val command: Int
    val args: ByteArray?

    constructor(command: Int) {
        this.command = command
        this.args = null
    }

    constructor(command: Int, args: ByteArray?) {
        this.command = command
        this.args = args
    }

    override fun equals(other: Any?): Boolean {
        return equalsPattern(other) { o ->
            command == o.command && args contentEquals o.args
        }
    }

    override fun hashCode(): Int {
        var result = command
        result = 31 * result + args.contentHashCode()

        return result
    }

    override fun append(builder: StringBuilder) {
        builder.run {
            append("{command=")
            append(RepoCommands.toString(command))
            append("; args=")
            appendArray(args)
            append('}')
        }
    }
}