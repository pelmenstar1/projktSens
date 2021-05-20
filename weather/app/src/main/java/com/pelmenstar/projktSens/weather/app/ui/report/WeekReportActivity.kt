package com.pelmenstar.projktSens.weather.app.ui.report

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.pelmenstar.projktSens.chartLite.data.ChartData
import com.pelmenstar.projktSens.chartLite.data.DataSet
import com.pelmenstar.projktSens.chartLite.data.Entry
import com.pelmenstar.projktSens.chartLite.formatter.ValueFormatter
import com.pelmenstar.projktSens.shared.StringUtils
import com.pelmenstar.projktSens.shared.android.Intent
import com.pelmenstar.projktSens.shared.android.charts.DateChartFormatter
import com.pelmenstar.projktSens.shared.android.ui.actionBar
import com.pelmenstar.projktSens.shared.android.ui.requireIntent
import com.pelmenstar.projktSens.shared.time.ShortDate
import com.pelmenstar.projktSens.shared.time.ShortDateInt
import com.pelmenstar.projktSens.shared.time.ShortDateRange
import com.pelmenstar.projktSens.weather.app.PreferredUnits
import com.pelmenstar.projktSens.weather.app.R
import com.pelmenstar.projktSens.weather.app.di.DaggerAppComponent
import com.pelmenstar.projktSens.weather.app.formatters.UnitChartValueFormatter
import com.pelmenstar.projktSens.weather.models.*

class WeekReportActivity : ReportActivityBase<DayRangeReport>(DayRangeReport.SERIALIZER) {
    private var startDate = 0
    private var endDate = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        val intent = requireIntent()

        startDate = intent.getIntExtra(EXTRA_START_DATE, ShortDate.NONE)
        endDate = intent.getIntExtra(EXTRA_END_DATE, ShortDate.NONE)

        actionBar {
            val component = DaggerAppComponent.create()
            val dateFormatter = component.prettyDateFormatter()

            setDisplayHomeAsUpEnabled(true)
            title = buildString {
                dateFormatter.appendPrettyDate(startDate, this)
                append('-')
                dateFormatter.appendPrettyDate(endDate, this)
            }
        }

