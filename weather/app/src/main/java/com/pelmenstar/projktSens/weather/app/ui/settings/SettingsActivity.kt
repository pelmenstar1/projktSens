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

    // here could be int, I'd like reduce collisions as possible
    // but in other way, don't reduce performance
    private lateinit var initialStateHashes: IntArray

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

        initialStateHashes = if(savedInstanceState != null) {
            val hashes = savedInstanceState.getIntArray(STATE_INITIAL_STATE_HASHES) ?: throw NullPointerException("STATE_INITIAL_STATE_HASHES is null")

            if(hashes.size != settings.size) {
                throw RuntimeException("Hashes loaded from savedInstanceState has size that differ from settings.size")
            }

            hashes
        } else {
            IntArray(settings.size) { i ->
                settings[i].state.hashCode()
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
                        var changed = false
                        for(i in settings.indices) {
                            val initialHash = initialStateHashes[i]
                            val currentHash = settings[i].state.hashCode()

                            if(initialHash != currentHash) {
                                changed = true
                                break
                            }
                        }

                        if(changed) {
                            settings.forEach {
                                it.saveStateToPrefs(prefs)
                            }
                        }

                        setResult(RESULT_OK, Intent().apply {
                            putExtra(RETURN_DATA_STATE_CHANGED, changed)
                        })
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

        // Why to save hashes, if we can re-compute it?
        // Saving here is VERY important.
        // The problem:
        //
        // When an user taps the save button, we compare hashes to determine whether
        // settings are actually changed.
        // But let's imagine such case:
        // An user changed something in settings, then rotated a screen, new activity was created and
        // states were loaded from savedInstanceState, hashes were computed from THAT version of states
        // Then the user changed nothing, taped the save button, we compared hashes and they were equal.
        // And why to save settings to preferences if they were not changed, right?
        // So user changed settings, but they were not saved.
        // To avoid that, we compute hashes only once on activity start.
        outState.putIntArray(STATE_INITIAL_STATE_HASHES, initialStateHashes)
    }

    override fun onBackPressed() {
        setResult(RESULT_OK, Intent().apply {
            putExtra(RETURN_DATA_STATE_CHANGED, false)
        })

        super.onBackPressed()
    }

    private fun loadNotosansFont(): Typeface {
        val notosans = ResourcesCompat.getFont(this, R.font.notosans_medium)

        return notosans ?: Typeface.SERIF
    }

    companion object {
        private const val STATE_INITIAL_STATE_HASHES = "SettingsActivity.state.initialStateHashes"

        const val RETURN_DATA_STATE_CHANGED = "SettingsActivity.returnData.stateChanged"

        @JvmStatic
        fun intent(context: Context): Intent {
            return Intent(context, SettingsActivity::class.java)
        }
    }
}