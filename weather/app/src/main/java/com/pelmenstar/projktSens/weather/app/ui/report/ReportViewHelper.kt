@file:JvmName("ReportViewHelper")
@file:Suppress("DEPRECATION")

package com.pelmenstar.projktSens.weather.app.ui.report

import android.content.Context
import android.graphics.Typeface
import android.text.style.CharacterStyle
import android.text.style.StyleSpan
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.setPadding
import com.pelmenstar.projktSens.chartLite.LineChart
import com.pelmenstar.projktSens.chartLite.data.ChartData
import com.pelmenstar.projktSens.shared.android.RoundRectDrawable
import com.pelmenstar.projktSens.shared.android.ui.*
import com.pelmenstar.projktSens.weather.app.R
import com.pelmenstar.projktSens.weather.app.di.AppModule
import com.pelmenstar.projktSens.weather.app.di.DaggerAppComponent
import com.pelmenstar.projktSens.weather.app.formatters.UnitFormatter
import com.pelmenstar.projktSens.weather.app.ui.MaterialChart
import com.pelmenstar.projktSens.weather.models.*

typealias CommonChartOptionsFunc = (chart: LineChart) -> Unit
typealias ParamChartOptionsFunc = (chart: LineChart, unit: Int) -> Unit

private class ParameterStatsPrefixStrings(
    @JvmField val min: String,
    @JvmField val max: String,
    @JvmField val avg: String,
    @JvmField val median: String,
    @JvmField val amplitude: String
)

private class ChartViewCreationContext(
    @JvmField val textLeftMargin: Int,
    blockSideMargin: Int, blockTopBottomMargin: Int, headerBlockTopMargin: Int, chartHeight: Int,
    @JvmField val headerAppearance: TextAppearance, @JvmField val paramAppearance: TextAppearance,
    @JvmField val unitFormatter: UnitFormatter,
    @JvmField val strings: ParameterStatsPrefixStrings,
    @JvmField val chartOptions: (LineChart) -> Unit,
    @JvmField @ColorInt val blockBackgroundColor: Int,
    @JvmField val blockPadding: Int,
    @JvmField val blockRadius: Float,
) {
    @JvmField
    val blockLayoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT).apply {
        leftMargin = blockSideMargin
        rightMargin = blockSideMargin
        topMargin = blockTopBottomMargin
        bottomMargin = blockTopBottomMargin
    }

    @JvmField
    val headerLayoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
        gravity = Gravity.CENTER_HORIZONTAL
        topMargin = headerBlockTopMargin
    }

    @JvmField
    val chartLayoutParams = LinearLayout.LayoutParams(MATCH_PARENT, chartHeight)
}

private class ValueParamCreationContext(
    @JvmField val statsUnit: Int, @JvmField val prefUnit: Int,
    @JvmField val unitFormatter: UnitFormatter,
    @JvmField val textLeftMargin: Int,
    @JvmField val textAppearance: TextAppearance,
    @JvmField val valueStyle: CharacterStyle
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
    val valueStr = c.unitFormatter.formatValueWithDate(vd, c.statsUnit, c.prefUnit)

    ValueParamView(prefix, valueStr, c)
}

private fun ViewGroup.ValueParamView(
    prefix: String, value: Float,
    c: ValueParamCreationContext
) {
    val valueStr = c.unitFormatter.formatValue(
        UnitValue.getValue(value, c.statsUnit, c.prefUnit),
        c.prefUnit
    )

    ValueParamView(prefix, valueStr, c)
}

private fun ViewGroup.ValueParamView(
    prefix: String, value: String,
    c: ValueParamCreationContext
) {
    PrefixTextView {
        layoutParams = c.layoutParams

        valueStyle = c.valueStyle
        setPrefixAndValue(prefix, value)

        applyTextAppearance(c.textAppearance)
    }
}

