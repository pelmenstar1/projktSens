package com.pelmenstar.projktSens.weather.app.ui.firstStart

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.GridLayout
import androidx.core.widget.addTextChangedListener
import com.pelmenstar.projktSens.shared.InetAddressUtils
import com.pelmenstar.projktSens.shared.android.Preferences
import com.pelmenstar.projktSens.shared.android.ui.*
import com.pelmenstar.projktSens.shared.equalsPattern
import com.pelmenstar.projktSens.weather.app.AppPreferences
import com.pelmenstar.projktSens.weather.app.R

class ChooseAddressAndPortScreen: FirstStartScreen<ChooseAddressAndPortScreen.State>() {
    class State: IncompleteState {
        private var _hostString: String = ""
        var hostString: String
            get() = _hostString
            set(value) {
                _hostString = value

                val ipInt = InetAddressUtils.parseNumericalIpv4ToInt(value)
                if(ipInt != InetAddressUtils.IP_ERROR) {
                    _isHostValid = true
                    _hostInt = ipInt

                    updateValidity()
                } else {
                    _isHostValid = false
                    isValid = false
                }
            }

        private var _hostInt: Int = InetAddressUtils.IP_ERROR
        var hostInt: Int
            get() = _hostInt
            set(value) {
                _hostInt = value
                _hostString = InetAddressUtils.intIpv4ToString(value)
                _isHostValid = value != InetAddressUtils.IP_ERROR
                updateValidity()
            }

        private var _isHostValid = true
        val isHostValid: Boolean
            get() = _isHostValid

        var port: Int
            set(value) {
                field = value
                _isPortValid = InetAddressUtils.isValidFreePort(value)
                updateValidity()
            }

        private var _isPortValid = true
        val isPortValid: Boolean
            get() = _isPortValid

        constructor(hostStr: String, port: Int) {
            hostString = hostStr
            this.port = port
        }

        constructor(hostInt: Int, port: Int) {
            this.hostInt = hostInt
            this.port = port
        }

        private fun updateValidity() {
            isValid = _isHostValid && _isPortValid
        }

        override fun equals(other: Any?): Boolean {
            return equalsPattern(other) { o ->
                _hostString == o._hostString && port == o.port
            }
        }

        override fun hashCode(): Int {
            var result = _hostString.hashCode()
            result = result * 31 + port

            return result
        }
    }

    override fun getTitleId(): Int = R.string.firstStart_chooseAddressAndPortTitle

    override fun createView(context: Context): View {
        val res = context.resources
        val invalidAddressStr = res.getText(R.string.invalidInetAddress)
        val portReservedErrorLess = res.getText(R.string.portReservedError_less)
        val portReservedErrorGreater = res.getText(R.string.portReservedError_greater)
        val invalidPortNumberStr = res.getText(R.string.invalidNumberFormat)

        val body1 = TextAppearance(context, R.style.TextAppearance_MaterialComponents_Body1)

        return GridLayout(context).apply {
            columnCount = 2
            rowCount = 2

            val nameColumnSpec = GridLayout.spec(0, GridLayout.START)
            val viewColumnSpec = GridLayout.spec(1, GridLayout.END)

            val firstRowSpec = GridLayout.spec(0)
            val secondRowSpec = GridLayout.spec(1)

            TextView {
                gridLayoutParams(firstRowSpec, nameColumnSpec) {
                    gravity = Gravity.CENTER_VERTICAL
                }

                text = res.getText(R.string.serverHost)
                applyTextAppearance(body1)
            }

            EditText {
                gridLayoutParams(firstRowSpec, viewColumnSpec)

                inputType = IP_ADDRESS_INPUT_TYPE
                setText(state.hostString)
                if(!state.isHostValid) {
                    error = invalidAddressStr
                }

                addTextChangedListener {
                    if(it != null) {
                        val state = state
                        val text = it.toString()
                        state.hostString = text
                        if (!state.isHostValid) {
                            error = invalidAddressStr
                        }
                    }
                }
            }

            TextView {
                gridLayoutParams(secondRowSpec, nameColumnSpec) {
                    gravity = Gravity.CENTER_VERTICAL
                }

                text = res.getText(R.string.port)
                applyTextAppearance(body1)
            }

            EditText {
                gridLayoutParams(secondRowSpec, viewColumnSpec)

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
    }

    override fun loadDefaultState() {
        state = State(DEFAULT_HOST_INT, DEFAULT_PORT)
    }

    override fun loadStateFromBundle(bundle: Bundle): Boolean {
        val hostStr = bundle.getString(STATE_HOST_STRING)
        if(hostStr != null) {
            val port = bundle.getInt(STATE_PORT, -1)
            if(port != -1) {
                state = State(hostStr, port)
                return true
            }
        }
        return false
    }

    override fun saveStateToPrefs(prefs: Preferences) {
        prefs.setInt(AppPreferences.SERVER_HOST_INT, state.hostInt)
        prefs.setInt(AppPreferences.SERVER_PORT, state.port)
    }

    override fun saveStateToBundle(outState: Bundle) {
        outState.putString(STATE_HOST_STRING, state.hostString)
        outState.putInt(STATE_PORT, state.port)
    }

    companion object {
        private const val STATE_HOST_STRING = "ChooseAddressAndPortScreen.state.hostString"
        private const val STATE_PORT = "ChooseAddressAndPortScreen.state.port"

        private val DEFAULT_HOST_INT = InetAddressUtils.ip(192, 168, 0, 0)
        private const val DEFAULT_PORT = 10001

        private const val IP_ADDRESS_INPUT_TYPE =
            (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS) and
                    (InputType.TYPE_TEXT_FLAG_AUTO_CORRECT or InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE).inv()
    }
}