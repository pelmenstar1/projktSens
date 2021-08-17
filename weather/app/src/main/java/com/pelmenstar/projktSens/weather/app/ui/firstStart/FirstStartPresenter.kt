package com.pelmenstar.projktSens.weather.app.ui.firstStart

import android.os.Bundle
import android.view.View
import com.pelmenstar.projktSens.shared.android.mvp.BasePresenter
import com.pelmenstar.projktSens.weather.app.AppPreferences

class FirstStartPresenter(
    private val prefs: AppPreferences
) : BasePresenter<FirstStartContract.View>(), FirstStartContract.Presenter {
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

        changeScreen(if (savedPosition == -1) 0 else savedPosition)
    }

    override fun inflateAllScreens(): Array<out View> {
        val context = context
        return Array(screens.size) { i ->
            screens[i].createView(context)
        }
    }

    override fun getScreenTitles(): Array<out String> {
        val res = context.resources

        return Array(screens.size) { i ->
            val strId = screens[i].getTitleId()

            res.getString(strId)
        }
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

    override fun previousScreen() {
        if (position > 0) {
            changeScreen(position - 1)
        }
    }

    override fun nextScreen() {
        if (position < screens.size - 1) {
            changeScreen(position + 1)
        }
    }

    private fun changeScreen(pos: Int) {
        var oldPosition = position
        if (oldPosition != -1) {
            val prevScreen = screens[oldPosition]
            prevScreen.saveStateToBundle(tempStateBundle)
        } else {
            oldPosition = pos
        }

        position = pos

        val v = view

        v.setPosition(oldPosition, pos)

        v.setCurrentScreenFlags(pos == 0, pos == screens.size - 1)
    }

    private fun FirstStartScreen<*>.loadStateFromBundleOrDefault(bundle: Bundle) {
        val success = loadStateFromBundle(bundle)
        if (!success) {
            loadDefaultState()
        }
    }

    override fun onFinish() {
        prefs.beginModifying()
        for (screen in screens) {
            screen.saveStateToPrefs(prefs)
        }
        prefs.isFirstStart = false
        prefs.endModifying()
    }

    companion object {
        private const val STATE_POSITION = "FirstStartActivity.state.position"
        private const val STATE_TEMP_INTERNAL_STATE_BUNDLE =
            "FirstStartActivity.state.tempInternalStateBundle"
    }
}