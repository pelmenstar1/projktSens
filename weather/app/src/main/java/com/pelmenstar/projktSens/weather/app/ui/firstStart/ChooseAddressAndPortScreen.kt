package com.pelmenstar.projktSens.weather.app.ui.firstStart

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.GridLayout
import androidx.core.widget.addTextChangedListener
import com.pelmenstar.projktSens.shared.EmptyArray
import com.pelmenstar.projktSens.shared.InetAddressUtils
import com.pelmenstar.projktSens.shared.StringUtils
import com.pelmenstar.projktSens.shared.android.Preferences
import com.pelmenstar.projktSens.shared.android.ui.*
import com.pelmenstar.projktSens.shared.equalsPattern
import com.pelmenstar.projktSens.weather.app.AppPreferences
import com.pelmenstar.projktSens.weather.app.R

class ChooseAddressAndPortScreen : FirstStartScreen<ChooseAddressAndPortScreen.State>() {
    class State : IncompleteState {
        private var _hostBuffer = EmptyArray.CHAR
        var hostBuffer: CharArray
            get() = _hostBuffer
            set(value) {
                _hostBuffer = value

                val ipInt = InetAddressUtils.parseNumericalIpv4ToInt(value)
                if (ipInt != InetAddressUtils.IP_ERROR) {
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
                _hostBuffer = InetAddressUtils.intIpv4ToCharArray(_hostBuffer, value)
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

        constructor(hostBuffer: CharArray, port: Int) {
            this.hostBuffer = hostBuffer
            this.port = port
        }

        constructor(hostInt: Int, port: Int) {
            this.hostInt = hostInt
            this.port = port
        }

        internal fun setHost(text: Editable) {
            val textLength = text.length
            val buffer = if (_hostBuffer.size == textLength) {
                _hostBuffer
            } else {
                CharArray(textLength)
            }
            text.getChars(0, textLength, buffer, 0)

            hostBuffer = buffer
        }

        private fun updateValidity() {
            isValid = _isHostValid && _isPortValid
        }

        override fun equals(other: Any?): Boolean {
            return equalsPattern(other) { o ->
                _hostBuffer.contentEquals(o._hostBuffer) && port == o.port
            }
        }

        override fun hashCode(): Int {
            var result = _hostBuffer.contentHashCode()
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
                val initialHost = state.hostBuffer
                setText(initialHost, 0, initialHost.size)

                if (!state.isHostValid) {
                    error = invalidAddressStr
                }

                addTextChangedListener { text ->
                    if (text != null) {
                        val state = state
                        state.setHost(text)
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
    }

    override fun loadDefaultState() {
        state = State(DEFAULT_HOST_INT, DEFAULT_PORT)
    }

    override fun loadStateFromBundle(bundle: Bundle): Boolean {
        val hostBuffer = bundle.getCharArray(STATE_HOST_STRING)
        if (hostBuffer != null) {
            val port = bundle.get(STATE_PORT)
            if (port != null) {
                state = State(hostBuffer, port as Int)
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
        outState.putCharArray(STATE_HOST_STRING, state.hostBuffer)
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