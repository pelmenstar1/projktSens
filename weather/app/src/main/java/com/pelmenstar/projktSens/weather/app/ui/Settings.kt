package com.pelmenstar.projktSens.weather.app.ui

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import androidx.annotation.ArrayRes
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.widget.addTextChangedListener
import com.pelmenstar.projktSens.serverProtocol.ContractType
import com.pelmenstar.projktSens.shared.EmptyArray
import com.pelmenstar.projktSens.shared.InetAddressUtils
import com.pelmenstar.projktSens.shared.StringUtils
import com.pelmenstar.projktSens.shared.android.Preferences
import com.pelmenstar.projktSens.shared.android.ReadonlyArrayAdapter
import com.pelmenstar.projktSens.shared.android.ui.EditText
import com.pelmenstar.projktSens.shared.android.ui.settings.Setting
import com.pelmenstar.projktSens.shared.equalsPattern
import com.pelmenstar.projktSens.weather.app.AppPreferences
import com.pelmenstar.projktSens.weather.app.R
import com.pelmenstar.projktSens.weather.models.ValueUnit
import com.pelmenstar.projktSens.weather.models.ValueUnitsPacked

@JvmField
val APP_SETTING_CLASSES: Array<out Class<out Setting<*>>> = arrayOf(
    TemperatureSetting::class.java,
    PressureSetting::class.java,
    ServerHostSetting::class.java,
    ServerContractSetting::class.java,
    ServerPortSetting::class.java,
    WeatherReceiveIntervalSetting::class.java
)

@JvmField
val APP_SETTING_CLASS_NAMES: Array<out String> = Array(APP_SETTING_CLASSES.size) { i ->
    APP_SETTING_CLASSES[i].name
}

abstract class ValueUnitSetting : Setting<ValueUnitSetting.State>() {
    data class State(@JvmField var unit: Int)

    private val bundleKey = "${javaClass.name}.state.unit"

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
    class State : IncompleteState {
        private var _hostBuffer = EmptyArray.CHAR
        var hostBuffer: CharArray
            get() = _hostBuffer
            set(value) {
                _hostBuffer = value

                val ipInt = InetAddressUtils.parseNumericalIpv4ToInt(value)
                if (ipInt != InetAddressUtils.IP_ERROR) {
                    isValid = true
                    _hostInt = ipInt
                } else {
                    isValid = false
                }
            }

        private var _hostInt: Int = 0
        var hostInt: Int
            get() = _hostInt
            set(value) {
                _hostInt = value
                _hostBuffer = InetAddressUtils.intIpv4ToCharArray(_hostBuffer, value)
                isValid = value != InetAddressUtils.IP_ERROR
            }

        constructor(hostBuffer: CharArray) {
            this.hostBuffer = hostBuffer
        }

        constructor(ipInt: Int) {
            hostInt = ipInt
        }

        internal fun setHost(text: Editable) {
            val textLength = text.length
            val buffer = if (_hostBuffer.size != textLength) {
                _hostBuffer
            } else {
                CharArray(textLength)
            }
            text.getChars(0, textLength, buffer, 0)

            hostBuffer = buffer
        }

        override fun equals(other: Any?): Boolean {
            return equalsPattern(other) { o ->
                _hostBuffer.contentEquals(o._hostBuffer)
            }
        }

        override fun hashCode(): Int {
            return _hostBuffer.contentHashCode()
        }
    }

    override val nameId: Int get() = R.string.serverHost

    override fun createView(context: Context): View {
        val invalidAddressStr = context.resources.getString(R.string.invalidInetAddress)

        return EditText(context) {
            val initialHost = state.hostBuffer
            setText(initialHost, 0, initialHost.size)
            if (!state.isValid) {
                error = invalidAddressStr
            }

            addTextChangedListener { text ->
                if (text != null) {
                    val state = state
                    state.setHost(text)

                    if (!state.isValid) {
                        error = invalidAddressStr
                    }
                }
            }
        }
    }

    override fun saveStateToPrefs(prefs: Preferences) {
        prefs.setInt(AppPreferences.SERVER_HOST_INT, state.hostInt)
    }

    override fun saveStateToBundle(outState: Bundle) {
        outState.putCharArray(BUNDLE_STATE_HOST, state.hostBuffer)
    }

    override fun loadStateFromPrefs(prefs: Preferences) {
        state = State(prefs.getInt(AppPreferences.SERVER_HOST_INT))
    }

    override fun loadStateFromBundle(bundle: Bundle): Boolean {
        val hostBuffer = bundle.getCharArray(BUNDLE_STATE_HOST)
        return if (hostBuffer != null) {
            state = State(hostBuffer)

            true
        } else {
            false
        }
    }

    companion object {
        private const val BUNDLE_STATE_HOST = "ServerHostSetting.State.host"
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
                    ContractType.CONTRACT_RAW -> 0
                    ContractType.CONTRACT_JSON -> 1
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
                        0 -> ContractType.CONTRACT_RAW
                        1 -> ContractType.CONTRACT_JSON

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

class ServerPortSetting : Setting<ServerPortSetting.State>() {
    class State(port: Int) : IncompleteState() {
        var port: Int = 0
            set(value) {
                field = value
                isValid = InetAddressUtils.isValidFreePort(value)
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

    override val nameId: Int get() = R.string.port

    override fun createView(context: Context): View {
        val res = context.resources
        val invalidPortNumberStr = res.getString(R.string.invalidNumberFormat)
        val portReservedErrorLess = res.getString(R.string.portReservedError_less)
        val portReservedErrorGreater = res.getString(R.string.portReservedError_greater)

        return EditText(context) {
            fun setErrorIfInvalidPort(port: Int) {
                when {
                    (port < InetAddressUtils.FREE_MIN_PORT) -> {
                        error = portReservedErrorLess
                    }
                    (port > InetAddressUtils.FREE_MAX_PORT) -> {
                        error = portReservedErrorGreater
                    }
                }
            }

            setText(state.port.toString())
            setErrorIfInvalidPort(state.port)

            inputType = InputType.TYPE_CLASS_NUMBER

            addTextChangedListener { text ->
                if (text != null) {
                    val state = state

                    val port = StringUtils.parsePositiveInt(text)
                    if (port != -1) {
                        state.port = port

                        setErrorIfInvalidPort(port)
                    } else {
                        error = invalidPortNumberStr
                        state.isValid = false
                    }
                }
            }
        }
    }

    override fun saveStateToPrefs(prefs: Preferences) {
        prefs.setInt(AppPreferences.SERVER_PORT, state.port)
    }

    override fun saveStateToBundle(outState: Bundle) {
        outState.putInt(BUNDLE_PORT, state.port)
    }

    override fun loadStateFromPrefs(prefs: Preferences) {
        val port = prefs.getInt(AppPreferences.SERVER_PORT)
        state = State(port)
    }

    override fun loadStateFromBundle(bundle: Bundle): Boolean {
        val port = bundle.get(BUNDLE_PORT)

        return if (port != null) {
            state = State(port as Int)

            true
        } else {
            false
        }
    }


    companion object {
        private const val BUNDLE_PORT = "ServerPortSetting.state.port"
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