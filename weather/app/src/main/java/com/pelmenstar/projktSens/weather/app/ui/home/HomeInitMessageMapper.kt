package com.pelmenstar.projktSens.weather.app.ui.home

import android.content.res.Resources
import com.pelmenstar.projktSens.shared.android.ui.initScreen.MessageMapper
import com.pelmenstar.projktSens.weather.app.R

class HomeInitMessageMapper(private val resources: Resources) : MessageMapper {
    override fun getTaskName(taskId: Int): CharSequence {
        return when (taskId) {
            HomePresenter.TASK_GEOLOCATION -> resources.getText(R.string.initLocation_stageName)
            HomePresenter.TASK_CALENDAR -> resources.getText(R.string.initCalendar_stageName)
            else -> throw IllegalArgumentException("taskId")
        }
    }

    override fun getErrorMessage(taskId: Int): CharSequence {
        return when (taskId) {
            HomePresenter.TASK_GEOLOCATION -> resources.getText(R.string.initLocation_errorMsg)
            HomePresenter.TASK_CALENDAR -> resources.getText(R.string.initCalendar_errorMsg)
            else -> throw IllegalArgumentException("taskId")
        }
    }
}