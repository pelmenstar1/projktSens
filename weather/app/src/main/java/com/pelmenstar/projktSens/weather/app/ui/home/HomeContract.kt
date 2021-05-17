package com.pelmenstar.projktSens.weather.app.ui.home

import com.pelmenstar.projktSens.shared.android.mvp.DefaultContract
import com.pelmenstar.projktSens.shared.android.ui.initScreen.InitContext
import com.pelmenstar.projktSens.shared.time.ShortDateInt
import com.pelmenstar.projktSens.shared.time.TimeRange
import com.pelmenstar.projktSens.weather.models.WeatherInfo

interface HomeContract {
    interface Presenter : DefaultContract.Presenter<View> {
        val initContext: InitContext

        fun onInitEnded()

        fun startTodayReportView()
        fun startYesterdayReportView()

        fun startThisWeekReportView()
        fun startPreviousWeekReportView()

        fun startThisMonthReportView()
        fun startPreviousMonthReportView()

        fun startDayReportView(@ShortDateInt date: Int)

        fun connectToWeatherChannel()
        fun disconnectFromWeatherChannel()
    }

    interface View : DefaultContract.View {
        fun setCurrentTime(time: Int)
        fun setSunriseSunset(sunrise: Int, sunset: Int)
        fun setMoonPhase(phase: Float)
        fun setWeather(value: WeatherInfo)

        fun setCalendarMinDate(millis: Long)
        fun setCalendarMaxDate(millis: Long)

        fun onServerAvailable()
        fun onServerUnavailable()
    }
}