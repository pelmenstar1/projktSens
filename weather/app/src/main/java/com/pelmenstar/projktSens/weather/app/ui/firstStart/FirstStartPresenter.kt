package com.pelmenstar.projktSens.weather.app.ui.firstStart

import android.os.Bundle
import android.util.Log
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

    private val setCurrentScreenValid = FirstStartScreen.IncompleteState.OnValidChanged {
        view.setCurrentStateValid(it)
    }

    override fun attach(view: FirstStartContract.View) {
        super.attach(view)

        changeScreen(0)
    }

    override fun saveState(outState: Bundle) {
        outState.putInt(STATE_POSITION, position)
        outState.putBundle(STATE_TEMP_INTERNAL_STATE_BUNDLE, tempStateBundle)
    }

    override fun restoreState(state: Bundle) {
        val pos = state.getInt(STATE_POSITION, -1)
        if(pos != -1) {
            changeScreen(pos)

            val bundle = state.getBundle(STATE_TEMP_INTERNAL_STATE_BUNDLE)
            if (bundle != null) {
                tempStateBundle.putAll(bundle)

                for (i in 0 until pos) {
                    screens[i].loadStateFromBundleOrDefault(bundle)
                }
            }
        }
    }

    override fun previousScreen() {
        if(position == 0) {
            Log.e(TAG, "previousScreen() and screen is the first")
        } else {
            changeScreen(position - 1)
        }
    }

    override fun nextScreen() {
        if(position == screens.size - 1) {
            Log.e(TAG, "previousScreen() and screen is the last")
        } else {
            changeScreen(position + 1)
        }
    }

    private fun changeScreen(pos: Int) {
        if(position != -1) {
            val prevScreen = screens[position]
            prevScreen.saveStateToBundle(tempStateBundle)
        }

        position = pos
        val screen = screens[pos]

        val v = view
        val context = v.context
        screen.loadStateFromBundleOrDefault(tempStateBundle)
        val state = screen.state
        if(state is FirstStartScreen.IncompleteState) {
            state.onValidChanged = setCurrentScreenValid
        }

        v.setScreenTitle(context.resources.getString(screen.getTitleId()))
        v.setScreenView(screen.createView(context))

        v.setCurrentScreenFlags(pos == 0, pos == screens.size - 1)
    }

    private fun FirstStartScreen<*>.loadStateFromBundleOrDefault(bundle: Bundle) {
        val success = loadStateFromBundle(bundle)
        if(!success) {
            loadDefaultState()
        }
    }

    override fun onFinish() {
        prefs.beginModifying()
        for(screen in screens) {
            screen.saveStateToPrefs(prefs)
        }
        prefs.isFirstStart = false
        prefs.endModifying()
    }
    
    companion object {
        private const val TAG = "FirstStartActivity"

        private const val STATE_POSITION = "FirstStartActivity.state.position"
        private const val STATE_TEMP_INTERNAL_STATE_BUNDLE = "FirstStartActivity.state.tempInternalStateBundle"
    }
}