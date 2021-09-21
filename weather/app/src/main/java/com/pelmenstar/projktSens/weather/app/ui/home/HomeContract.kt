package com.pelmenstar.projktSens.weather.app.ui.home

import com.pelmenstar.projktSens.shared.android.mvp.DefaultContract
import com.pelmenstar.projktSens.shared.time.ShortDateInt
import com.pelmenstar.projktSens.weather.app.ui.home.weatherView.RequestLocationSubcomponent
import com.pelmenstar.projktSens.weather.app.ui.home.weatherView.RetryGetLocationSubcomponent
import com.pelmenstar.projktSens.weather.models.WeatherInfo

interface HomeContract {
    interface Presenter : DefaultContract.Presenter<View> {
        fun getLoadMinMaxCalendarHandler(): LazyLoadingCalendarView.LoadMinMaxHandler
        fun getOnRetryGetLocationListener(): RetryGetLocationSubcomponent.OnRetryGetLocationListener
        fun getRequestLocationPermissionHandler(): RequestLocationSubcomponent.RequestLocationPermissionHandler

        fun startTodayReportView()
        fun startYesterdayReportView()

        fun startThisWeekReportView()
        fun startPreviousWeekReportView()

        fun startThisMonthReportView()
        fun startPreviousMonthReportView()

        fun startDayReportView(@ShortDateInt date: Int)

        fun connectToWeatherChannel()
        fun disconnectFromWeatherChannel()

        fun refreshLocationPermissionGrantedState()
    }

    interface View : DefaultContract.View {
        fun setCurrentTime(time: Int)
        fun setSunriseSunset(sunrise: Int, sunset: Int)
        fun setWeather(value: WeatherInfo)
        fun setLocationLoaded(value: Boolean)
        fun setCanLoadLocation(value: Boolean)

        fun onServerAvailable()
        fun onServerUnavailable()
    }
}