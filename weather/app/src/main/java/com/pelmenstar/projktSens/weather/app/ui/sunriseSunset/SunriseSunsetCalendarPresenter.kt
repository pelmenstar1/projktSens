package com.pelmenstar.projktSens.weather.app.ui.sunriseSunset

import android.os.Bundle
import com.pelmenstar.projktSens.shared.android.mvp.BasePresenter
import com.pelmenstar.projktSens.shared.geo.Geolocation
import com.pelmenstar.projktSens.shared.time.ShortDate
import com.pelmenstar.projktSens.weather.app.GeolocationCache
import com.pelmenstar.projktSens.weather.models.astro.SunInfoProvider

class SunriseSunsetCalendarPresenter(
    private val sunInfoProvider: SunInfoProvider
): BasePresenter<SunriseSunsetCalendarContract.View>(),
    SunriseSunsetCalendarContract.Presenter {
    private var selectedDayOfYear: Int = -1
    private var location: Geolocation? = null

    override fun attach(view: SunriseSunsetCalendarContract.View) {
        super.attach(view)

        location = GeolocationCache.get()
        if(location == null) {
            throw IllegalStateException("GeolocationCache.get() is null")
        }

        onDaySelected(ShortDate.getDayOfYear(ShortDate.now()))
    }

    override fun saveState(outState: Bundle) {
        super.saveState(outState)

        outState.putInt(STATE_SELECTED_DAY, selectedDayOfYear)
    }

    override fun restoreState(state: Bundle) {
        val dayOfYear = state.getInt(STATE_SELECTED_DAY, -1)

        if(dayOfYear != -1) {
            onDaySelected(dayOfYear)
        }
    }

    override fun onDaySelected(dayOfYear: Int) {
        selectedDayOfYear = dayOfYear

        val location = location

        if(location != null) {
            val sunrise = sunInfoProvider.getSunriseTime(dayOfYear, location)
            val sunset = sunInfoProvider.getSunsetTime(dayOfYear, location)
            val dayLength = sunset - sunrise

            view.run {
                setSunriseTime(sunrise)
                setSunsetTime(sunset)
                setDayLength(dayLength)
            }
        }
    }

    companion object {
        private const val STATE_SELECTED_DAY = "state:SunriseSunsetCalendar:selected_day"
    }
}