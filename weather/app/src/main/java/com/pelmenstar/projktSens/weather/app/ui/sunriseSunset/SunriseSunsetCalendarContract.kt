package com.pelmenstar.projktSens.weather.app.ui.sunriseSunset

import com.pelmenstar.projktSens.shared.android.mvp.DefaultContract
import com.pelmenstar.projktSens.shared.time.TimeInt

interface SunriseSunsetCalendarContract {
    interface Presenter : DefaultContract.Presenter<View> {
        fun onDaySelected(dayOfYear: Int)
        fun onLocationPresent()
    }

    interface View : DefaultContract.View {
        fun setSunriseTime(@TimeInt sunrise: Int)
        fun setSunsetTime(@TimeInt sunset: Int)
        fun setDayLength(@TimeInt time: Int)
    }
}