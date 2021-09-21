package com.pelmenstar.projktSens.weather.app.ui

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.setPadding
import androidx.fragment.app.DialogFragment
import com.pelmenstar.projktSens.shared.android.RectDrawable
import com.pelmenstar.projktSens.shared.android.ext.obtainStyledAttributes
import com.pelmenstar.projktSens.shared.android.ui.*
import com.pelmenstar.projktSens.weather.app.PermissionUtils
import com.pelmenstar.projktSens.weather.app.R

@RequiresApi(23)
class RequestLocationPermissionDialog : DialogFragment() {
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.any { it.value }) {
            dismissWithLocationGranted()
        } else if (PermissionUtils.isNeverShowAgainOnLocation(this)) {
            setState(STATE_NEVER_SHOW_AGAIN)
        }
    }

    private lateinit var content: FrameLayout
    private val notGrantedContent: View by lazy { createNotGrantedContent() }
    private val neverShowAgainContent: View by lazy { createNeverShowAgainContent() }

    private lateinit var defTextAppearance: TextAppearance

    private var _isLocPermGranted = false
    val isLocationPermissionGranted: Boolean
        get() = _isLocPermGranted

    var onDismissCallback: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val context = requireContext()
        val theme = context.theme
        val res = context.resources

        return FrameLayout(context) {
            content = this

            val fillColor: Int
            theme.obtainStyledAttributes(R.style.AppTheme, android.R.attr.colorBackground) {
                fillColor = it.getColor(0, Color.BLACK)
            }

            val strokeColor = ResourcesCompat.getColor(res, R.color.colorPrimary, theme)

            background = RectDrawable.strokeAndFill(
                strokeColor, res.getDimension(R.dimen.requestLocPerm_roundRectStrokeWidth),
                fillColor
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val context = requireContext()

        defTextAppearance = TextAppearance(context, R.style.TextAppearance_MaterialComponents_Body1)
        if (!PermissionUtils.isLocationGranted(context)) {
            setState(STATE_NOT_GRANTED)
        } else {
            dismissWithLocationGranted()
        }
    }

    private fun setState(state: Int) {
        val c = content

        c.removeAllViews()

        when (state) {
            STATE_NOT_GRANTED -> {
                c.addView(notGrantedContent)
            }

            STATE_NEVER_SHOW_AGAIN -> {
                c.addView(neverShowAgainContent)
            }
        }
    }

    private fun createNotGrantedContent(): View {
        val res = resources

        return LinearLayout(requireContext()) {
            orientation = LinearLayout.VERTICAL
            setPadding(res.getDimensionPixelSize(R.dimen.requestLocPerm_contentPadding))

            TextView {
                linearLayoutParams(WRAP_CONTENT, WRAP_CONTENT) {
                    gravity = Gravity.CENTER_HORIZONTAL
                }

                defTextAppearance.apply(this)
                setText(R.string.gpsIsNotGranted)
            }

            Button(R.attr.materialButtonOutlinedStyle) {
                linearLayoutParams(WRAP_CONTENT, WRAP_CONTENT) {
                    gravity = Gravity.CENTER_HORIZONTAL
                    topMargin = res.getDimensionPixelSize(R.dimen.requestLocPerm_actionTopMargin)
                }

                setText(R.string.allowGps)
                setOnClickListener { requestGps() }
            }
        }
    }

    private fun createNeverShowAgainContent(): View {
        val res = resources

        return LinearLayout(requireContext()) {
            orientation = LinearLayout.VERTICAL
            setPadding(res.getDimensionPixelSize(R.dimen.requestLocPerm_contentPadding))

            TextView {
                linearLayoutParams(WRAP_CONTENT, WRAP_CONTENT) {
                    gravity = Gravity.CENTER_HORIZONTAL
                }

                defTextAppearance.apply(this)
                setText(R.string.locDependentActivity_requestLocPerm_gpsNeverShowAgain)
            }

            Button(R.attr.materialButtonOutlinedStyle) {
                linearLayoutParams(WRAP_CONTENT, WRAP_CONTENT) {
                    gravity = Gravity.CENTER_HORIZONTAL
                    topMargin = res.getDimensionPixelSize(R.dimen.requestLocPerm_actionTopMargin)
                }

                setText(R.string.goToSettings)
                setOnClickListener { goToSettingPermissions() }
            }
        }
    }

    private fun requestGps() {
        permissionLauncher.launch(PermissionUtils.LOCATION_PERMISSIONS)
    }

    private fun goToSettingPermissions() {
        val appUri = Uri.fromParts("package", requireContext().packageName, null)
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, appUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        startActivity(intent)
    }

    private fun dismissWithLocationGranted() {
        _isLocPermGranted = true
        dismiss()
    }

    override fun onResume() {
        super.onResume()

        // In the case of 'Never show again', an user should go to the settings to manually enable location
        // permission.
        // So after the user returns, we need to check whether the user actually changes access to location
        if (PermissionUtils.isLocationGranted(requireContext())) {
            dismissWithLocationGranted()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        onDismissCallback?.invoke()
    }

    companion object {
        private const val STATE_NOT_GRANTED = 0
        private const val STATE_NEVER_SHOW_AGAIN = 1
    }
}