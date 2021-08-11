package com.pelmenstar.projktSens.weather.app.ui.firstStart

import android.view.View
import com.pelmenstar.projktSens.shared.android.mvp.DefaultContract

interface FirstStartContract {
    interface Presenter: DefaultContract.Presenter<View> {
        fun previousScreen()
        fun nextScreen()

        fun onFinish()
    }

    interface View: DefaultContract.View {
        fun setScreenTitle(title: String)
        fun setScreenView(view: android.view.View)

        fun setCurrentScreenFlags(first: Boolean, last: Boolean)
        fun setCurrentStateValid(value: Boolean)
    }
}