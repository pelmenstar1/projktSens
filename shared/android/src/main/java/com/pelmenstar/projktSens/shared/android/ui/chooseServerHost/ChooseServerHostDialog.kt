package com.pelmenstar.projktSens.shared.android.ui.chooseServerHost

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import com.pelmenstar.projktSens.serverProtocol.Contract
import com.pelmenstar.projktSens.serverProtocol.ContractType
import com.pelmenstar.projktSens.serverProtocol.ProjktSensServerChecker
import com.pelmenstar.projktSens.shared.InetAddressUtils
import com.pelmenstar.projktSens.shared.StringUtils
import com.pelmenstar.projktSens.shared.android.R
import com.pelmenstar.projktSens.shared.android.ext.Message
import com.pelmenstar.projktSens.shared.android.ui.*
import kotlinx.coroutines.*
import java.net.InetSocketAddress

class ChooseServerHostDialog: DialogFragment() {
    private lateinit var addressInput: AppCompatEditText
    private lateinit var portInput: AppCompatEditText
    private lateinit var resultView: TextView

    private var isCancellableInArgs: Boolean = true

    private var addressText: String = DEFAULT_ADDRESS_TEXT
    private var portText: String = DEFAULT_PORT_TEXT

    private var _address: Int = DEFAULT_ADDRESS
    private var _port: Int = DEFAULT_PORT

    private var isAddressValid = true
    private var isPortValid = true

    private var contract: Contract? = null

    private var checkJob: Job? = null

    private val mainThreadHandler = MainThreadHandler()

    var onChosen: ((address: Int, port: Int) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.choose_server_host_dialog, null)

        val builder = AlertDialog.Builder(context)
        builder.setPositiveButton(R.string.ok) { _, _ ->
            dismiss()
        }

        builder.setView(view)

        addressInput = view.findViewById(R.id.chooseServerHost_addressInput)
        portInput = view.findViewById(R.id.chooseServerHost_portInput)
        resultView = view.findViewById(R.id.chooseServerHost_result)

        if (savedInstanceState != null) {
            val savedAddress = savedInstanceState.getString(STATE_ADDRESS_TEXT)
            if (savedAddress != null) {
                setAddressText(savedAddress)
            }

            val savedPort = savedInstanceState.getString(STATE_PORT_TEXT)
            if (savedPort != null) {
                setPortText(savedPort)
            }
        } else {
            val args = arguments
            if (args != null) {
                val givenAddress = args.get(ARGS_ADDRESS) as Int?
                if (givenAddress != null) {
                    _address = givenAddress
                    addressText = InetAddressUtils.intIpv4ToString(_address)

                    addressInput.setText(addressText)
                }

                val givenPort = args.get(ARGS_PORT) as Int?
                if (givenPort != null) {
                    _port = givenPort
                    portText = _port.toString()

                    portInput.setText(portText)
                }

                val contractId = args.get(ARGS_CONTRACT_ID) as Int?

                if (contractId != null) {
                    contract = ContractType.toObject(contractId)
                }

                isCancellableInArgs = args.getBoolean(ARGS_CANCELLABLE, true)

                if (givenAddress != null || givenPort != null) {
                    checkIp()
                }
            }
        }

        addressInput.addTextChangedListener {
            if (it != null) setAddressText(it.toString())
        }

        portInput.addTextChangedListener {
            if (it != null) setPortText(it.toString())
        }

