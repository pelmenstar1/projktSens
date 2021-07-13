package com.pelmenstar.projktSens.weather.app.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.pelmenstar.projktSens.shared.android.ui.requestPermissions.RequestPermissionsActivity
import com.pelmenstar.projktSens.shared.android.ui.requestPermissions.RequestPermissionsContext
import com.pelmenstar.projktSens.weather.app.AppPreferences
import com.pelmenstar.projktSens.weather.app.R
import com.pelmenstar.projktSens.weather.app.Startup
import com.pelmenstar.projktSens.weather.app.di.AppModule
import com.pelmenstar.projktSens.weather.app.di.DaggerAppComponent
import com.pelmenstar.projktSens.weather.app.ui.home.HomeActivity

class StartupActivity : Activity() {
    private lateinit var prefs: AppPreferences

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val component = DaggerAppComponent.builder().appModule(AppModule(this)).build()
        prefs = component.preferences()

        val permContext = RequestPermissionsContext {
            if(!prefs.isGpsPermissionDenied) {
                permission(
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
            startHomeActivityAndFinish()
        }
        setContentView(View(this))
    }

    private fun startHomeActivityAndFinish() {
        val appContext = applicationContext
        Startup.init(appContext)

        val intent = Intent(appContext, HomeActivity::class.java)
        startActivityForResult(intent, 0, null)
        finish()
        overridePendingTransition(0, 0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(data != null && !prefs.isGpsPermissionDenied) {
            val deniedPermissions = data.getIntArrayExtra(RequestPermissionsActivity.RETURN_DATA_DENIED_PERMISSION_INDICES)
            if(deniedPermissions != null && deniedPermissions.contains(0)) {
                prefs.isGpsPermissionDenied = true
            }
        }

        startHomeActivityAndFinish()
    }
}