package com.pelmenstar.projktSens.weather.app.ui.firstStart

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import com.pelmenstar.projktSens.shared.android.Preferences

abstract class FirstStartScreen<TState> {
    abstract class IncompleteState {
        fun interface OnValidChanged {
            fun onChanged(newValue: Boolean)
        }

        var onValidChanged: OnValidChanged? = null

        var isValid: Boolean = true
            set(value) {
                val oldValue = field
                field = value

                if (value != oldValue) {
                    onValidChanged?.onChanged(value)
                }
            }
    }

    private var _state: TState? = null

    var state: TState
        get() = _state ?: throw RuntimeException("State is not loaded")
        protected set(value) {
            _state = value
        }

    @StringRes
    abstract fun getTitleId(): Int
    abstract fun createView(context: Context): View

    abstract fun loadDefaultState()
    abstract fun loadStateFromBundle(bundle: Bundle): Boolean

    abstract fun saveStateToPrefs(prefs: Preferences)
    abstract fun saveStateToBundle(outState: Bundle)
}