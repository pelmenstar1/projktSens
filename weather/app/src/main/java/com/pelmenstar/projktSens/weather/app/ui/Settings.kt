package com.pelmenstar.projktSens.weather.app.ui

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.GridLayout
import androidx.annotation.ArrayRes
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.widget.addTextChangedListener
import com.google.android.material.switchmaterial.SwitchMaterial
import com.pelmenstar.projktSens.serverProtocol.ContractType
import com.pelmenstar.projktSens.shared.EmptyArray
import com.pelmenstar.projktSens.shared.InetAddressUtils
import com.pelmenstar.projktSens.shared.StringUtils
import com.pelmenstar.projktSens.shared.android.Preferences
import com.pelmenstar.projktSens.shared.android.ReadonlyArrayAdapter
import com.pelmenstar.projktSens.shared.android.ui.EditText
import com.pelmenstar.projktSens.shared.android.ui.chooseServerHost.ChooseServerHostView
import com.pelmenstar.projktSens.shared.android.ui.settings.Setting
import com.pelmenstar.projktSens.shared.android.ui.settings.SettingGroup
import com.pelmenstar.projktSens.shared.equalsPattern
import com.pelmenstar.projktSens.weather.app.AppPreferences
import com.pelmenstar.projktSens.weather.app.R
import com.pelmenstar.projktSens.weather.models.ValueUnit
import com.pelmenstar.projktSens.weather.models.ValueUnitsPacked

@JvmField
val APP_SETTING_GROUPS: Array<out SettingGroup> = arrayOf(
    SettingGroup(
        TemperatureSetting::class.java,
        PressureSetting::class.java,
    ),
    SettingGroup(
        ServerHostSetting::class.java,
        ServerContractSetting::class.java,
        WeatherReceiveIntervalSetting::class.java
    ),
    SettingGroup(
        KeepHomeScreenOnSetting::class.java
    )
)

abstract class ValueUnitSetting : Setting<ValueUnitSetting.State>() {
    data class State(@JvmField var unit: Int)

    private val bundleKey = createBundleKey(javaClass)

    abstract val packedUnitType: Int
    abstract val spinnerInfo: Long

