package com.pelmenstar.projktSens.weather.app.ui.firstStart

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import com.pelmenstar.projktSens.serverProtocol.ContractType
import com.pelmenstar.projktSens.shared.android.Preferences
import com.pelmenstar.projktSens.shared.android.ReadonlyArrayAdapter
import com.pelmenstar.projktSens.shared.android.ui.*
import com.pelmenstar.projktSens.weather.app.AppPreferences
import com.pelmenstar.projktSens.weather.app.R

class ChooseServerContractScreen : FirstStartScreen<ChooseServerContractScreen.State>() {
    class State(@JvmField var contractType: Int)

    override fun getTitleId(): Int = R.string.firstStart_chooseServerContractTitle

    override fun createView(context: Context): View {
        val res = context.resources
        val contracts = res.getStringArray(R.array.serverContracts)

        return FrameLayout(context) {
            layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)

            TextView {
                frameLayoutParams(WRAP_CONTENT, WRAP_CONTENT) {
                    gravity = Gravity.START
                }

                applyTextAppearance(R.style.TextAppearance_MaterialComponents_Body1)
                text = res.getText(R.string.serverContract)
            }

            Spinner {
                frameLayoutParams(WRAP_CONTENT, WRAP_CONTENT) {
                    gravity = Gravity.END
                }

                adapter = ReadonlyArrayAdapter(
                    context,
                    android.R.layout.simple_list_item_1,
                    contracts
                ).apply {
                    setDropDownResource(android.R.layout.simple_spinner_dropdown_item)
                }

                setSelection(
                    when (state.contractType) {
                        ContractType.RAW -> 0
                        ContractType.JSON -> 1
                        else -> throw RuntimeException()
                    }
                )

                onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        state.contractType = when (position) {
                            0 -> ContractType.RAW
                            1 -> ContractType.JSON
                            else -> throw RuntimeException()
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                    }
                }
            }
        }
    }

    override fun loadDefaultState() {
        state = State(ContractType.RAW)
    }

    override fun loadStateFromBundle(bundle: Bundle): Boolean {
        val type = bundle.get(STATE_CONTRACT)
        return if (type != null) {
            state = State(type as Int)
            true
        } else {
            false
        }
    }

    override fun saveStateToPrefs(prefs: Preferences) {
        prefs.setInt(AppPreferences.CONTRACT, state.contractType)
    }

    override fun saveStateToBundle(outState: Bundle) {
        outState.putInt(STATE_CONTRACT, state.contractType)
    }

    companion object {
        private const val STATE_CONTRACT = "ChooseServerContractScreen.state.contract"
    }
}