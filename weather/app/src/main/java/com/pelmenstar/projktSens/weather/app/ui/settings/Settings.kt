package com.pelmenstar.projktSens.weather.app.ui.settings

import android.content.Context
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import androidx.annotation.ArrayRes
import androidx.appcompat.widget.AppCompatSpinner
import com.pelmenstar.projktSens.shared.android.ReadonlyArrayAdapter
import com.pelmenstar.projktSens.weather.app.Preferences
import com.pelmenstar.projktSens.weather.app.PreferredUnits
import com.pelmenstar.projktSens.weather.app.R
import com.pelmenstar.projktSens.weather.models.ValueUnit
import com.pelmenstar.projktSens.weather.models.ValueUnitsPacked

abstract class Setting<TState : Any> {
    private var _state: TState? = null
    protected var state: TState
        get() = _state ?: throw RuntimeException("State is not loaded")
        set(value) {
            _state = value
        }

    abstract fun getName(context: Context): String
    abstract fun createView(context: Context): View

    abstract fun loadState(prefs: Preferences)
    abstract fun saveState(prefs: Preferences)
}

data class ValueUnitState(var unit: Int)

class TemperatureSetting: Setting<ValueUnitState>() {
    override fun getName(context: Context): String {
        return context.resources.getString(R.string.temperature)
    }

    override fun createView(context: Context): View {
        return AppCompatSpinner(context).apply {
            adapter = simpleArrayAdapter(context, R.array.temperatureUnits)

            setSelection(
                when (state.unit) {
                    ValueUnit.CELSIUS -> 0
                    ValueUnit.KELVIN -> 1
                    ValueUnit.FAHRENHEIT -> 2
                    else -> throw IllegalStateException("Illegal prefs")
                }
            )

            onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    state.unit = when (position) {
                        0 -> ValueUnit.CELSIUS
                        1 -> ValueUnit.KELVIN
                        2 -> ValueUnit.FAHRENHEIT

                        else -> return
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    override fun saveState(prefs: Preferences) {
        val pressUnit = ValueUnitsPacked.getPressureUnit(PreferredUnits.getUnits())
        val newUnits = ValueUnitsPacked.create(state.unit, pressUnit)

        PreferredUnits.setUnits(newUnits)
        prefs.units = newUnits
    }

    override fun loadState(prefs: Preferences) {
        val units = prefs.units
        state = ValueUnitState(ValueUnitsPacked.getTemperatureUnit(units))
    }
}

class PressureSetting: Setting<ValueUnitState>() {
    override fun getName(context: Context): String {
        return context.resources.getString(R.string.pressure)
    }

    override fun createView(context: Context): View {
        return AppCompatSpinner(context).apply {
            adapter = simpleArrayAdapter(context, R.array.pressureUnits)

            setSelection(when(state.unit) {
                ValueUnit.MM_OF_MERCURY -> 0
                ValueUnit.PASCAL -> 1
                else -> throw IllegalStateException("Illegal prefs")
            })

            onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    state.unit = when (position) {
                        0 -> ValueUnit.MM_OF_MERCURY
                        1 -> ValueUnit.PASCAL

                        else -> return
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }

    override fun saveState(prefs: Preferences) {
        val tempUnit = ValueUnitsPacked.getTemperatureUnit(PreferredUnits.getUnits())
        val newUnits = ValueUnitsPacked.create(tempUnit, state.unit)

        PreferredUnits.setUnits(newUnits)
        prefs.units = newUnits
    }

    override fun loadState(prefs: Preferences) {
        val units = prefs.units

        state = ValueUnitState(ValueUnitsPacked.getPressureUnit(units))
    }
}

private fun simpleArrayAdapter(context: Context, @ArrayRes resId: Int): ReadonlyArrayAdapter<String> {
    return ReadonlyArrayAdapter(
        context,
        android.R.layout.simple_spinner_item,
        context.resources.getStringArray(resId)
    ).apply {
        setDropDownResource(android.R.layout.simple_spinner_dropdown_item)
    }
}