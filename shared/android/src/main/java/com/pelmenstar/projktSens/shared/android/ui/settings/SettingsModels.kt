package com.pelmenstar.projktSens.shared.android.ui.settings

import android.content.Context
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.View
import androidx.annotation.StringRes
import com.pelmenstar.projktSens.shared.ReflectionUtils
import com.pelmenstar.projktSens.shared.android.Preferences
import com.pelmenstar.projktSens.shared.android.ext.readNonNullString
import com.pelmenstar.projktSens.shared.android.ui.*

private typealias SettingClass = Class<out Setting<*>>

class SettingGroup: Parcelable {
    private var _items: Array<out Setting<*>>? = null
    private var _itemClasses: Array<out SettingClass>? = null

    val items: Array<out Setting<*>>
        get() {
            var items = _items
            if(items != null) {
                return items
            }

            val classes = _itemClasses ?: throw RuntimeException("_classes == null")

            items = Array(classes.size) { i ->
                ReflectionUtils.createFromEmptyConstructor(classes[i])
            }
            _items = items

            return items
        }

    val itemClasses: Array<out SettingClass>
        get() {
            var itemClasses = _itemClasses
            if(itemClasses != null) {
                return itemClasses
            }

            val items = _items ?: throw RuntimeException("_items == null")
            itemClasses = Array(items.size) { i -> items[i].javaClass }
            _itemClasses = itemClasses

            return itemClasses
        }

    constructor(items: Array<out Setting<*>>) {
        _items = items
    }

    constructor(vararg itemClassNames: SettingClass) {
        _itemClasses = itemClassNames
    }

    constructor(parcel: Parcel) {
        val itemsCount = parcel.readInt()
        _itemClasses = Array(itemsCount) {
            Class.forName(parcel.readNonNullString()) as SettingClass
        }
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        val items = _items
        if(items != null) {
            dest.writeInt(items.size)
            for (setting in items) {
                dest.writeString(setting.javaClass.name)
            }
        } else {
            val classes = _itemClasses!!

            dest.writeInt(classes.size)
            for(c in classes) {
                dest.writeString(c.name)
            }
        }
    }

    companion object {
        @JvmField
        val CREATOR = object: Parcelable.Creator<SettingGroup> {
            override fun createFromParcel(source: Parcel) = SettingGroup(source)
            override fun newArray(size: Int) = arrayOfNulls<SettingGroup>(size)
        }
    }
}

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

                if (value != oldValue) {
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
    @get:StringRes
    abstract val nameId: Int

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