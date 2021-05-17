package com.pelmenstar.projktSens.shared.android.ui.initScreen

/**
 * Gives capability to convert task id to different user messages, like task name or error message
 */
interface MessageMapper {
    /**
     * Returns name of task with specified task id
     */
    fun getTaskName(taskId: Int): CharSequence

    /**
     * Returns readable error message of task with specified task id
     */
    fun getErrorMessage(taskId: Int): CharSequence
}