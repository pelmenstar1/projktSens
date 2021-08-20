package com.pelmenstar.projktSens.weather.app.ui.moon

import com.pelmenstar.projktSens.shared.android.mvp.DefaultContract
import com.pelmenstar.projktSens.shared.time.ShortDateInt

interface MoonCalendarContract {
    interface Presenter : DefaultContract.Presenter<View> {
        fun onDateSelected(@ShortDateInt date: Int)
    }

    interface View : DefaultContract.View {
        fun setMoonPhase(phase: Float)
    }
}