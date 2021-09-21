package com.pelmenstar.projktSens.weather.app.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.pelmenstar.projktSens.weather.app.di.AppModule
import com.pelmenstar.projktSens.weather.app.di.DaggerAppComponent
import com.pelmenstar.projktSens.weather.app.ui.firstStart.FirstStartActivity
import com.pelmenstar.projktSens.weather.app.ui.home.HomeActivity

class StartupActivity : Activity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val component = DaggerAppComponent.builder().appModule(AppModule(this)).build()
        val prefs = component.preferences()

        if (prefs.isFirstStart) {
            val intent = FirstStartActivity.intent(this)
            startActivityForResult(intent, REQUEST_CODE_FIRST_START)
        } else {
            startHomeActivityAndFinish()
        }

        setContentView(View(this))
    }

    private fun startHomeActivityAndFinish() {
        val intent = Intent(this, HomeActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when(requestCode) {
            REQUEST_CODE_FIRST_START -> {
                if (resultCode == RESULT_OK) {
                    startHomeActivityAndFinish()
                } else {
                    finish()
                }
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_FIRST_START = 1
    }
}