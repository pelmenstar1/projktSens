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
import android.widget.GridLayout
import com.pelmenstar.projktSens.shared.ReflectionUtils
import com.pelmenstar.projktSens.shared.android.*
import com.pelmenstar.projktSens.shared.android.ext.Intent
import com.pelmenstar.projktSens.shared.android.ext.getStringArrayNotNull
import com.pelmenstar.projktSens.shared.android.ext.getStringNotNull
import com.pelmenstar.projktSens.shared.android.ui.*
import com.pelmenstar.projktSens.shared.getChars

class SettingsActivity : HomeButtonSupportActivity() {
    private lateinit var settings: Array<out Setting<*>>
    private lateinit var prefs: Preferences

    private lateinit var saveButton: Button

    private var initialStateHash: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        actionBar {
            title = getText(R.string.settings_title)
            setDisplayHomeAsUpEnabled(true)
        }

        initFromIntentExtra()
        loadStates(savedInstanceState)
        computeHashOrGetFromBundle(savedInstanceState)

        setContentView(createContent())

        disableSaveButtonIfSettingsInvalid()
    }

    private fun initFromIntentExtra() {
        val intent = requireIntent()
        val settingClassNames = intent.getStringArrayNotNull(EXTRA_SETTINGS)
        settings = Array(settingClassNames.size) { i ->
            ReflectionUtils.createFromEmptyConstructor(settingClassNames[i])
        }

        val prefsName = intent.getStringNotNull(EXTRA_PREFERENCES)
        prefs = ReflectionUtils.createFromEmptyConstructorOrInstance<Preferences>(prefsName).also {
            it.initialize(this)
        }
    }

    private fun loadStates(savedInstanceState: Bundle?) {
        settings.forEach {
            if (savedInstanceState != null) {
                val success = it.loadStateFromBundle(savedInstanceState)
                if (success) {
                    return@forEach
                }
            }
            it.loadStateFromPrefs(prefs)
        }
    }

    private fun computeHashOrGetFromBundle(savedInstanceState: Bundle?) {
        val savedHash = savedInstanceState?.get(STATE_INITIAL_STATE_HASH)
        initialStateHash = if (savedHash != null) {
            savedHash as Long
        } else {
            computeCurrentStateHash()
        }
    }

    private fun computeCurrentStateHash(): Long {
        val settings = settings
        var result: Long = 1
        for (setting in settings) {
            result = result * 31 + setting.state.hashCode()
        }

        return result
    }

    private fun disableSaveButtonIfSettingsInvalid() {
        val onValidChangedListener = Setting.IncompleteState.OnValidChanged { isValid ->
            saveButton.isEnabled = isValid
        }

        settings.forEach {
            // after state is loaded
            val state = it.state

            if (state is Setting.IncompleteState) {
                state.onValidChanged = onValidChangedListener
                // isValid can already be false, so update saveButton
                if (!state.isValid) {
                    saveButton.isEnabled = false
                }
            }
        }
    }

    private fun createContent(): View {
        val prefs = prefs
        val settings = settings

        val res = resources
        val context = this
        val boundsMargin = res.getDimensionPixelSize(R.dimen.settings_boundsMargin)

        val body1 = TextAppearance(context, R.style.TextAppearance_MaterialComponents_Body1)

        return FrameLayout(this) {
            GridLayout {
                frameLayoutParams(MATCH_PARENT, MATCH_PARENT)

                columnCount = 2
                rowCount = settings.size

                val settingNameSpec = GridLayout.spec(0, GridLayout.START)
                val viewSpec = GridLayout.spec(1, GridLayout.END)

                for (i in settings.indices) {
                    val setting = settings[i]

                    val rowSpec = GridLayout.spec(i)
                    TextView {
                        gridLayoutParams(
                            rowSpec,
                            columnSpec = settingNameSpec
                        ) {
                            leftMargin = boundsMargin
                        }

                        applyTextAppearance(body1)

                        val name = res.getString(setting.nameId)
                        val nameLength = name.length

                        val buffer = CharArray(nameLength + 1)
                        name.getChars(0, nameLength, buffer, 0)
                        buffer[nameLength] = ':'

                        setText(buffer, 0, buffer.size)
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
                        val recomputedHash: Long = computeCurrentStateHash()
                        val changed = recomputedHash != initialStateHash

                        if (changed) {
                            prefs.beginModifying()
                            settings.forEach {
                                it.saveStateToPrefs(prefs)
                            }
                            prefs.endModifying()
                        }

                        setResult(changed)
                        finish()
                    }
                }
            }
        }
    }

    private fun setResult(settingsChanged: Boolean) {
        setResult(RESULT_OK, Intent().apply {
            putExtra(RETURN_DATA_STATE_CHANGED, settingsChanged)
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        settings.forEach { it.saveStateToBundle(outState) }

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
        outState.putLong(STATE_INITIAL_STATE_HASH, initialStateHash)
    }

    override fun onBackPressed() {
        setResult(settingsChanged = false)
        super.onBackPressed()
    }

    companion object {
        private const val STATE_INITIAL_STATE_HASH = "SettingsActivity.state.initialStateHash"
        private const val EXTRA_SETTINGS = "SettingsActivity.intent.settings"
        private const val EXTRA_PREFERENCES = "SettingsActivity.intent.preferences"

        const val RETURN_DATA_STATE_CHANGED = "SettingsActivity.returnData.stateChanged"

        @JvmStatic
        fun intent(
            context: Context,
            settingClasses: Array<out Class<out Setting<*>>>,
            prefsClass: Class<out Preferences>
        ): Intent {
            val names = Array(settingClasses.size) { i ->
                settingClasses[i].name
            }

            return intent(context, names, prefsClass)
        }

        @JvmStatic
        fun intent(
            context: Context,
            settingClassNames: Array<out String>,
            prefsClass: Class<out Preferences>
        ): Intent {
            return Intent(context, SettingsActivity::class.java) {
                putExtra(EXTRA_SETTINGS, settingClassNames)
                putExtra(EXTRA_PREFERENCES, prefsClass.name)
            }
        }
    }
}