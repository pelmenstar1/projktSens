package com.pelmenstar.projktSens.jserver

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.AdapterView
import androidx.annotation.ArrayRes
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.widget.addTextChangedListener
import com.pelmenstar.projktSens.serverProtocol.ContractType
import com.pelmenstar.projktSens.shared.InetAddressUtils
import com.pelmenstar.projktSens.shared.StringUtils
import com.pelmenstar.projktSens.shared.android.Preferences
import com.pelmenstar.projktSens.shared.android.ReadonlyArrayAdapter
import com.pelmenstar.projktSens.shared.android.ui.EditText
import com.pelmenstar.projktSens.shared.android.ui.settings.Setting
import com.pelmenstar.projktSens.shared.android.ui.settings.SettingGroup
import com.pelmenstar.projktSens.shared.equalsPattern

@JvmField
val APP_SETTING_GROUPS: Array<out SettingGroup> = arrayOf(
    SettingGroup(
        ServerPortSetting::class.java,
        ServerContractSetting::class.java,
        WeatherSendIntervalSetting::class.java
    )
)

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

    override val nameId: Int
        get() = R.string.settings_serverPortName

    override fun createView(context: Context): View {
        val res = context.resources
        val invalidPortNumberStr = res.getString(R.string.invalidNumberFormat)
        val portReservedErrorLess = res.getString(R.string.settings_portReservedError_less)
        val portReservedErrorGreater = res.getString(R.string.settings_portReservedError_greater)

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

class ServerContractSetting : Setting<ServerContractSetting.State>() {
    data class State(@JvmField var contractType: Int)

    override val nameId: Int get() = R.string.settings_serverContract

    override fun createView(context: Context): View {
        return AppCompatSpinner(context).apply {
            adapter = simpleArrayAdapter(context, R.array.serverContracts)

            setSelection(
                when (state.contractType) {
                    ContractType.RAW -> 0
                    ContractType.JSON -> 1
                    else -> throw RuntimeException("Invalid state.contractType")
                }
            )

            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    state.contractType = when (position) {
                        0 -> ContractType.RAW
                        1 -> ContractType.JSON

                        else -> return
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    override fun saveStateToPrefs(prefs: Preferences) {
        prefs.setInt(AppPreferences.SERVER_CONTRACT, state.contractType)
    }

    override fun saveStateToBundle(outState: Bundle) {
        outState.putInt(BUNDLE_STATE_CONTRACT_TYPE, state.contractType)
    }

    override fun loadStateFromPrefs(prefs: Preferences) {
        state = State(prefs.getInt(AppPreferences.SERVER_CONTRACT))
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

class WeatherSendIntervalSetting : Setting<WeatherSendIntervalSetting.State>() {
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
        get() = R.string.settings_weatherSendInterval_name

    override fun createView(context: Context): View {
        val res = context.resources
        val invalidNumberStr = res.getString(R.string.invalidNumberFormat)
        val lessOrZeroErrorStr = res.getString(R.string.settings_weatherSendInterval_lessOrEqZero)

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
        prefs.setInt(AppPreferences.WEATHER_SEND_INTERVAL, state.interval)
    }

    override fun saveStateToBundle(outState: Bundle) {
        outState.putInt(BUNDLE_STATE_INTERVAL, state.interval)
    }

    override fun loadStateFromPrefs(prefs: Preferences) {
        state = State(prefs.getInt(AppPreferences.WEATHER_SEND_INTERVAL))
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
        private const val BUNDLE_STATE_INTERVAL = "WeatherSendIntervalSetting.State.interval"
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