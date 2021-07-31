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
    @JvmField val min: String,
    @JvmField val max: String,
    @JvmField val avg: String,
    @JvmField val median: String
)

private class ChartViewCreationContext(
    @JvmField val textLeftMargin: Int,
    chartSideMargin: Int, blockTopMargin: Int, chartHeight: Int,
    @JvmField val headerAppearance: TextAppearance, @JvmField val paramAppearance: TextAppearance,
    @JvmField val unitFormatter: UnitFormatter,
    @JvmField val strings: ParameterStatsPrefixStrings,
    @JvmField val chartOptions: (LineChart) -> Unit
) {
    @JvmField
    val headerLayoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
        gravity = Gravity.CENTER_HORIZONTAL
        topMargin = blockTopMargin
    }

    @JvmField
    val chartLayoutParams = LinearLayout.LayoutParams(MATCH_PARENT, chartHeight).apply {
        leftMargin = chartSideMargin
        rightMargin = chartSideMargin
    }
}

private class ValueParamCreationContext(
    @JvmField val statsUnit: Int, @JvmField val prefUnit: Int,
    @JvmField val unitFormatter: UnitFormatter,
    @JvmField val textLeftMargin: Int,
    @JvmField val textAppearance: TextAppearance
) {
    @JvmField
    val layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
        leftMargin = textLeftMargin
    }
}

private fun ViewGroup.ValueParamView(
    prefix: String, vd: ValueWithDate,
    c: ValueParamCreationContext
) {
    PrefixTextView {
        layoutParams = c.layoutParams

        val valueSb = c.unitFormatter.formatValueWithDateToBuilder(vd, c.statsUnit, c.prefUnit)
        setPrefixAndValue(prefix, valueSb)

        applyTextAppearance(c.textAppearance)
    }
}

private fun ViewGroup.ValueParamView(
    prefix: String, value: Float,
    c: ValueParamCreationContext
) {
    PrefixTextView {
        layoutParams = c.layoutParams

        val valueSb = c.unitFormatter.formatValueToBuilder(
            UnitValue.getValue(value, c.statsUnit, c.prefUnit),
            c.prefUnit
        )

        setPrefixAndValue(prefix, valueSb)

        applyTextAppearance(c.textAppearance)
    }
}

private fun ViewGroup.ParamStatsBlock(
    paramStats: ParameterStats,
    statsUnit: Int, prefUnit: Int,
    @StringRes headerRes: Int,
    data: ChartData,
    creationContext: ChartViewCreationContext
) {
    val strings = creationContext.strings

    TextView {
        layoutParams = creationContext.headerLayoutParams

        text = context.resources.getText(headerRes)
        applyTextAppearance(creationContext.headerAppearance)
    }

    val vpContext = ValueParamCreationContext(
        statsUnit, prefUnit,
        creationContext.unitFormatter,
        creationContext.textLeftMargin,
        creationContext.paramAppearance
    )

    ValueParamView(strings.min, paramStats.min, vpContext)
    ValueParamView(strings.max, paramStats.max, vpContext)
    ValueParamView(strings.avg, paramStats.avg, vpContext)
    ValueParamView(strings.median, paramStats.median, vpContext)

    MaterialChart {
        layoutParams = creationContext.chartLayoutParams

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

    val headerAppearance = TextAppearance(context, R.style.TextAppearance_MaterialComponents_Headline4)
    val paramAppearance = TextAppearance(context, R.style.TextAppearance_MaterialComponents_Body1)

    val strings = ParameterStatsPrefixStrings(
        res.getString(R.string.min),
        res.getString(R.string.max),
        res.getString(R.string.avg),
        res.getString(R.string.median)
    )

    val creationContext = ChartViewCreationContext(
        textLeftMargin, chartSideMargin, blockTopMargin,
        chartHeight,
        headerAppearance, paramAppearance,
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

