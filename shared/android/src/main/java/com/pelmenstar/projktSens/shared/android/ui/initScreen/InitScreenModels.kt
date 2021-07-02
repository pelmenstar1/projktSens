@file:Suppress("FunctionName", "NOTHING_TO_INLINE")

package com.pelmenstar.projktSens.shared.android.ui.initScreen

import com.pelmenstar.projktSens.shared.add
import com.pelmenstar.projktSens.shared.equalsPattern
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Contains appropriate information for task which initializes some component of application
 *
 * @param id can be any integer, but mustn't repeat in one [InitContext] (should be unique)
 *
 * @param timeout timeout, should be > 0
 *
 * @param isRequired `true`, if it is necessary for task to be executed, `false`, if not
 */
abstract class InitTask(
    val id: Int,
    timeout: Int = 10 * 1000,
    val isRequired: Boolean = false
) {
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
            id == o.id && timeout == o.timeout && isRequired == o.isRequired
        }
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + if (isRequired) 1 else 0
        result = 31 * result + timeout

        return result
    }

    override fun toString(): String {
        return "{id=$id, timeout=$timeout, isRequired=$isRequired}"
    }

    abstract suspend fun run(): Result
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
class InitContextBuilder(initialSize: Int = 1) {
    @JvmField
    var _tasks = arrayOfNulls<InitTask>(initialSize)

    @JvmField
    var _tasksActualLength = 0

    fun add(task: InitTask) {
        if(_tasks.size == _tasksActualLength) {
            _tasks = _tasks.add(task)
        } else {
            _tasks[_tasksActualLength++] = task
        }
    }
}

/**
 * [InitContext] builder, tasks can be added in [block]
 *
 * @param messageMapper message mapper of [InitContext]
 */
@Suppress("UNCHECKED_CAST")
inline fun InitContext(messageMapper: MessageMapper, initialSize: Int = 1, block: InitContextBuilder.() -> Unit): InitContext {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    val builder = InitContextBuilder(initialSize)
    builder.block()

    var tasks = builder._tasks
    if(tasks.size != builder._tasksActualLength) {
        val newTasks = arrayOfNulls<InitTask>(builder._tasksActualLength)
        System.arraycopy(tasks, 0, newTasks, 0, newTasks.size)

        tasks = newTasks
    }

    return InitContext(messageMapper, tasks as Array<out InitTask>)
}

