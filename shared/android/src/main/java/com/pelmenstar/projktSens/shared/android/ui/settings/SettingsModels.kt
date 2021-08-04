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

private typealias SettingClass = Class<out Setting<*>>

/**
 * Stores information needed for handling settings. This class implements [Parcelable]
 */
class SettingsContext : Parcelable {
    private var _settings: Array<out Setting<*>>? = null
    val settings: Array<out Setting<*>>
        get() {
            var s = _settings
            if(s != null) {
                return s
            }

            val sc = _settingClasses ?: throw RuntimeException("Something goes wrong. _settingClasses == null")
            s = Array(sc.size) { i ->
                ReflectionUtils.createFromEmptyConstructor(sc[i])
            }
            _settings = s
            return s
        }

    private var _settingClasses: Array<out SettingClass>? = null
    val settingsClasses: Array<out SettingClass>
        get() {
            var sc = _settingClasses
            if(sc != null) {
                return sc
            }

            val s = _settings ?: throw RuntimeException("Something goes wrong. _settings == null")
            sc = Array(s.size) { i -> s[i].javaClass }
            _settingClasses = sc

            return sc
        }

    constructor(settings: Array<out Setting<*>>) {
        _settings = settings
    }

    constructor(classes: Array<out SettingClass>) {
        _settingClasses = classes
    }

    constructor(parcel: Parcel) {
        val classLoader = javaClass.classLoader ?: ClassLoader.getSystemClassLoader()

        val size = parcel.readInt()
        _settingClasses = Array(size) {
            val name = parcel.readNonNullString()
            Class.forName(name, true, classLoader) as SettingClass
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        val sc = _settingClasses
        if(sc != null) {
            parcel.writeInt(sc.size)
            for (c in sc) {
                parcel.writeString(c.name)
            }
        } else {
            val settings = _settings ?: throw IllegalStateException("Something goes wrong. _settings == null")

            parcel.writeInt(settings.size)
            for(setting in settings) {
                parcel.writeString(setting.javaClass.name)
            }
        }
    }

    override fun describeContents(): Int = 0

    companion object {
        @JvmField
        val CREATOR = object: Parcelable.Creator<SettingsContext> {
            override fun createFromParcel(parcel: Parcel): SettingsContext {
                return SettingsContext(parcel)
            }

            override fun newArray(size: Int): Array<SettingsContext?> {
                return arrayOfNulls(size)
            }
        }
    }
}