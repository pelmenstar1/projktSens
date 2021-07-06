package com.pelmenstar.projktSens.jserver

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.pelmenstar.projktSens.shared.android.ui.*
import com.pelmenstar.projktSens.shared.android.ui.settings.SettingsActivity
import com.pelmenstar.projktSens.shared.android.ui.settings.SettingsContext

@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {
    private lateinit var startButton: Button
    private lateinit var stopButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val controller = Controller(MainConfig(this))

        content {
            LinearLayout(this) {
                orientation = LinearLayout.VERTICAL

                Button {
                    startButton = this
                    text = "Start"

                    setOnClickListener {
                        isEnabled = false
                        stopButton.isEnabled = true

                        controller.startAll()
                    }
                }

                Button {
                    stopButton = this
                    text = "Stop"
                    isEnabled = false

                    setOnClickListener {
                        isEnabled = false
                        startButton.isEnabled = true

                        controller.stopAll()
                    }
                }

                if(BuildConfig.DEBUG) {
                    Button {
                        text = "Clear db"

                        setOnClickListener { controller.clearRepository() }
                    }

                    Button {
                        text = "Gen db"

                        setOnClickListener { controller.debugGenDb() }
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val settingsContext = SettingsContext(*SETTINGS)
        val context = this

        menu.add {
            item(
                titleRes = R.string.main_menu_settings,
                showsAsAction = MenuItem.SHOW_AS_ACTION_IF_ROOM,
                iconRes = R.drawable.ic_settings) {
                val intent = SettingsActivity.intent(context, settingsContext, AppPreferences::class.java)
                context.startActivity(intent)
                true
            }
        }
        return true
    }
}