private fun ViewGroup.ParamStatsBlock(
    paramStats: ParameterStats,
    statsUnit: Int, prefUnit: Int,
    @StringRes headerRes: Int,
    data: ChartData,
    creationContext: ChartViewCreationContext,
    customChartOptionsFunc: ParamChartOptionsFunc?,
) {
    val strings = creationContext.strings

    LinearLayout {
        layoutParams = creationContext.blockLayoutParams
        orientation = LinearLayout.VERTICAL

        setPadding(creationContext.blockPadding)
        background = RoundRectDrawable(
            creationContext.blockBackgroundColor,
            creationContext.blockRadius,
            true
        )

        TextView {
            layoutParams = creationContext.headerLayoutParams

            text = context.resources.getText(headerRes)
            applyTextAppearance(creationContext.headerAppearance)
        }

        val vpContext = ValueParamCreationContext(
            statsUnit, prefUnit,
            creationContext.unitFormatter,
            creationContext.textLeftMargin,
            creationContext.paramAppearance,
            StyleSpan(Typeface.BOLD)
        )

        ValueParamView(strings.min, paramStats.min, vpContext)
        ValueParamView(strings.max, paramStats.max, vpContext)
        ValueParamView(strings.amplitude, paramStats.amplitude, vpContext)
        ValueParamView(strings.avg, paramStats.avg, vpContext)
        ValueParamView(strings.median, paramStats.median, vpContext)

        MaterialChart {
            layoutParams = creationContext.chartLayoutParams

            this.data = data

            creationContext.chartOptions(this)
            customChartOptionsFunc?.invoke(this, prefUnit)

        }
    }
}

fun createChartView(
    context: Context,
    stats: ReportStats,
    temperatureData: ChartData,
    humidityData: ChartData,
    pressureData: ChartData,

    commonChartOptions: CommonChartOptionsFunc,
): View {
    val component = DaggerAppComponent
        .builder()
        .appModule(AppModule(context))
        .build()

    val unitFormatter = component.unitFormatter()
    val prefs = component.preferences()

    val statsUnits = stats.units
    val statsTempUnit = ValueUnitsPacked.getTemperatureUnit(statsUnits)
    val statsPressUnit = ValueUnitsPacked.getPressureUnit(statsUnits)

    val prefUnits = prefs.units
    val prefTempUnit = ValueUnitsPacked.getTemperatureUnit(prefUnits)
    val prefPressUnit = ValueUnitsPacked.getPressureUnit(prefUnits)

    val res = context.resources
    val theme = context.theme

    val textLeftMargin = res.getDimensionPixelOffset(R.dimen.reportActivity_chartView_textLeftMargin)
    val blockHeaderTopMargin = res.getDimensionPixelOffset(R.dimen.reportActivity_chartView_blockHeaderTopMargin)
    val blockSideMargin = res.getDimensionPixelOffset(R.dimen.reportActivity_chartView_blockSideMargin)
    val blockTopBottomMargin = res.getDimensionPixelOffset(R.dimen.reportActivity_chartView_blockTopBottomMargin)
    val chartHeight = res.getDimensionPixelSize(R.dimen.reportActivity_chartHeight)
    val blockRadius = res.getDimension(R.dimen.reportActivity_chartView_blockRadius)
    val blockPadding = res.getDimensionPixelOffset(R.dimen.reportActivity_chartView_blockPadding)

    val blockBackgroundColor = ResourcesCompat.getColor(res, R.color.report_blockColor, theme)

    val headerAppearance =
        TextAppearance(context, R.style.TextAppearance_MaterialComponents_Headline4)
    val paramAppearance = TextAppearance(context, R.style.TextAppearance_MaterialComponents_Body1)

    val strings = ParameterStatsPrefixStrings(
        res.getString(R.string.min),
        res.getString(R.string.max),
        res.getString(R.string.avg),
        res.getString(R.string.median),
        res.getString(R.string.amplitude)
    )

    val creationContext = ChartViewCreationContext(
        textLeftMargin, blockSideMargin, blockTopBottomMargin, blockHeaderTopMargin, chartHeight,
        headerAppearance, paramAppearance,
        unitFormatter,
        strings,
        commonChartOptions,
        blockBackgroundColor,
        blockPadding,
        blockRadius
    )

    return ScrollView(context) {
        LinearLayout {
            linearLayoutParams(MATCH_PARENT, WRAP_CONTENT)

            orientation = LinearLayout.VERTICAL

            ParamStatsBlock(
                stats.temperature,
                statsTempUnit, prefTempUnit,
                R.string.temperature, temperatureData,
                creationContext,
                ParamChartOptions.TEMPERATURE
            )

            ParamStatsBlock(
                stats.humidity,
                ValueUnit.HUMIDITY, ValueUnit.HUMIDITY,
                R.string.humidity, humidityData,
                creationContext,
                ParamChartOptions.HUMIDITY,
            )

            ParamStatsBlock(
                stats.pressure,
                statsPressUnit, prefPressUnit,
                R.string.pressure, pressureData,
                creationContext,
                ParamChartOptions.PRESSURE
            )
        }
    }
}

