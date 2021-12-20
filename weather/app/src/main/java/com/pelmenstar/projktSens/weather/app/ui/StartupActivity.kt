package com.pelmenstar.projktSens.weather.app.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.pelmenstar.projktSens.shared.android.ui.chooseServerHost.ChooseServerHostDialog
import com.pelmenstar.projktSens.weather.app.AppPreferences
import com.pelmenstar.projktSens.weather.app.R
import com.pelmenstar.projktSens.weather.app.di.AppModule
import com.pelmenstar.projktSens.weather.app.di.DaggerAppComponent
import com.pelmenstar.projktSens.weather.app.ui.home.HomeActivity

class StartupActivity : AppCompatActivity() {
    private lateinit var prefs: AppPreferences

    public override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)

        super.onCreate(savedInstanceState)

        val component = DaggerAppComponent.builder().appModule(AppModule(this)).build()
        prefs = component.preferences()

        if (prefs.isFirstStart) {
            startChooseHostDialog()
        } else {
            startHomeActivityAndFinish()
        }

        setContentView(View(this))
    }

    private fun startChooseHostDialog() {
        ChooseServerHostDialog().also {
            it.arguments = ChooseServerHostDialog.arguments(
                0,
                10001,
                prefs.contractType,
                isCancellable = false
            )
            it.onChosen = { address, port ->
                prefs.run {
                    isFirstStart = false
                    serverHostInt = address
                    serverPort = port
                }

                startHomeActivityAndFinish()
            }

            it.show(supportFragmentManager, null)
        }
    }

    private fun startHomeActivityAndFinish() {
        val intent = Intent(this, HomeActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        startActivity(intent)
        overridePendingTransition(0, 0)
    }
}