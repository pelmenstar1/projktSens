@file:Suppress("FunctionName", "NOTHING_TO_INLINE")
@file:SuppressLint("SetTextI18n")

package com.pelmenstar.projktSens.shared.android.ui.settings

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import com.pelmenstar.projktSens.shared.ReflectionUtils
import com.pelmenstar.projktSens.shared.android.Intent
import com.pelmenstar.projktSens.shared.android.Preferences
import com.pelmenstar.projktSens.shared.android.R
import com.pelmenstar.projktSens.shared.android.ui.*

class SettingsActivity : HomeButtonSupportActivity() {
    private lateinit var settingsContext: SettingsContext
    private lateinit var prefs: Preferences

    private lateinit var saveButton: Button

    // here could be int, I'd like reduce collisions as possible
    // but in other way, don't reduce performance
    private lateinit var initialStateHashes: IntArray

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        actionBar {
            title = resources.getText(R.string.settings_title)
            setDisplayHomeAsUpEnabled(true)
        }

        initFromIntentExtra()
        loadStates(savedInstanceState)
        computeHashes(savedInstanceState)

        setContentView(createContent())

        disableSaveButtonIfSettingsInvalid()
    }

    private fun initFromIntentExtra() {
        val intent = requireIntent()
        settingsContext = intent.getParcelableExtra(EXTRA_SETTINGS_CONTEXT)!!
        val prefsName = intent.getStringExtra(EXTRA_PREFERENCES)!!
        prefs = ReflectionUtils.createFromEmptyConstructorOrInstance(prefsName)
        prefs.initialize(this)
    }

    private fun loadStates(savedInstanceState: Bundle?) {
        settingsContext.settings.forEach {
            if(savedInstanceState != null) {
                val success = it.loadStateFromBundle(savedInstanceState)
                if(!success) {
                    it.loadStateFromPrefs(prefs)
                }
            } else {
                it.loadStateFromPrefs(prefs)
            }
        }
    }

    private fun computeHashes(savedInstanceState: Bundle?) {
        val settings = settingsContext.settings
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
    }

    private fun disableSaveButtonIfSettingsInvalid() {
        val onValidChangedListener = Setting.IncompleteState.OnValidChanged { isValid ->
            saveButton.isEnabled = isValid
        }

        settingsContext.settings.forEach {
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
        val prefs = prefs
        val settings = settingsContext.settings

        val res = resources
        val context = this
        val boundsMargin = res.getDimensionPixelSize(R.dimen.settings_boundsMargin)

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
                            leftMargin = boundsMargin
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
                            rightMargin = boundsMargin
                        }
                    })
                }
            }

            FrameLayout {
                saveButton = Button {
                    frameLayoutParams(WRAP_CONTENT, WRAP_CONTENT) {
                        gravity = Gravity.BOTTOM or Gravity.END
                        rightMargin = boundsMargin
                        bottomMargin = boundsMargin
                    }
                    text = res.getText(R.string.settings_save)

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
                            prefs.beginModifying()
                            settings.forEach {
                                it.saveStateToPrefs(prefs)
                            }
                            prefs.endModifying()
                        }

                        setResult(RESULT_OK, Intent().apply {
                            putExtra(RETURN_DATA_STATE_CHANGED, changed)
                        })
                        finish()
                    }
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val settings = settingsContext.settings

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

    companion object {
        private const val STATE_INITIAL_STATE_HASHES = "SettingsActivity.state.initialStateHashes"
        private const val EXTRA_SETTINGS_CONTEXT = "SettingsActivity.intent.settingsContext"
        private const val EXTRA_PREFERENCES = "SettingsActivity.intent.preferences"

        const val RETURN_DATA_STATE_CHANGED = "SettingsActivity.returnData.stateChanged"

        @JvmStatic
        fun intent(
            context: Context,
            settingsContext: SettingsContext,
            prefsClass: Class<out Preferences>
        ): Intent {
            return Intent(context, SettingsActivity::class.java) {
                putExtra(EXTRA_SETTINGS_CONTEXT, settingsContext)
                putExtra(EXTRA_PREFERENCES, prefsClass.name)
            }
        }
    }
}