@file:Suppress("FunctionName", "NOTHING_TO_INLINE")
@file:SuppressLint("SetTextI18n")

package com.pelmenstar.projktSens.shared.android.ui.settings

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.widget.LinearLayout
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import androidx.core.view.setMargins
import com.pelmenstar.projktSens.shared.ReflectionUtils
import com.pelmenstar.projktSens.shared.android.*
import com.pelmenstar.projktSens.shared.android.ext.Intent
import com.pelmenstar.projktSens.shared.android.ext.getStringNotNull
import com.pelmenstar.projktSens.shared.android.ui.*

class SettingsActivity : HomeButtonSupportActivity() {
    private lateinit var settingGroups: Array<out SettingGroup>
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
        val groupsRaw = intent.getParcelableArrayExtra(EXTRA_GROUPS)!!
        settingGroups = Array(groupsRaw.size) { i -> groupsRaw[i] as SettingGroup }

        val prefsName = intent.getStringNotNull(EXTRA_PREFERENCES)
        prefs = ReflectionUtils.createFromEmptyConstructorOrInstance<Preferences>(prefsName).also {
            it.initialize(this)
        }
    }

    private fun loadStates(savedInstanceState: Bundle?) {
        settingGroups.forEach {
            for(setting in it.items) {
                if (savedInstanceState != null) {
                    val success = setting.loadStateFromBundle(savedInstanceState)
                    if (success) {
                        continue
                    }
                }

                setting.loadStateFromPrefs(prefs)
            }
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
        val groups = settingGroups
        var result: Long = 1

        for(group in groups) {
            for(setting in group.items) {
                result = result * 31 + setting.state.hashCode()
            }
        }

        return result
    }

    private fun disableSaveButtonIfSettingsInvalid() {
        val onValidChangedListener = Setting.IncompleteState.OnValidChanged { isValid ->
            saveButton.isEnabled = isValid
        }

        // after state is loaded
        settingGroups.forEach { group ->
            group.items.forEach { setting ->
                val state = setting.state

                if (state is Setting.IncompleteState) {
                    state.onValidChanged = onValidChangedListener

                    // isValid can already be false, so update saveButton
                    if (!state.isValid) {
                        saveButton.isEnabled = false
                    }
                }
            }


        }
    }

    private fun createContent(): View {
        val prefs = prefs
        val groups = settingGroups

        val res = resources
        val boundsMargin = res.getDimensionPixelSize(R.dimen.settings_groupMargin)

        return FrameLayout(this) {
            LinearLayout {
                frameLayoutParams(MATCH_PARENT, MATCH_PARENT)

                orientation = LinearLayout.VERTICAL

                val groupLayoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
                    setMargins(boundsMargin)
                }

                val options = GroupInflateOptions(context)

                for(group in groups) {
                    addView(createGroupLayout(context, group, options).apply {
                        layoutParams = groupLayoutParams
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
                            prefs.modify {
                                groups.forEach { group ->
                                    group.items.forEach { setting ->
                                        setting.saveStateToPrefs(this)
                                    }
                                }
                            }
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

        settingGroups.forEach {
            it.items.forEach { setting -> setting.saveStateToBundle(outState)  }
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
        outState.putLong(STATE_INITIAL_STATE_HASH, initialStateHash)
    }

    override fun onBackPressed() {
        setResult(settingsChanged = false)
        super.onBackPressed()
    }

    companion object {
        private const val STATE_INITIAL_STATE_HASH = "SettingsActivity.state.initialStateHash"
        private const val EXTRA_GROUPS = "SettingsActivity.intent.groups"
        private const val EXTRA_PREFERENCES = "SettingsActivity.intent.preferences"

        const val RETURN_DATA_STATE_CHANGED = "SettingsActivity.returnData.stateChanged"

        @JvmStatic
        fun intent(
            context: Context,
            groups: Array<out SettingGroup>,
            prefsClass: Class<out Preferences>
        ): Intent {
            return Intent(context, SettingsActivity::class.java) {
                putExtra(EXTRA_GROUPS, groups)
                putExtra(EXTRA_PREFERENCES, prefsClass.name)
            }
        }
    }
}