    override fun createView(context: Context): View {
        return AppCompatSpinner(context).apply {
            val si = spinnerInfo
            val arrayRes = getArrayRes(si)
            val unitOffset = getUnitOffset(si)
            val valuesCount = getValuesCount(si)

            adapter = simpleArrayAdapter(context, arrayRes)

            val index = state.unit - unitOffset
            if (index < 0 || index > valuesCount) {
                throw RuntimeException("Invalid unit")
            }
            setSelection(index)

            onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    if (position > valuesCount) {
                        throw RuntimeException("Invalid position")
                    }
                    state.unit = position + unitOffset
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    override fun saveStateToPrefs(prefs: Preferences) {
        val units = prefs.getInt(AppPreferences.UNITS)
        val newUnits = ValueUnitsPacked.withUnit(units, packedUnitType, state.unit)

        prefs.setInt(AppPreferences.UNITS, newUnits)
    }

    override fun saveStateToBundle(outState: Bundle) {
        outState.putInt(bundleKey, state.unit)
    }

    override fun loadStateFromPrefs(prefs: Preferences) {
        val units = prefs.getInt(AppPreferences.UNITS)
        state = State(ValueUnitsPacked.getUnit(units, packedUnitType))
    }

    override fun loadStateFromBundle(bundle: Bundle): Boolean {
        val unit = bundle.getInt(bundleKey, ValueUnit.NONE)
        return if (unit != ValueUnit.NONE) {
            state = State(unit)
            true
        } else {
            false
        }
    }

    companion object {
        private fun createBundleKey(c: Class<*>): String {
            val className = c.name
            val dotIndex = className.lastIndexOf('.')
            var reservedLength = className.length + 10
            if (dotIndex >= 0) {
                reservedLength -= dotIndex
            }

            return buildString(reservedLength) {
                if (dotIndex < 0) {
                    append(className)
                } else {
                    append(className, dotIndex, className.length)
                }

                append(".state.unit")
            }
        }

        @JvmStatic
        protected fun spinnerInfo(
            @ArrayRes arrayRes: Int,
            unitOffset: Int,
            valuesCount: Int
        ): Long {
            return (valuesCount.toLong() shl 48) or (unitOffset.toLong() shl 32) or arrayRes.toLong()
        }

        @ArrayRes
        private fun getArrayRes(spinnerInfo: Long): Int {
            return spinnerInfo.toInt()
        }

        private fun getUnitOffset(spinnerInfo: Long): Int {
            return (spinnerInfo shr 32).toInt() and 0xffff
        }

        private fun getValuesCount(spinnerInfo: Long): Int {
            return (spinnerInfo shr 48).toInt() and 0xffff
        }
    }
}

class TemperatureSetting : ValueUnitSetting() {
    override val nameId: Int get() = R.string.temperature

    override val spinnerInfo: Long = spinnerInfo(R.array.temperatureUnits, 0, 3)

    override val packedUnitType: Int
        get() = ValueUnitsPacked.TYPE_TEMPERATURE
}

class PressureSetting : ValueUnitSetting() {
    override val nameId: Int get() = R.string.pressure

    override val spinnerInfo: Long = spinnerInfo(R.array.pressureUnits, 4, 2)

    override val packedUnitType: Int
        get() = ValueUnitsPacked.TYPE_PRESSURE
}

class ServerHostSetting : Setting<ServerHostSetting.State>() {
    data class State(var address: Int, var port: Int, val contractId: Int)

    override val nameId: Int get() = R.string.serverHost

    override fun createView(context: Context): View {
        return ChooseServerHostView(context).apply {
            contractId = state.contractId
            setHost(state.address, state.port)

            onHostChangedByUser = { address, port ->
                state.address = address
                state.port = port
            }
        }
    }

    override fun saveStateToPrefs(prefs: Preferences) {
        prefs.setInt(AppPreferences.SERVER_HOST_INT, state.address)
        prefs.setInt(AppPreferences.SERVER_PORT, state.port)
    }

    override fun saveStateToBundle(outState: Bundle) {
        outState.putInt(BUNDLE_STATE_ADDRESS, state.address)
        outState.putInt(BUNDLE_STATE_PORT, state.port)
        outState.putInt(BUNDLE_STATE_CONTRACT_ID, state.contractId)
    }

    override fun loadStateFromPrefs(prefs: Preferences) {
        state = State(
            prefs.getInt(AppPreferences.SERVER_HOST_INT),
            prefs.getInt(AppPreferences.SERVER_PORT),
            prefs.getInt(AppPreferences.CONTRACT)
        )
    }

    override fun loadStateFromBundle(bundle: Bundle): Boolean {
        val address = bundle.get(BUNDLE_STATE_ADDRESS) as Int?
        val port = bundle.get(BUNDLE_STATE_PORT) as Int?
        val contractId = bundle.get(BUNDLE_STATE_CONTRACT_ID) as Int?

        return if(address != null && port != null && contractId != null) {
            state = State(address, port, contractId)

            true
        } else false
    }

    companion object {
        private const val BUNDLE_STATE_ADDRESS = "ServerHostSetting.State.address"
        private const val BUNDLE_STATE_PORT = "ServerHostSetting.State.port"
        private const val BUNDLE_STATE_CONTRACT_ID = "ServerHostSetting.State.contractId"
    }
}

class ServerContractSetting : Setting<ServerContractSetting.State>() {
    data class State(@JvmField var contractType: Int)

    override val nameId: Int
        get() = R.string.serverContract

    override fun createView(context: Context): View {
        return AppCompatSpinner(context).apply {
            adapter = simpleArrayAdapter(context, R.array.serverContracts)

            setSelection(
                when (state.contractType) {
                    ContractType.RAW -> 0
                    else -> throw RuntimeException("Invalid state.contractType")
                }
            )

            onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    state.contractType = when (position) {
                        0 -> ContractType.RAW
                        else -> return
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    override fun saveStateToPrefs(prefs: Preferences) {
        prefs.setInt(AppPreferences.CONTRACT, state.contractType)
    }

    override fun saveStateToBundle(outState: Bundle) {
        outState.putInt(BUNDLE_STATE_CONTRACT_TYPE, state.contractType)
    }

    override fun loadStateFromPrefs(prefs: Preferences) {
        state = State(prefs.getInt(AppPreferences.CONTRACT))
    }

    override fun loadStateFromBundle(bundle: Bundle): Boolean {
        val type = bundle.get(BUNDLE_STATE_CONTRACT_TYPE)
        return if (type != null) {
            state = State(type as Int)

            true
        } else {
            false
        }
    }

    companion object {
        private const val BUNDLE_STATE_CONTRACT_TYPE = "ServerContractSetting.State.contractType"
    }
}

class WeatherReceiveIntervalSetting : Setting<WeatherReceiveIntervalSetting.State>() {
    class State(interval: Int) : IncompleteState() {
        var interval: Int = 0
            set(value) {
                field = value
                isValid = (value > 0)
            }

        init {
            // if we assign value from constructor like:
            // var interval: Int = interval
            // Custom setter won't be called, that's not OK
            this.interval = interval
        }

        override fun equals(other: Any?): Boolean {
            return equalsPattern(other) { o ->
                interval == o.interval
            }
        }

        override fun hashCode(): Int {
            return interval
        }
    }

    override val nameId: Int
        get() = R.string.settings_weatherRcvInterval_name

    override fun createView(context: Context): View {
        val res = context.resources
        val invalidNumberStr = res.getString(R.string.invalidNumberFormat)
        val lessOrZeroErrorStr = res.getString(R.string.settings_weatherRcvInterval_lessOrEqZero)

        return EditText(context) {
            setText(state.interval.toString())
            if (!state.isValid) {
                error = lessOrZeroErrorStr
            }

            inputType = InputType.TYPE_CLASS_NUMBER

            addTextChangedListener { text ->
                if (text != null) {
                    val state = state

                    val interval = StringUtils.parsePositiveInt(text)
                    if (interval != -1) {
                        state.interval = interval

                        if (interval <= 0) {
                            error = lessOrZeroErrorStr
                        }
                    } else {
                        error = invalidNumberStr
                        state.isValid = false
                    }
                }
            }
        }
    }

    override fun saveStateToPrefs(prefs: Preferences) {
        prefs.setInt(AppPreferences.WEATHER_RECEIVE_INTERVAL, state.interval)
    }

    override fun saveStateToBundle(outState: Bundle) {
        outState.putInt(BUNDLE_STATE_INTERVAL, state.interval)
    }

    override fun loadStateFromPrefs(prefs: Preferences) {
        state = State(prefs.getInt(AppPreferences.WEATHER_RECEIVE_INTERVAL))
    }

    override fun loadStateFromBundle(bundle: Bundle): Boolean {
        val interval = bundle.get(BUNDLE_STATE_INTERVAL)
        return if (interval != null) {
            state = State(interval as Int)
            true
        } else {
            false
        }
    }

    companion object {
        private const val BUNDLE_STATE_INTERVAL = "WeatherReceiveIntervalSetting.State.interval"
    }
}

class KeepHomeScreenOnSetting: Setting<KeepHomeScreenOnSetting.State>() {
    class State(@JvmField var isEnabled: Boolean) {
        override fun equals(other: Any?): Boolean {
            return equalsPattern(other) { o -> isEnabled == o.isEnabled }
        }

        override fun hashCode(): Int {
            return if(isEnabled) 1 else 0
        }
    }

    override val nameId: Int
        get() = R.string.settings_lockHomeScreen

    override fun createView(context: Context): View {
        return SwitchMaterial(context).apply {
            isChecked = state.isEnabled

            setOnCheckedChangeListener { _, isChecked ->
                state.isEnabled = isChecked
            }
        }
    }

    override fun loadStateFromPrefs(prefs: Preferences) {
        state = State(prefs.getBoolean(AppPreferences.KEEP_HOME_SCREEN_ON))
    }

    override fun loadStateFromBundle(bundle: Bundle): Boolean {
        val isEnabled = bundle.get(BUNDLE_STATE_KEEP_HOME_SCREEN_ON) as Boolean?
        return if(isEnabled != null) {
            state = State(isEnabled)
            true
        } else {
            false
        }
    }

    override fun saveStateToPrefs(prefs: Preferences) {
        prefs.setBoolean(AppPreferences.KEEP_HOME_SCREEN_ON, state.isEnabled)
    }

    override fun saveStateToBundle(outState: Bundle) {
        outState.putBoolean(BUNDLE_STATE_KEEP_HOME_SCREEN_ON, state.isEnabled)
    }

    companion object {
        private const val BUNDLE_STATE_KEEP_HOME_SCREEN_ON = "KeepHomeScreenOnSetting.state.isKeepHomeScreenOn"
    }
}

private fun simpleArrayAdapter(
    context: Context,
    @ArrayRes resId: Int
): ReadonlyArrayAdapter<String> {
    return ReadonlyArrayAdapter(
        context,
        android.R.layout.simple_spinner_item,
        context.resources.getStringArray(resId)
    ).apply {
        setDropDownResource(android.R.layout.simple_spinner_dropdown_item)
    }
}