        return builder.create()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(STATE_ADDRESS_TEXT, addressText)
        outState.putString(STATE_PORT_TEXT, portText)
    }

    private fun setAddressText(value: String) {
        addressText = value

        val result = InetAddressUtils.parseNumericalIpv4ToInt(value)
        if(result != InetAddressUtils.IP_ERROR) {
            setAddressValidity(true)
            _address = result

            postCheckIpDelayed()
        } else {
            setAddressValidity(false)
        }
    }

    private fun setPortText(value: String) {
        portText = value

        val port = StringUtils.parsePositiveInt(value)
        if(!InetAddressUtils.isValidFreePort(port)) {
            setPortValidity(false)
            return
        }

        _port = port
        postCheckIpDelayed()
        setPortValidity(true)
    }

    private fun setAddressValidity(value: Boolean) {
        if(value) {
            addressInput.error = null
            isAddressValid = true
        } else {
            addressInput.error = getString(R.string.invalid_ip_address)
            isAddressValid = false
        }

        updateTotalValidity()
    }

    private fun setPortValidity(value: Boolean) {
        if(value) {
            portInput.error = null
            isPortValid = true
        } else {
            portInput.error = getString(R.string.invalid_server_port)
            isPortValid = false
        }

        updateTotalValidity()
    }

    private fun updateTotalValidity() {
        setTotalValidity(isAddressValid && isPortValid)
    }

    private fun setTotalValidity(value: Boolean) {
        isCancelable = isCancellableInArgs && value
        (dialog as AlertDialog?)?.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = value
    }

    private fun setStatus(status: Int) {
        val context = requireContext()
        val res = context.resources
        val resultView = resultView
        val theme = context.theme

        val validColor = ResourcesCompat.getColor(res, R.color.chooseServerHostDialog_validEndpointColor, theme)
        val invalidColor = ResourcesCompat.getColor(res, R.color.chooseServerHostDialog_invalidEndpointColor, theme)

        when(status) {
            STATUS_LOADING -> {
                resultView.text = null

                setTotalValidity(false)
            }
            STATUS_UNAVAILABLE -> {
                resultView.text = getString(R.string.unreachable_endpoint)
                resultView.setTextColor(invalidColor)

                setTotalValidity(false)
            }
            STATUS_NOT_PROJKT_SENS_SERVER -> {
                resultView.text = getString(R.string.not_projkt_sens_server)
                resultView.setTextColor(invalidColor)

                setTotalValidity(false)
            }
            STATUS_IS_PROJKT_SENS_SERVER -> {
                resultView.text = getString(R.string.is_projkt_sens_server)
                resultView.setTextColor(validColor)

                setTotalValidity(true)
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        mainThreadHandler.removeCallbacksAndMessages(null)
        checkJob?.cancel()

        onChosen?.invoke(_address, _port)
    }

    private fun postSetStatus(status: Int) {
        mainThreadHandler.sendMessage(Message {
            what = MSG_SET_STATUS
            obj = this@ChooseServerHostDialog
            arg1 = status
        })
    }

    private fun postCheckIpDelayed() {
        if(!(isAddressValid && isPortValid)) {
            return
        }

        setTotalValidity(false)

        mainThreadHandler.removeMessages(MSG_CHECK_IP)
        mainThreadHandler.sendMessageDelayed(Message {
            what = MSG_CHECK_IP
            obj = this@ChooseServerHostDialog
        }, CHECK_IP_DELAY)
    }

    private fun checkIp() {
        val contract = contract ?: throw RuntimeException("Contract is null")

        checkJob?.cancel()
        checkJob = scope.launch {
            postSetStatus(STATUS_LOADING)

            val socketAddress = InetSocketAddress(InetAddressUtils.parseInt(_address), _port)

            val newStatus = ProjktSensServerChecker.isProjktSensServer(contract, socketAddress)

            postSetStatus(when(newStatus) {
                ProjktSensServerChecker.Status.UNAVAILABLE -> STATUS_UNAVAILABLE
                ProjktSensServerChecker.Status.NOT_PROJKT_SENS_SERVER -> STATUS_NOT_PROJKT_SENS_SERVER
                ProjktSensServerChecker.Status.IS_PROJKT_SENS_SERVER -> STATUS_IS_PROJKT_SENS_SERVER
            })
        }
    }

    class MainThreadHandler: Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            val dialog = msg.obj as ChooseServerHostDialog

            when(msg.what) {
                MSG_SET_STATUS -> dialog.setStatus(msg.arg1)
                MSG_CHECK_IP -> dialog.checkIp()
            }
        }
    }

    companion object {
        private val scope = CoroutineScope(Dispatchers.IO + CoroutineName("ChooseSeverIpDialog"))

        private const val DEFAULT_ADDRESS_TEXT = "192.168.0.0"
        private const val DEFAULT_PORT_TEXT = "10001"

        private val DEFAULT_ADDRESS = InetAddressUtils.ip(192, 168, 0, 0)
        private const val DEFAULT_PORT = 10001

        private const val CHECK_IP_DELAY: Long = 200

        private const val STATUS_LOADING = 0
        private const val STATUS_UNAVAILABLE = 1
        private const val STATUS_NOT_PROJKT_SENS_SERVER = 2
        private const val STATUS_IS_PROJKT_SENS_SERVER = 3

        private const val MSG_SET_STATUS = 0
        private const val MSG_CHECK_IP = 1

        private const val STATE_ADDRESS_TEXT = "ChooseServerHostDialog:state:addressText"
        private const val STATE_PORT_TEXT = "ChooseServerHostDialog:state:portText"

        private const val ARGS_ADDRESS = "ChooseServerHostDialog:args:address"
        private const val ARGS_PORT = "ChooseServerHostDialog:args:port"
        private const val ARGS_CONTRACT_ID = "ChooseServerHostDialog:args:contractId"
        private const val ARGS_CANCELLABLE = "ChooseServerHostDialog:args:cancellable"

        fun arguments(address: Int, port: Int, contractId: Int, isCancellable: Boolean = true): Bundle {
            return Bundle(3).apply {
                putInt(ARGS_ADDRESS, address)
                putInt(ARGS_PORT, port)
                putInt(ARGS_CONTRACT_ID, contractId)
                putBoolean(ARGS_CANCELLABLE, isCancellable)
            }
        }
    }
}