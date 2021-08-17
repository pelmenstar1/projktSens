package com.pelmenstar.projktSens.weather.app.ui.firstStart

import com.pelmenstar.projktSens.shared.android.mvp.DefaultContract

interface FirstStartContract {
    interface Presenter: DefaultContract.Presenter<View> {
        fun previousScreen()
        fun nextScreen()

        fun afterRestoredFromSavedState()
        fun inflateAllScreens(): Array<out android.view.View>

        fun onFinish()
    }

    interface View: DefaultContract.View {
        fun setScreenTitle(title: String)
        fun setPosition(oldPosition: Int, newPosition: Int)

        fun setCurrentScreenFlags(first: Boolean, last: Boolean)
        fun setCurrentStateValid(value: Boolean)
    }
}