        super.onCreate(savedInstanceState)
    }

    override suspend fun loadReport(dataSource: WeatherDataSource): DayRangeReport? {
        return dataSource.getDayRangeReport(ShortDateRange(startDate, endDate))
    }

    override fun createChartView(report: DayRangeReport): View {
        val resources = resources
        val theme = theme
        val minColor = ResourcesCompat.getColor(resources, R.color.chartMinColor, theme)
        val maxColor = ResourcesCompat.getColor(resources, R.color.chartMaxColor, theme)
        val unitFormatter = DaggerAppComponent.create().unitFormatter()

        val prefUnits = PreferredUnits.getUnits()
        val prefTempUnit = ValueUnitsPacked.getTemperatureUnit(prefUnits)
        val prefPressUnit = ValueUnitsPacked.getPressureUnit(prefUnits)

        val units = report.stats.units
        val tempUnit = ValueUnitsPacked.getTemperatureUnit(units)
        val pressUnit = ValueUnitsPacked.getPressureUnit(units)

        val monthEntries = report.entries
        val entriesSize = monthEntries.size

        val minTempEntries = LongArray(entriesSize)
        val maxTempEntries = LongArray(entriesSize)
        val minHumEntries = LongArray(entriesSize)
        val maxHumEntries = LongArray(entriesSize)
        val minPressEntries = LongArray(entriesSize)
        val maxPressEntries = LongArray(entriesSize)

        val xValueFormatter: ValueFormatter
        val formatKind: Int

        if (ShortDate.getYear(startDate) == ShortDate.getYear(endDate)) {
            if (ShortDate.getMonth(startDate) == ShortDate.getMonth(endDate)) {
                formatKind = FORMAT_KIND_DAY
                xValueFormatter = DayChartFormatter
            } else {
                formatKind = FORMAT_KIND_MONTH_AND_DAY
                xValueFormatter = DayMonthChartFormatter
            }
        } else {
            formatKind = FORMAT_KIND_DATE
            xValueFormatter = DateChartFormatter.INSTANCE
        }

        for (i in 0 until entriesSize) {
            val reportEntry = monthEntries[i]
            val date = reportEntry.date
            val minTemp =
                UnitValue.getValue(reportEntry.minTemperature, tempUnit, prefTempUnit)
            val maxTemp =
                UnitValue.getValue(reportEntry.maxTemperature, tempUnit, prefTempUnit)
            val minPress =
                UnitValue.getValue(reportEntry.minPressure, pressUnit, prefPressUnit)
            val maxPress =
                UnitValue.getValue(reportEntry.maxPressure, pressUnit, prefPressUnit)

            val x = when (formatKind) {
                FORMAT_KIND_DATE -> ShortDate.toEpochDay(date).toFloat()
                FORMAT_KIND_MONTH_AND_DAY -> (ShortDate.getMonth(date) * 31 + ShortDate.getDayOfMonth(date)).toFloat()
                else -> ShortDate.getDayOfMonth(date).toFloat()
            }

            minTempEntries[i] = Entry.of(x, minTemp)
            maxTempEntries[i] = Entry.of(x, maxTemp)
            minHumEntries[i] = Entry.of(x, reportEntry.minHumidity)
            maxHumEntries[i] = Entry.of(x, reportEntry.maxHumidity)
            minPressEntries[i] = Entry.of(x, minPress)
            maxPressEntries[i] = Entry.of(x, maxPress)
        }

        val tempFormatter = UnitChartValueFormatter(unitFormatter, prefTempUnit)
        val humFormatter = UnitChartValueFormatter(unitFormatter, ValueUnit.HUMIDITY)
        val pressFormatter = UnitChartValueFormatter(unitFormatter, prefPressUnit)

        val minTempDataSet = DataSet(minTempEntries, tempFormatter).customizeOptions(minColor)
        val maxTempDataSet = DataSet(maxTempEntries, tempFormatter).customizeOptions(maxColor)
        val minHumDataSet = DataSet(minHumEntries, humFormatter).customizeOptions(minColor)
        val maxHumDataSet = DataSet(maxHumEntries, humFormatter).customizeOptions(maxColor)
        val minPressDataSet = DataSet(minPressEntries, pressFormatter).customizeOptions(minColor)
        val maxPressDataSet = DataSet(maxPressEntries, pressFormatter).customizeOptions(maxColor)

        val tempData = ChartData(minTempDataSet, maxTempDataSet)
        val humData = ChartData(minHumDataSet, maxHumDataSet)
        val pressData = ChartData(minPressDataSet, maxPressDataSet)

        return createChartView(
            this,
            report.stats,
            tempData,
            humData,
            pressData
        ) { chart ->
            chart.xAxis.apply {
                valueFormatter = xValueFormatter
                granularity = 1f
            }
            chart.yAxis.apply {
                granularity = 1f
            }
        }
    }

    private fun DataSet.customizeOptions(color: Int): DataSet {
        this.color = color
        circleColor = color

        return this
    }

    private object DayChartFormatter : ValueFormatter {
        private val textCache = CharArray(2)

        override fun format(value: Float): String {
            val text = textCache

            val day = value.toInt()
            StringUtils.writeTwoDigits(text, 0, day)
            return String(text, 0, 2)
        }
    }

    private object DayMonthChartFormatter : ValueFormatter {
        private val textCache = CharArray(5)

        override fun format(value: Float): String {
            val text = textCache

            val month = (value / 31).toInt()
            val day = (value - month * 31).toInt()
            StringUtils.writeTwoDigits(text, 0, month)
            text[2] = '.'
            StringUtils.writeTwoDigits(text, 3, day)
            return String(text, 0, 5)
        }
    }

    companion object {
        private const val EXTRA_START_DATE = "WeekReportActivity:startDate"
        private const val EXTRA_END_DATE = "WeekReportActivity:endDate"

        private const val FORMAT_KIND_DATE = 0
        private const val FORMAT_KIND_MONTH_AND_DAY = 1
        private const val FORMAT_KIND_DAY = 2

        fun intent(context: Context, @ShortDateInt startDate: Int, @ShortDateInt endDate: Int): Intent {
            return Intent(context, WeekReportActivity::class.java) {
                putExtra(EXTRA_START_DATE, startDate)
                putExtra(EXTRA_END_DATE, endDate)
            }
        }
    }
}