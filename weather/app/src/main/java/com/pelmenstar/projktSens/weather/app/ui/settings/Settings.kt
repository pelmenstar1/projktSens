package com.pelmenstar.projktSens.weather.app.ui.settings

import android.content.Context
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import androidx.annotation.ArrayRes
import androidx.appcompat.widget.AppCompatSpinner
import com.pelmenstar.projktSens.shared.android.ReadonlyArrayAdapter
import com.pelmenstar.projktSens.weather.app.Preferences
import com.pelmenstar.projktSens.weather.app.PreferredUnits
import com.pelmenstar.projktSens.weather.app.R
import com.pelmenstar.projktSens.weather.models.ValueUnit
import com.pelmenstar.projktSens.weather.models.ValueUnitsPacked

interface Setting {
    fun getName(context: Context): String

    fun createView(context: Context): View
}

object TemperatureSetting: Setting {
    override fun getName(context: Context): String {
        return context.resources.getString(R.string.temperature)
    }

    override fun createView(context: Context): View {
        val prefs = Preferences.of(context)
        val prefUnits = prefs.units
        val pressUnit = ValueUnitsPacked.getPressureUnit(prefUnits)

        return AppCompatSpinner(context).apply {
            adapter = simpleArrayAdapter(context, R.array.temperatureUnits)

            setSelection(
                when (ValueUnitsPacked.getTemperatureUnit(prefs.units)) {
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
                    val tempUnit = when (position) {
                        0 -> ValueUnit.CELSIUS
                        1 -> ValueUnit.KELVIN
                        2 -> ValueUnit.FAHRENHEIT

                        else -> return
                    }

                    val units = ValueUnitsPacked.create(tempUnit, pressUnit)
                    prefs.units = units
                    PreferredUnits.setUnits(units)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
    }
}

object PressureSetting: Setting {
    override fun getName(context: Context): String {
        return context.resources.getString(R.string.pressure)
    }

    override fun createView(context: Context): View {
        val prefs = Preferences.of(context)
        val prefUnits = prefs.units
        val tempUnit = ValueUnitsPacked.getTemperatureUnit(prefUnits)

        return AppCompatSpinner(context).apply {
            adapter = simpleArrayAdapter(context, R.array.pressureUnits)

            setSelection(when(ValueUnitsPacked.getPressureUnit(prefUnits)) {
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
                    val pressUnit = when (position) {
                        0 -> ValueUnit.MM_OF_MERCURY
                        1 -> ValueUnit.PASCAL

                        else -> return
                    }

                    val units = ValueUnitsPacked.create(tempUnit, pressUnit)

                    prefs.units = units
                    PreferredUnits.setUnits(units)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }
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