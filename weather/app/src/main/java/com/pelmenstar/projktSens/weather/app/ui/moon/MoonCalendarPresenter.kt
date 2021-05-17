package com.pelmenstar.projktSens.weather.app.ui.moon

import android.os.Bundle
import com.pelmenstar.projktSens.shared.android.mvp.BasePresenter
import com.pelmenstar.projktSens.shared.time.ShortDate
import com.pelmenstar.projktSens.shared.time.ShortDateInt
import com.pelmenstar.projktSens.weather.models.astro.MoonInfoProvider

class MoonCalendarPresenter(
    private val moonInfoProvider: MoonInfoProvider
): MoonCalendarContract.Presenter, BasePresenter<MoonCalendarContract.View>() {
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
        val date = state.getInt(STATE_SELECTED_DATE, ShortDate.NONE)

        if(date != ShortDate.NONE) {
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