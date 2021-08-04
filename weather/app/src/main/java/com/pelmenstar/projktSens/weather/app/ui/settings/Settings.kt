package com.pelmenstar.projktSens.weather.app.ui.settings

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import androidx.annotation.ArrayRes
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.widget.addTextChangedListener
import com.pelmenstar.projktSens.serverProtocol.repo.RepoContractType
import com.pelmenstar.projktSens.shared.InetAddressUtils
import com.pelmenstar.projktSens.shared.android.Preferences
import com.pelmenstar.projktSens.shared.android.ReadonlyArrayAdapter
import com.pelmenstar.projktSens.shared.android.ui.EditText
import com.pelmenstar.projktSens.shared.android.ui.settings.Setting
import com.pelmenstar.projktSens.shared.equalsPattern
import com.pelmenstar.projktSens.weather.app.AppPreferences
import com.pelmenstar.projktSens.weather.app.R
import com.pelmenstar.projktSens.weather.models.ValueUnit
import com.pelmenstar.projktSens.weather.models.ValueUnitsPacked

val APP_SETTING_CLASSES: Array<out Class<out Setting<*>>> = arrayOf(
    TemperatureSetting::class.java,
    PressureSetting::class.java,
    ServerHostSetting::class.java,
    ServerContractSetting::class.java,
    RepoPortSetting::class.java,
    WeatherReceiveIntervalSetting::class.java
)

data class ValueUnitState(@JvmField var unit: Int)

class TemperatureSetting: Setting<ValueUnitState>() {
    override fun getNameId(): Int {
        return R.string.temperature
    }

