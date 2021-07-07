package com.pelmenstar.projktSens.jserver

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.AdapterView
import androidx.annotation.ArrayRes
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.widget.addTextChangedListener
import com.pelmenstar.projktSens.serverProtocol.repo.RepoContractType
import com.pelmenstar.projktSens.shared.android.Preferences
import com.pelmenstar.projktSens.shared.android.ReadonlyArrayAdapter
import com.pelmenstar.projktSens.shared.android.ui.EditText
import com.pelmenstar.projktSens.shared.android.ui.settings.Setting
import com.pelmenstar.projktSens.shared.equalsPattern

@JvmField
val SETTINGS: Array<out Setting<*>> = arrayOf(
    RepoPortSetting(),
    WciPortSetting(),
    ServerContractSetting(),
    WeatherSendIntervalSetting()
)

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
    override fun getNameId(): Int = R.string.settings_repoPortName
    override fun getBundleStatePortKey(): String = "RepoPortSetting.State.port"
    override fun getPreferencesKey(): Int = AppPreferences.REPO_PORT
}

class WciPortSetting: PortSettingBase() {
    override fun getNameId(): Int = R.string.settings_weatherChannelInfoPortName
    override fun getBundleStatePortKey(): String = "WciPortSetting.State.port"
    override fun getPreferencesKey(): Int = AppPreferences.WCI_PORT
}

class ServerContractSetting: Setting<ServerContractSetting.State>() {
    data class State(@JvmField var contractType: Int)

    override fun getNameId(): Int {
        return R.string.settings_serverContract
    }

    override fun createView(context: Context): View {
        return AppCompatSpinner(context).apply {
            adapter = simpleArrayAdapter(context, R.array.serverContracts)

            setSelection(when(state.contractType) {
                RepoContractType.CONTRACT_RAW -> 0
                RepoContractType.CONTRACT_JSON -> 1
                else -> throw RuntimeException("Invalid state.contractType")
            })

            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
        prefs.setInt(AppPreferences.SERVER_CONTRACT, state.contractType)
    }

    override fun saveStateToBundle(outState: Bundle) {
        outState.putInt(BUNDLE_STATE_CONTRACT_TYPE, state.contractType)
    }

    override fun loadStateFromPrefs(prefs: Preferences) {
        state = State(prefs.getInt(AppPreferences.SERVER_CONTRACT))
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

class WeatherSendIntervalSetting: Setting<WeatherSendIntervalSetting.State>() {
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
        return R.string.settings_weatherSendInterval_name
    }

    override fun createView(context: Context): View {
        val res = context.resources
        val invalidNumberStr = res.getString(R.string.invalidNumberFormat)
        val lessOrZeroErrorStr = res.getString(R.string.settings_weatherSendInterval_lessOrEqZero)

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
        prefs.setInt(AppPreferences.WEATHER_SEND_INTERVAL, state.interval)
    }

    override fun saveStateToBundle(outState: Bundle) {
        outState.putInt(BUNDLE_STATE_INTERVAL, state.interval)
    }

    override fun loadStateFromPrefs(prefs: Preferences) {
        state = State(prefs.getInt(AppPreferences.WEATHER_SEND_INTERVAL))
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
        private const val BUNDLE_STATE_INTERVAL = "WeatherSendIntervalSetting.State.interval"
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