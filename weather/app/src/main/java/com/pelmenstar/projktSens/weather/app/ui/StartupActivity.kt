package com.pelmenstar.projktSens.weather.app.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import com.pelmenstar.projktSens.shared.android.ui.requestPermissions.PackedPermissionState
import com.pelmenstar.projktSens.shared.android.ui.requestPermissions.RequestPermissionsActivity
import com.pelmenstar.projktSens.shared.android.ui.requestPermissions.RequestPermissionsContext
import com.pelmenstar.projktSens.weather.app.AppPreferences
import com.pelmenstar.projktSens.weather.app.R
import com.pelmenstar.projktSens.weather.app.di.AppModule
import com.pelmenstar.projktSens.weather.app.di.DaggerAppComponent
import com.pelmenstar.projktSens.weather.app.ui.firstStart.FirstStartActivity
import com.pelmenstar.projktSens.weather.app.ui.home.HomeActivity

class StartupActivity : Activity() {
    private lateinit var prefs: AppPreferences

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val component = DaggerAppComponent.builder().appModule(AppModule(this)).build()
        prefs = component.preferences()

        val permContext = RequestPermissionsContext {
            if (!prefs.isGpsPermissionDenied) {
                permission(
                    id = PERMISSION_ID_LOCATION,
                    userDescriptionId = R.string.permissionGps_userDescription,
                    whyTextId = R.string.permissionGps_whyText
                ) {
                    anyOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                }
            }
        }

        if (RequestPermissionsActivity.shouldStartActivity(this, permContext)) {
            val intent = RequestPermissionsActivity.intent(this, permContext)

            startActivityForResult(intent, 0)
            overridePendingTransition(0, 0)
        } else {
            onPermissionsRequested()
        }
        setContentView(View(this))
    }

    private fun onPermissionsRequested() {
        if (prefs.isFirstStart) {
            val intent = FirstStartActivity.intent(this)
            startActivityForResult(intent, REQUEST_CODE_FIRST_START, null)
        } else {
            startHomeActivityAndFinish()
        }
    }

    private fun startHomeActivityAndFinish() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivityForResult(intent, REQUEST_CODE_PERMISSIONS, null)
        finish()
        overridePendingTransition(0, 0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (data != null && !prefs.isGpsPermissionDenied) {
                val packedPermissionStates =
                    data.getLongArrayExtra(RequestPermissionsActivity.RETURN_DATA_PERMISSION_STATES)

                if(packedPermissionStates != null) {
                    for(packedState in packedPermissionStates) {
                        if(PackedPermissionState.getId(packedState) == PERMISSION_ID_LOCATION) {
                            val state = PackedPermissionState.getState(packedState)
                            if(state == PackageManager.PERMISSION_DENIED) {
                                prefs.isGpsPermissionDenied = true
                            }
                        }
                    }
                }
            }

            onPermissionsRequested()
        } else if (requestCode == REQUEST_CODE_FIRST_START) {
            if (resultCode == RESULT_OK) {
                startHomeActivityAndFinish()
            } else {
                finish()
            }
        }
    }

    companion object {
        private const val PERMISSION_ID_LOCATION = 0

        private const val REQUEST_CODE_PERMISSIONS = 0
        private const val REQUEST_CODE_FIRST_START = 1
    }
}