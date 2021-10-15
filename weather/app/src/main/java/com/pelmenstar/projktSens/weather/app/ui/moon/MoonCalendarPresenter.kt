package com.pelmenstar.projktSens.weather.app.ui.moon

import android.os.Bundle
import com.pelmenstar.projktSens.shared.android.mvp.BasePresenter
import com.pelmenstar.projktSens.shared.time.ShortDate
import com.pelmenstar.projktSens.shared.time.ShortDateInt
import com.pelmenstar.projktSens.weather.app.astro.MoonInfoProvider

class MoonCalendarPresenter(
    private val moonInfoProvider: MoonInfoProvider
) : MoonCalendarContract.Presenter, BasePresenter<MoonCalendarContract.View>() {
    @ShortDateInt
    private var selectedDate: Int = ShortDate.NONE

    override fun attach(view: MoonCalendarContract.View) {
        super.attach(view)

        onDateSelected(ShortDate.now())
    }

    override fun saveState(outState: Bundle) {
        outState.putInt(STATE_SELECTED_DATE, selectedDate)
    }

    override fun restoreState(state: Bundle) {
        val date = state.get(STATE_SELECTED_DATE) as Int?

        if (date != null) {
            onDateSelected(date)
        }
    }

    override fun onDateSelected(@ShortDateInt date: Int) {
        selectedDate = date

        val phase = moonInfoProvider.getMoonPhase(date)

        view.setMoonPhase(phase)
    }

    companion object {
        private const val STATE_SELECTED_DATE = "state:MoonCalendarPresenter:selectedDate"
    }
}