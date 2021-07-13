package com.pelmenstar.projktSens.weather.app.ui

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.view.setPadding
import androidx.fragment.app.DialogFragment
import com.pelmenstar.projktSens.shared.android.ui.*
import com.pelmenstar.projktSens.weather.app.PermissionUtils
import com.pelmenstar.projktSens.weather.app.R

@RequiresApi(23)
class RequestLocationPermissionDialog: DialogFragment() {
    private lateinit var content: LinearLayout
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if(result.any { it.value }) {
            dismissWithLocationGranted()
        } else if(PermissionUtils.isNeverShowAgainOnLocation(this)) {
            setState(STATE_NEVER_SHOW_AGAIN)
        }
    }

    private var notGrantedContent: View? = null
    private var neverShowAgainContent: View? = null

    private lateinit var defTextAppearance: TextAppearance

    var isLocationPermissionGranted: Boolean = false
    var onDismissCallback: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return LinearLayout(requireContext()) {
            content = this
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val context = requireContext()

        defTextAppearance = TextAppearance(context, R.style.TextAppearance_MaterialComponents_Body1)
        if(!PermissionUtils.isLocationGranted(context)) {
            setState(STATE_NOT_GRANTED)
        } else {
            dismissWithLocationGranted()
        }
    }

    private fun setState(state: Int) {
        content.removeAllViews()

        when(state) {
            STATE_NOT_GRANTED -> {
                if(notGrantedContent == null) {
                    notGrantedContent = createNotGrantedContent()
                }

                content.addView(notGrantedContent)
            }

            STATE_NEVER_SHOW_AGAIN -> {
                if(neverShowAgainContent == null) {
                    neverShowAgainContent = createNeverShowAgainContent()
                }

                content.addView(neverShowAgainContent)
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

                setOnClickListener {
                    requestGps()
                }
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

                setOnClickListener {
                    goToSettingPermissions()
                }
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
        isLocationPermissionGranted = true
        dismiss()
    }

    override fun onResume() {
        super.onResume()

        // In the case of 'Never show again', an user should go to the settings to manually enable location
        // permission.
        // So after the user returns, we need to check whether the user actually changes access to location
        if(PermissionUtils.isLocationGranted(requireContext())) {
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