package com.pelmenstar.projktSens.weather.app.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.pelmenstar.projktSens.shared.android.ui.requestPermissions.RequestPermissionsActivity
import com.pelmenstar.projktSens.shared.android.ui.requestPermissions.RequestPermissionsContext
import com.pelmenstar.projktSens.weather.app.R
import com.pelmenstar.projktSens.weather.app.Startup
import com.pelmenstar.projktSens.weather.app.ui.home.HomeActivity

class StartupActivity : Activity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val res = resources

        val permContext = RequestPermissionsContext {
            permission(userDescription =  res.getString(R.string.needToRequestGps)) {
                anyOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
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
        startHomeActivityAndFinish()
    }
}