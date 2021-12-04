package com.pelmenstar.projktSens.weather.app.ui.report

import com.pelmenstar.projktSens.weather.models.ValueUnit
import kotlin.math.max
import kotlin.math.min

object ParamChartOptions {
    val TEMPERATURE: ParamChartOptionsFunc = { chart, unit ->
        chart.yAxis.apply {
            granularity = when(unit) {
                ValueUnit.FAHRENHEIT -> 10f
                ValueUnit.KELVIN -> 20f

                // Celsius
                else -> 5f
            }

            val data = chart.data
            if(data != null) {
                val yMin = data.yMin
                val yMax = data.yMax

                when(unit) {
                    ValueUnit.CELSIUS -> {
                        axisMinimum = max(yMin - 10f, -30f)
                        axisMaximum = min(yMax + 10f, 40f)
                    }
                    ValueUnit.FAHRENHEIT -> {
                        axisMinimum = max(yMin - 10f, 0f)
                        axisMaximum = min(yMax + 10f, 60f)
                    }
                    ValueUnit.KELVIN -> {
                        axisMinimum = max(yMin - 20f, 243f)
                        axisMaximum = min(yMax + 20f, 313f)
                    }
                }
            }
        }
    }

    val HUMIDITY: ParamChartOptionsFunc = { chart, _ ->
        chart.yAxis.apply {
            granularity = 5f

            val data = chart.data
            if(data != null) {
                val yMin = data.yMin
                val yMax = data.yMax

                axisMinimum = max(yMin - 10f, 0f)
                axisMaximum = max(yMax + 10f, 90f)
            }
        }
    }

    val PRESSURE: ParamChartOptionsFunc = { chart, unit ->
        chart.yAxis.apply {
            granularity = if(unit == ValueUnit.MM_OF_MERCURY) {
                10f
            } else {
                100f
            }
            granularity = 5f

            val data = chart.data
            if (data != null) {
                val yMin = data.yMin
                val yMax = data.yMax

                if(unit == ValueUnit.MM_OF_MERCURY) {
                    axisMinimum = max(yMin - 50f, 0f)
                    axisMaximum = min(yMax + 50f, 800f)
                } else {
                    axisMinimum = max(yMin - 100f, 0f)
                    axisMaximum = min(yMax + 100f, 106400f)
                }
            }
        }
    }
}