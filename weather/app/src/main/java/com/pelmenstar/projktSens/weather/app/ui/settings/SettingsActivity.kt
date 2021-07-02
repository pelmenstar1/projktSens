@file:Suppress("FunctionName", "NOTHING_TO_INLINE")
@file:SuppressLint("SetTextI18n")

package com.pelmenstar.projktSens.weather.app.ui.settings

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import androidx.core.content.res.ResourcesCompat
import com.pelmenstar.projktSens.shared.android.ui.*
import com.pelmenstar.projktSens.weather.app.Preferences
import com.pelmenstar.projktSens.weather.app.R

class SettingsActivity : HomeButtonSupportActivity() {
    private val settings: Array<Setting<*>> = arrayOf(
        TemperatureSetting(),
        PressureSetting(),
        ServerHostSetting(),
        ServerContractSetting(),
        RepoPortSetting(),
        WciPortSetting(),
        WeatherReceiveIntervalSetting()
    )

    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val res = resources

        actionBar {
            title = res.getText(R.string.settings)
            setDisplayHomeAsUpEnabled(true)
        }

        val prefs = Preferences.of(this)

        settings.forEach {
            if(savedInstanceState != null) {
                val success = it.loadStateFromBundle(savedInstanceState)
                if(!success) {
                    it.loadStateFromPrefs(prefs)
                }
            } else {
                it.loadStateFromPrefs(prefs)
            }
        }

        setContentView(createContent())

        val onValidChangedListener = Setting.IncompleteState.OnValidChanged { isValid ->
            saveButton.isEnabled = isValid
        }

        settings.forEach {
            // after state is loaded
            val state = it.state

            if(state is Setting.IncompleteState) {
                state.onValidChanged = onValidChangedListener
                // isValid can already be false, so update saveButton
                if(!state.isValid) {
                    saveButton.isEnabled = false
                }
            }
        }
    }

    private fun createContent(): View {
        val prefs = Preferences.of(this)

        val res = resources
        val dp5 = (5 * res.displayMetrics.density).toInt()
        val context = this

        val caption = TextAppearance(context, R.style.TextAppearance_MaterialComponents_Caption)
        val body1 = TextAppearance(context, R.style.TextAppearance_MaterialComponents_Body1)

        return FrameLayout(this) {
            GridLayout {
                frameLayoutParams(MATCH_PARENT, MATCH_PARENT)

                columnCount = 2
                rowCount = settings.size

                val settingNameSpec = android.widget.GridLayout.spec(0, android.widget.GridLayout.START)
                val viewSpec = android.widget.GridLayout.spec(1, android.widget.GridLayout.END)

                for(i in settings.indices) {
                    val setting = settings[i]

                    val rowSpec =  android.widget.GridLayout.spec(i)
                    TextView {
                        gridLayoutParams(
                            rowSpec,
                            columnSpec = settingNameSpec
                        ) {
                            leftMargin = dp5
                        }

                        applyTextAppearance(body1)
                        val name = res.getString(setting.getNameId())
                        text = "$name:"
                    }

                    addView(setting.createView(context).apply {
                        gridLayoutParams(
                            rowSpec,
                            columnSpec = viewSpec
                        ) {
                            rightMargin = dp5
                        }
                    })
                }
            }

            FrameLayout {
                Button {
                    frameLayoutParams(WRAP_CONTENT, WRAP_CONTENT) {
                        gravity = Gravity.BOTTOM or Gravity.END
                        rightMargin = dp5
                        bottomMargin = dp5
                    }
                    saveButton = this
                    text = res.getText(R.string.save_settings)

                    setOnClickListener {
                        settings.forEach {
                            it.saveStateToPrefs(prefs)
                        }
                        finish()
                    }
                }

                TextView {
                    frameLayoutParams(WRAP_CONTENT, WRAP_CONTENT) {
                        gravity = Gravity.BOTTOM or Gravity.START
                        leftMargin = dp5
                        bottomMargin = dp5
                    }

                    applyTextAppearance(caption)
                    typeface = loadNotosansFont()
                    text = res.getText(R.string.offCompanyName)
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        settings.forEach {
            it.saveStateToBundle(outState)
        }
    }

    private fun loadNotosansFont(): Typeface {
        val notosans = ResourcesCompat.getFont(this, R.font.notosans_medium)

        return notosans ?: Typeface.SERIF
    }

    companion object {
        @JvmStatic
        fun intent(context: Context): Intent {
            return Intent(context, SettingsActivity::class.java)
        }
    }
}