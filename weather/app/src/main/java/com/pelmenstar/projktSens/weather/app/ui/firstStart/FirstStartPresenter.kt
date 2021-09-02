package com.pelmenstar.projktSens.weather.app.ui.firstStart

import android.os.Bundle
import android.view.View
import com.pelmenstar.projktSens.shared.android.modify
import com.pelmenstar.projktSens.shared.android.mvp.BasePresenter
import com.pelmenstar.projktSens.weather.app.AppPreferences

class FirstStartPresenter(
    private val prefs: AppPreferences
) : BasePresenter<FirstStartContract.View>(), FirstStartContract.Presenter {
    override val screenViews: Array<out View> by lazy {
        val c = context

        Array(screens.size) { i ->
            screens[i].createView(c)
        }
    }

    private val screens: Array<out FirstStartScreen<*>> = arrayOf(
        ChooseAddressAndPortScreen(),
        ChooseServerContractScreen()
    )
    private val tempStateBundle = Bundle()

    private var position: Int = -1
    private var savedPosition: Int = -1

    override fun saveState(outState: Bundle) {
        outState.putInt(STATE_POSITION, position)

        screens[position].saveStateToBundle(tempStateBundle)

        outState.putBundle(STATE_TEMP_INTERNAL_STATE_BUNDLE, tempStateBundle)
    }

    override fun afterRestoredFromSavedState() {
        val setCurrentScreenValid = FirstStartScreen.IncompleteState.OnValidChanged {
            view.setCurrentStateValid(it)
        }

        for (screen in screens) {
            screen.loadStateFromBundleOrDefault(tempStateBundle)
            val state = screen.state

            if (state is FirstStartScreen.IncompleteState) {
                state.onValidChanged = setCurrentScreenValid
            }
        }

        changeScreen(if (savedPosition == -1) 0 else savedPosition, withAnimation = false)
    }

    override fun restoreState(state: Bundle) {
        val pos = state.get(STATE_POSITION) as Int?
        if (pos != null) {
            savedPosition = pos

            val bundle = state.getBundle(STATE_TEMP_INTERNAL_STATE_BUNDLE)
            if (bundle != null) {
                tempStateBundle.putAll(bundle)
            }
        }
    }

    override fun getScreenTitleAt(index: Int): String {
        return context.resources.getString(screens[index].getTitleId())
    }

    override fun previousScreen() {
        if (position > 0) {
            changeScreen(position - 1)
        }
    }

    override fun nextScreen() {
        if (position < screenViews.size - 1) {
            changeScreen(position + 1)
        }
    }

    override fun onScreenChangedByUser(newPos: Int) {
        val oldPosition = position
        if (oldPosition != -1) {
            val prevScreen = screens[oldPosition]
            prevScreen.saveStateToBundle(tempStateBundle)
        }

        position = newPos
        view.setCurrentScreenFlags(newPos == 0, newPos == screenViews.size - 1)
    }

    private fun changeScreen(pos: Int, withAnimation: Boolean = true) {
        val oldPosition = position
        if (oldPosition != -1) {
            val prevScreen = screens[oldPosition]
            prevScreen.saveStateToBundle(tempStateBundle)
        }

        position = pos

        val v = view

        v.setPosition(pos, screens[pos], withAnimation)
        v.setCurrentScreenFlags(pos == 0, pos == screenViews.size - 1)
    }

    private fun FirstStartScreen<*>.loadStateFromBundleOrDefault(bundle: Bundle) {
        val success = loadStateFromBundle(bundle)
        if (!success) {
            loadDefaultState()
        }
    }

    override fun onFinish() {
        prefs.modify {
            screens.forEach { screen ->
                screen.saveStateToPrefs(this)
            }
            prefs.isFirstStart = false
        }
    }

    companion object {
        private const val STATE_POSITION = "FirstStartActivity.state.position"
        private const val STATE_TEMP_INTERNAL_STATE_BUNDLE =
            "FirstStartActivity.state.tempInternalStateBundle"
    }
}