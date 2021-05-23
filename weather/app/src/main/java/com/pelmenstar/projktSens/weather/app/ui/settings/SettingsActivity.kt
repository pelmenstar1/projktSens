@file:Suppress("FunctionName", "NOTHING_TO_INLINE")
@file:SuppressLint("SetTextI18n")

package com.pelmenstar.projktSens.weather.app.ui.settings

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import androidx.core.content.res.ResourcesCompat
import com.pelmenstar.projktSens.shared.android.ui.*
import com.pelmenstar.projktSens.weather.app.Preferences
import com.pelmenstar.projktSens.weather.app.R

class SettingsActivity : HomeButtonSupportActivity() {
    private val settings: Array<Setting<*>> = arrayOf(
        TemperatureSetting(),
        PressureSetting()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val res = resources

        actionBar {
            title = res.getText(R.string.settings)
            setDisplayHomeAsUpEnabled(true)
        }

        val prefs = Preferences.of(this)
        settings.forEach { it.loadState(prefs) }

        content {
            val dp5 = (5 * res.displayMetrics.density).toInt()
            val context = this

            val caption = TextAppearance(context, R.style.TextAppearance_MaterialComponents_Caption)
            val body1 = TextAppearance(context, R.style.TextAppearance_MaterialComponents_Body1)

            FrameLayout(this) {
                LinearLayout {
                    frameLayoutParams(MATCH_PARENT, WRAP_CONTENT)

                    orientation = LinearLayout.VERTICAL

                    for(setting in settings) {
                        FrameLayout {
                            linearLayoutParams(MATCH_PARENT, WRAP_CONTENT)

                            TextView {
                                frameLayoutParams(WRAP_CONTENT, WRAP_CONTENT) {
                                    gravity = Gravity.CENTER_VERTICAL or Gravity.START
                                    leftMargin = dp5
                                }

                                applyTextAppearance(body1)
                                text = setting.getName(context) + ':'
                            }

                            addView(setting.createView(context).apply {
                                frameLayoutParams(WRAP_CONTENT, WRAP_CONTENT) {
                                    gravity = Gravity.CENTER_VERTICAL or Gravity.END
                                    rightMargin = dp5
                                }
                            })
                        }
                    }
                }

                Button {
                    frameLayoutParams(WRAP_CONTENT, WRAP_CONTENT) {
                        gravity = Gravity.BOTTOM or Gravity.END
                        rightMargin = dp5
                        bottomMargin = dp5
                    }
                    text = res.getText(R.string.save_settings)

                    setOnClickListener {
                        settings.forEach {
                            it.saveState(prefs)
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