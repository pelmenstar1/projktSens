package com.pelmenstar.projktSens.shared.android.ui.settings

import android.content.Context
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.View
import androidx.annotation.StringRes
import com.pelmenstar.projktSens.shared.ReflectionUtils
import com.pelmenstar.projktSens.shared.android.Preferences
import com.pelmenstar.projktSens.shared.android.readNonNullString

/**
 * Describes the required information to create setting, save and load state of it.
 * **Should have public constructor with no arguments**
 */
abstract class Setting<TState : Any> {
    /**
     * Special type of state which allows the state to incomplete (invalid).
     * When [IncompleteState.isValid] is false, it cannot be saved to preferences.
     * Though, it can be saved and loaded back from bundle, as temporary state usually saved to bundle
     * and it may be incomplete.
     */
    open class IncompleteState {
        fun interface OnValidChanged {
            fun onChanged(newValue: Boolean)
        }

        /**
         * [OnValidChanged.onChanged] called when [isValid] is changed
         */
        var onValidChanged: OnValidChanged? = null

        /**
         * Determines whether state is valid
         */
        var isValid: Boolean = true
            set(value) {
                val oldValue = field
                field = value

                if(value != oldValue) {
                    onValidChanged?.onChanged(value)
                }
            }
    }

    private var _state: TState? = null

    /**
     * State of [Setting]
     *
     * @throws RuntimeException if state isn't loaded
     */
    var state: TState
        get() = _state ?: throw RuntimeException("State is not loaded")
        protected set(value) {
            _state = value
        }

    /**
     * Returns resource ID of string which describes the setting for user
     */
    @StringRes
    abstract fun getNameId(): Int

    /**
     * Creates some [View] in order to give user a possibility to change setting
     */
    abstract fun createView(context: Context): View

    /**
     * Loads state from given [Preferences]
     */
    abstract fun loadStateFromPrefs(prefs: Preferences)

    /**
     * Loads state from given [Bundle].
     * Note that if [TState] is derived from [IncompleteState],
     * it is allowed to bundle to contain incomplete (invalid) values
     * but values have to be loaded anyway
     */
    abstract fun loadStateFromBundle(bundle: Bundle): Boolean

    /**
     * Saves current state of setting to [Preferences]
     */
    abstract fun saveStateToPrefs(prefs: Preferences)

    /**
     * Saves current state of setting to [Bundle].
     * Note that if [TState] is derived from [IncompleteState],
     * state have to be saved even if values are invalid.
     */
    abstract fun saveStateToBundle(outState: Bundle)
}