@file:Suppress("FunctionName", "NOTHING_TO_INLINE")

package com.pelmenstar.projktSens.shared.android.ui.initScreen

import com.pelmenstar.projktSens.shared.add
import com.pelmenstar.projktSens.shared.equalsPattern
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

typealias InitTaskRunner = suspend () -> InitTask.Result

/**
 * Contains appropriate information for task which initializes some component of application
 *
 * @param id can be any integer, but mustn't repeat in one [InitContext] (should be unique)
 *
 * @param timeout timeout, should be > 0
 *
 * @param isRequired `true`, if it is necessary for task to be executed, `false`, if not
 * @param runner block of code to run
 */
class InitTask(val id: Int, timeout: Int = 10 * 1000, val isRequired: Boolean = false, val runner: InitTaskRunner) {
    /**
     * Timeout
     */
    val timeout: Int

    init {
        if(timeout <= 0) {
            throw IllegalArgumentException("timeout <= 0")
        }

        this.timeout = timeout
    }

    /**
     * Contains all possible result of executing task
     */
    sealed class Result {
        /**
         * Task completed successfully
         */
        object Ok: Result()

        /**
         * Task completed with error
         */
        object Error: Result()
    }

    override fun equals(other: Any?): Boolean {
        return equalsPattern(other) { o ->
            id == o.id && timeout == o.timeout && isRequired == o.isRequired && runner === o.runner
        }
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + if (isRequired) 1 else 0
        result = 31 * result + (timeout xor (timeout shl 32))
        result = 31 * result + runner.hashCode()

        return result
    }

    override fun toString(): String {
        return "{id=$id, timeout=$timeout, isRequired=$isRequired}"
    }
}

/**
 * Represents a context of initialization component
 *
 */
class InitContext(val messageMapper: MessageMapper, val tasks: Array<out InitTask>) {
    override fun equals(other: Any?): Boolean {
        return equalsPattern(other) { o ->
            o.messageMapper == messageMapper && tasks contentEquals o.tasks
        }
    }

    override fun hashCode(): Int {
        var result = messageMapper.hashCode()
        result = result * 31 + tasks.contentHashCode()

        return result
    }
}

/**
 * Builder for [InitContext]. Don't use this directly
 */
class InitContextBuilder {
    @JvmField
    var _tasks = emptyArray<InitTask>()

    inline fun InitTask(
        id: Int,
        timeout: Int = 10 * 1000,
        required: Boolean = false,
        noinline runner: InitTaskRunner
    ) {
        val task = com.pelmenstar.projktSens.shared.android.ui.initScreen.InitTask(id, timeout, required, runner)

        _tasks = _tasks.add(task)
    }
}

/**
 * [InitContext] builder, tasks can be added in [block]
 *
 * @param messageMapper message mapper of [InitContext]
 */
inline fun InitContext(messageMapper: MessageMapper, block: InitContextBuilder.() -> Unit): InitContext {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    val builder = InitContextBuilder()
    builder.block()

    return InitContext(messageMapper, builder._tasks)
}

