package com.pelmenstar.projktSens.shared.android.ui.chooseServerHost

import android.content.Context
import android.util.AttributeSet
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import com.pelmenstar.projktSens.shared.InetAddressUtils
import com.pelmenstar.projktSens.shared.android.R
import com.pelmenstar.projktSens.shared.android.ui.Button
import com.pelmenstar.projktSens.shared.android.ui.TextView
import com.pelmenstar.projktSens.shared.android.ui.WRAP_CONTENT
import com.pelmenstar.projktSens.shared.android.ui.linearLayoutParams

class ChooseServerHostView @JvmOverloads constructor (
    context: Context,
    attrs: AttributeSet? = null,
): MaterialTextView(context, attrs, android.R.attr.textViewStyle, 0) {
    var address: Int = DEFAULT_ADDRESS
        private set

    var port: Int = DEFAULT_PORT
        private set

    var contractId: Int = -1

    var onHostChangedByUser: ((address: Int, port: Int) -> Unit)? = null

    init {
        linearLayoutParams(WRAP_CONTENT, WRAP_CONTENT)

        val dp = resources.displayMetrics.density
        val vOffset = (10 * dp).toInt()
        setPadding(0, vOffset, 0, vOffset)

        setOnClickListener {
            startChangeHostDialog()
        }
    }

    fun setHost(address: Int, port: Int) {
        this.address = address
        this.port = port

        refreshTextView()
    }

    private fun startChangeHostDialog() {
        val context = context
        ChooseServerHostDialog().also {
            it.arguments = ChooseServerHostDialog.arguments(address, port, contractId)
            it.onChosen = { address, port ->
                setHost(address, port)
                onHostChangedByUser?.invoke(address, port)
            }

            it.show((context as FragmentActivity).supportFragmentManager, null)
        }
    }

    private fun refreshTextView() {
        this.text = buildString(21 /* max possible length */) {
            InetAddressUtils.appendIntIpv4(address, this)
            append(':')
            append(port)
        }
    }

    companion object {
        private val DEFAULT_ADDRESS = InetAddressUtils.ip(127, 0, 0, 1)
        private const val DEFAULT_PORT = 1000
    }
}