    override fun createView(context: Context): View {
        return AppCompatSpinner(context).apply {
            adapter = simpleArrayAdapter(context, R.array.temperatureUnits)

            setSelection(
                when (state.unit) {
                    ValueUnit.CELSIUS -> 0
                    ValueUnit.KELVIN -> 1
                    ValueUnit.FAHRENHEIT -> 2
                    else -> throw IllegalStateException("Illegal prefs")
                }
            )

            onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    state.unit = when (position) {
                        0 -> ValueUnit.CELSIUS
                        1 -> ValueUnit.KELVIN
                        2 -> ValueUnit.FAHRENHEIT

                        else -> return
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    override fun saveStateToPrefs(prefs: Preferences) {
        val pressUnit = ValueUnitsPacked.getPressureUnit(prefs.getInt(AppPreferences.UNITS))
        val newUnits = ValueUnitsPacked.create(state.unit, pressUnit)

        prefs.setInt(AppPreferences.UNITS, newUnits)
    }

    override fun saveStateToBundle(outState: Bundle) {
        outState.putInt(BUNDLE_STATE_UNIT, state.unit)
    }

    override fun loadStateFromPrefs(prefs: Preferences) {
        val units = prefs.getInt(AppPreferences.UNITS)
        state = ValueUnitState(ValueUnitsPacked.getTemperatureUnit(units))
    }

    override fun loadStateFromBundle(bundle: Bundle): Boolean {
        val unit = bundle.getInt(BUNDLE_STATE_UNIT, ValueUnit.NONE)
        return if(unit != ValueUnit.NONE) {
            state = ValueUnitState(unit)
            true
        } else {
            false
        }
    }

    companion object {
        private const val BUNDLE_STATE_UNIT = "TemperatureSetting.State.unit"
    }
}

class PressureSetting: Setting<ValueUnitState>() {
    override fun getNameId(): Int {
        return R.string.pressure
    }

    override fun createView(context: Context): View {
        return AppCompatSpinner(context).apply {
            adapter = simpleArrayAdapter(context, R.array.pressureUnits)

            setSelection(when(state.unit) {
                ValueUnit.MM_OF_MERCURY -> 0
                ValueUnit.PASCAL -> 1
                else -> throw IllegalStateException("Illegal prefs")
            })

            onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    state.unit = when (position) {
                        0 -> ValueUnit.MM_OF_MERCURY
                        1 -> ValueUnit.PASCAL

                        else -> return
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    override fun saveStateToPrefs(prefs: Preferences) {
        val tempUnit = ValueUnitsPacked.getTemperatureUnit(prefs.getInt(AppPreferences.UNITS))
        val newUnits = ValueUnitsPacked.create(tempUnit, state.unit)

        prefs.setInt(AppPreferences.UNITS, newUnits)
    }

    override fun saveStateToBundle(outState: Bundle) {
        outState.putInt(BUNDLE_STATE_UNIT, state.unit)
    }

    override fun loadStateFromPrefs(prefs: Preferences) {
        val units = prefs.getInt(AppPreferences.UNITS)

        state = ValueUnitState(ValueUnitsPacked.getPressureUnit(units))
    }

    override fun loadStateFromBundle(bundle: Bundle): Boolean {
        val unit = bundle.getInt(BUNDLE_STATE_UNIT, ValueUnit.NONE)
        return if(unit != ValueUnit.NONE) {
            state = ValueUnitState(unit)
            true
        } else {
            false
        }
    }

    companion object {
        private const val BUNDLE_STATE_UNIT = "PressureSetting.State.unit"
    }
}

class ServerHostSetting: Setting<ServerHostSetting.State>() {
    class State: IncompleteState {
        private var _hostString: String = ""
        var hostString: String
            get() = _hostString
            set(value) {
                _hostString = value

                isValid = InetAddressUtils.isValidNumericalIpv4(value)
            }

        constructor(hostString: String) {
            this.hostString = hostString
        }

        constructor(hostString: String, notParseHostMarker: Boolean) {
            _hostString = hostString
        }

        override fun equals(other: Any?): Boolean {
            return equalsPattern(other) { o ->
                _hostString == o._hostString
            }
        }

        override fun hashCode(): Int {
            return _hostString.hashCode()
        }
    }

    override fun getNameId(): Int {
        return R.string.serverHost
    }

    override fun createView(context: Context): View {
        val invalidAddressStr = context.resources.getString(R.string.serverIsNotAvailable_settings)

        return EditText(context) {
            setText(state.hostString)
            if(!state.isValid) {
                error = invalidAddressStr
            }

            addTextChangedListener {
                if(it != null) {
                    val state = state
                    val text = it.toString()
                    state.hostString = text
                    if (!state.isValid) {
                        error = invalidAddressStr
                    }
                }
            }
        }
    }

    override fun saveStateToPrefs(prefs: Preferences) {
        prefs[AppPreferences.SERVER_HOST] = state.hostString
    }

    override fun saveStateToBundle(outState: Bundle) {
        outState.putString(BUNDLE_STATE_HOST, state.hostString)
    }

    override fun loadStateFromPrefs(prefs: Preferences) {
        state = State(prefs[AppPreferences.SERVER_HOST] as String, false)
    }

    override fun loadStateFromBundle(bundle: Bundle): Boolean {
        val hostString = bundle.getString(BUNDLE_STATE_HOST)
        return if(hostString != null) {
            state = State(hostString)

            true
        } else {
            false
        }
    }

    companion object {
        private const val BUNDLE_STATE_HOST = "ServerHostSetting.State.host"
    }
}

class ServerContractSetting: Setting<ServerContractSetting.State>() {
    data class State(@JvmField var contractType: Int)

    override fun getNameId(): Int {
        return R.string.serverContract
    }

    override fun createView(context: Context): View {
        return AppCompatSpinner(context).apply {
            adapter = simpleArrayAdapter(context, R.array.serverContracts)

            setSelection(when(state.contractType) {
                RepoContractType.CONTRACT_RAW -> 0
                RepoContractType.CONTRACT_JSON -> 1
                else -> throw RuntimeException("Invalid state.contractType")
            })

            onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    state.contractType = when (position) {
                        0 -> RepoContractType.CONTRACT_RAW
                        1 -> RepoContractType.CONTRACT_JSON

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
        val type = bundle.getInt(BUNDLE_STATE_CONTRACT_TYPE, -1)
        return if(type != -1) {
            state = State(type)

            true
        } else {
            false
        }
    }

    companion object {
        private const val BUNDLE_STATE_CONTRACT_TYPE = "ServerContractSetting.State.contractType"
    }
}

abstract class PortSettingBase: Setting<PortSettingBase.State>() {
    class State(port: Int): IncompleteState() {
        var port: Int = 0
            set(value) {
                field = value
                isValid = isValidPort(value)
            }

        init {
            // if we assign value from constructor like:
            // var port: Int = port
            // Custom setter won't be called, that's not OK
            this.port = port
        }

        override fun equals(other: Any?): Boolean {
            return equalsPattern(other) { o ->
                port == o.port
            }
        }

        override fun hashCode(): Int {
            return port
        }
    }

    override fun createView(context: Context): View {
        val res = context.resources
        val invalidPortNumberStr = res.getString(R.string.invalidNumberFormat)
        val portReservedErrorLess = res.getString(R.string.settings_portReservedError_less)
        val portReservedErrorGreater = res.getString(R.string.settings_portReservedError_greater)

        return EditText(context) {
            fun setErrorIfInvalidPort(port: Int) {
                when {
                    (port < PORT_MIN) -> {
                        error = portReservedErrorLess
                    }
                    (port > PORT_MAX) -> {
                        error = portReservedErrorGreater
                    }
                }
            }

            setText(state.port.toString())
            setErrorIfInvalidPort(state.port)

            inputType = InputType.TYPE_CLASS_NUMBER

            addTextChangedListener {
                if (it != null) {
                    val state = state
                    val text = it.toString()

                    try {
                        val port = text.toInt()
                        state.port = port

                        setErrorIfInvalidPort(port)
                    } catch (e: Exception) {
                        error = invalidPortNumberStr
                        state.isValid = false
                    }
                }
            }
        }
    }

    override fun saveStateToPrefs(prefs: Preferences) {
        prefs.setInt(getPreferencesKey(), state.port)
    }

    override fun saveStateToBundle(outState: Bundle) {
        outState.putInt(getBundleStatePortKey(), state.port)
    }

    override fun loadStateFromPrefs(prefs: Preferences) {
        val port = prefs.getInt(getPreferencesKey())
        state = State(port)
    }

    override fun loadStateFromBundle(bundle: Bundle): Boolean {
        val port = bundle.getInt(getBundleStatePortKey(), -1)

        return if(port != -1) {
            state = State(port)

            true
        } else {
            false
        }
    }

    protected abstract fun getBundleStatePortKey(): String
    protected abstract fun getPreferencesKey(): Int

    companion object {
        private const val PORT_MIN = 1024
        private const val PORT_MAX = 49151

        // maybe isn't the best place for such method
        private fun isValidPort(port: Int): Boolean {
           return port in (PORT_MIN..PORT_MAX)
        }
    }
}

class RepoPortSetting: PortSettingBase() {
    override fun getNameId(): Int {
        return R.string.settings_repoPortName
    }

    override fun getBundleStatePortKey(): String {
        return "RepoPortSetting.State.port"
    }

    override fun getPreferencesKey(): Int = AppPreferences.REPO_PORT
}

class WeatherReceiveIntervalSetting: Setting<WeatherReceiveIntervalSetting.State>() {
    class State(interval: Int): IncompleteState() {
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

    override fun getNameId(): Int {
        return R.string.settings_weatherRcvInterval_name
    }

    override fun createView(context: Context): View {
        val res = context.resources
        val invalidNumberStr = res.getString(R.string.invalidNumberFormat)
        val lessOrZeroErrorStr = res.getString(R.string.settings_weatherRcvInterval_lessOrEqZero)

        return EditText(context) {
            setText(state.interval.toString())
            if(!state.isValid) {
                error = lessOrZeroErrorStr
            }

            inputType = InputType.TYPE_CLASS_NUMBER

            addTextChangedListener {
                if (it != null) {
                    val state = state
                    val text = it.toString()

                    try {
                        val interval = text.toInt()
                        state.interval = interval

                        if (interval <= 0) {
                            error = lessOrZeroErrorStr
                        }
                    } catch (e: Exception) {
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
        val interval = bundle.getInt(BUNDLE_STATE_INTERVAL, -1)
        return if(interval != -1) {
            state = State(interval)
            true
        } else {
            false
        }
    }

    companion object {
        private const val BUNDLE_STATE_INTERVAL = "WeatherReceiveIntervalSetting.State.interval"
    }
}

private fun simpleArrayAdapter(context: Context, @ArrayRes resId: Int): ReadonlyArrayAdapter<String> {
    return ReadonlyArrayAdapter(
        context,
        android.R.layout.simple_spinner_item,
        context.resources.getStringArray(resId)
    ).apply {
        setDropDownResource(android.R.layout.simple_spinner_dropdown_item)
    }
}