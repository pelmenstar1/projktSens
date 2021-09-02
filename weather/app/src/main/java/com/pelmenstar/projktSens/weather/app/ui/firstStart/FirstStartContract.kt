package com.pelmenstar.projktSens.weather.app.ui.firstStart

import com.pelmenstar.projktSens.shared.android.mvp.DefaultContract

interface FirstStartContract {
    interface Presenter : DefaultContract.Presenter<View> {
        val screenViews: Array<out android.view.View>

        fun getScreenTitleAt(index: Int): String
        fun onScreenChangedByUser(newPos: Int)
        fun previousScreen()
        fun nextScreen()

        fun afterRestoredFromSavedState()
        fun onFinish()
    }

    interface View : DefaultContract.View {
        fun setPosition(position: Int, screen: FirstStartScreen<*>, withAnimation: Boolean)

        fun setCurrentScreenFlags(first: Boolean, last: Boolean)
        fun setCurrentStateValid(value: Boolean)
    }
}