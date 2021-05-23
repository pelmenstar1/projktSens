@file:JvmName("ReportViewHelper")
@file:Suppress("DEPRECATION")

package com.pelmenstar.projktSens.weather.app.ui.report

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.StringRes
import com.pelmenstar.projktSens.chartLite.LineChart
import com.pelmenstar.projktSens.chartLite.data.ChartData
import com.pelmenstar.projktSens.shared.android.ui.*
import com.pelmenstar.projktSens.weather.app.PreferredUnits
import com.pelmenstar.projktSens.weather.app.R
import com.pelmenstar.projktSens.weather.app.di.AppModule
import com.pelmenstar.projktSens.weather.app.di.DaggerAppComponent
import com.pelmenstar.projktSens.weather.app.formatters.UnitFormatter
import com.pelmenstar.projktSens.weather.app.ui.MaterialChart
import com.pelmenstar.projktSens.weather.models.*

private class ParameterStatsPrefixStrings(
    val min: String,
    val max: String,
    val avg: String,
    val median: String,
    val stdDev: String,
    val stdErr: String,
)

private class ChartViewCreationContext(
    val textLeftMargin: Int, val chartSideMargin: Int, val blockTopMargin: Int,
    val chartHeight: Int,
    val headline4: TextAppearance, val body1: TextAppearance,
    val unitFormatter: UnitFormatter,
    val strings: ParameterStatsPrefixStrings,
    val chartOptions: (LineChart) -> Unit
)

@Suppress("NOTHING_TO_INLINE")
private inline fun ViewGroup.ValueParamView(
    prefix: String, value: Float,
    statsUnit: Int, prefUnit: Int,
    unitFormatter: UnitFormatter,
    textLeftMargin: Int,
    textAppearance: TextAppearance
) {
    PrefixTextView {
        linearLayoutParams(WRAP_CONTENT, WRAP_CONTENT) {
            leftMargin = textLeftMargin
        }

        this.prefix = prefix
        this.value = unitFormatter.formatValue(
            UnitValue.getValue(value, statsUnit, prefUnit),
            prefUnit
        )

        applyTextAppearance(textAppearance)
    }
}

private fun ViewGroup.ParamStatsBlock(
    paramStats: ParameterStats,
    statsUnit: Int, prefUnit: Int,
    @StringRes headerRes: Int,
    data: ChartData,
    creationContext: ChartViewCreationContext
) {
    val unitFormatter = creationContext.unitFormatter
    val strings = creationContext.strings

    val body1 = creationContext.body1

    val textLeftMargin = creationContext.textLeftMargin

    TextView {
        linearLayoutParams(WRAP_CONTENT, WRAP_CONTENT) {
            gravity = Gravity.CENTER_HORIZONTAL
            topMargin = creationContext.blockTopMargin
        }

        text = context.resources.getText(headerRes)
        applyTextAppearance(creationContext.headline4)
    }

    ValueParamView(strings.min, paramStats.min, statsUnit, prefUnit, unitFormatter, textLeftMargin, body1)
    ValueParamView(strings.max, paramStats.max, statsUnit, prefUnit, unitFormatter, textLeftMargin, body1)
    ValueParamView(strings.avg, paramStats.avg, statsUnit, prefUnit, unitFormatter, textLeftMargin, body1)
    ValueParamView(strings.median, paramStats.median, statsUnit, prefUnit, unitFormatter, textLeftMargin, body1)
    ValueParamView(strings.stdDev, paramStats.stdDev, statsUnit, prefUnit, unitFormatter, textLeftMargin, body1)
    ValueParamView(strings.stdErr, paramStats.stdErr, statsUnit, prefUnit, unitFormatter, textLeftMargin, body1)

    MaterialChart {
        val chartSideMargin = creationContext.chartSideMargin

        linearLayoutParams(MATCH_PARENT, creationContext.chartHeight) {
            leftMargin = chartSideMargin
            rightMargin = chartSideMargin
        }

        creationContext.chartOptions(this)
        this.data = data
    }
}

fun createChartView(
    context: Context,
    stats: ReportStats,
    temperatureData: ChartData,
    humidityData: ChartData,
    pressureData: ChartData,
    chartOptions: (LineChart) -> Unit
): View {
    val component = DaggerAppComponent
        .builder()
        .appModule(AppModule(context))
        .build()

    val unitFormatter = component.unitFormatter()

    val statsUnits = stats.units
    val statsTempUnit = ValueUnitsPacked.getTemperatureUnit(statsUnits)
    val statsPressUnit = ValueUnitsPacked.getPressureUnit(statsUnits)

    val prefUnits = PreferredUnits.getUnits()
    val prefTempUnit = ValueUnitsPacked.getTemperatureUnit(prefUnits)
    val prefPressUnit = ValueUnitsPacked.getPressureUnit(prefUnits)

    val res = context.resources

    val textLeftMargin = res.getDimensionPixelOffset(R.dimen.reportActivity_chartViewTextLeftMargin)
    val chartSideMargin = res.getDimensionPixelOffset(R.dimen.reportActivity_chartViewSideMargin)
    val blockTopMargin = res.getDimensionPixelOffset(R.dimen.reportActivity_chartViewBlockTopMargin)
    val chartHeight = res.getDimensionPixelSize(R.dimen.reportActivity_chartHeight)

    val headline4 = TextAppearance(context, R.style.TextAppearance_MaterialComponents_Headline4)
    val body1 = TextAppearance(context, R.style.TextAppearance_MaterialComponents_Body1)

    val strings = ParameterStatsPrefixStrings(
        res.getString(R.string.min),
        res.getString(R.string.max),
        res.getString(R.string.avg),
        res.getString(R.string.median),
        res.getString(R.string.stdDev),
        res.getString(R.string.stdErr),
    )

    val creationContext = ChartViewCreationContext(
        textLeftMargin, chartSideMargin, blockTopMargin,
        chartHeight,
        headline4, body1,
        unitFormatter,
        strings,
        chartOptions
    )

    return ScrollView(context) {
        LinearLayout {
            linearLayoutParams(MATCH_PARENT, WRAP_CONTENT)

            orientation = LinearLayout.VERTICAL

            ParamStatsBlock(
                stats.temperature,
                statsTempUnit, prefTempUnit,
                R.string.temperature, temperatureData,
                creationContext
            )

            ParamStatsBlock(
                stats.humidity,
                ValueUnit.HUMIDITY, ValueUnit.HUMIDITY,
                R.string.humidity, humidityData,
                creationContext
            )

            ParamStatsBlock(
                stats.pressure,
                statsPressUnit, prefPressUnit,
                R.string.pressure, pressureData,
                creationContext
            )
        }
    }
}

