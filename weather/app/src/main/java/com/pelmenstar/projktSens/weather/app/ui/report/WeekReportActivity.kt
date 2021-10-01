package com.pelmenstar.projktSens.weather.app.ui.report

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.pelmenstar.projktSens.chartLite.GradientFill
import com.pelmenstar.projktSens.chartLite.data.ChartData
import com.pelmenstar.projktSens.chartLite.data.DataSet
import com.pelmenstar.projktSens.chartLite.data.Entry
import com.pelmenstar.projktSens.chartLite.formatter.ValueFormatter
import com.pelmenstar.projktSens.shared.StringUtils
import com.pelmenstar.projktSens.shared.android.charts.DateChartFormatter
import com.pelmenstar.projktSens.shared.android.ext.Intent
import com.pelmenstar.projktSens.shared.android.ext.withAlpha
import com.pelmenstar.projktSens.shared.android.ui.actionBar
import com.pelmenstar.projktSens.shared.android.ui.requireIntent
import com.pelmenstar.projktSens.shared.time.ShortDate
import com.pelmenstar.projktSens.shared.time.ShortDateInt
import com.pelmenstar.projktSens.shared.time.ShortDateRange
import com.pelmenstar.projktSens.weather.app.R
import com.pelmenstar.projktSens.weather.app.di.AppComponent
import com.pelmenstar.projktSens.weather.app.di.AppModule
import com.pelmenstar.projktSens.weather.app.di.DaggerAppComponent
import com.pelmenstar.projktSens.weather.app.formatters.UnitChartValueFormatter
import com.pelmenstar.projktSens.weather.models.*

class WeekReportActivity : ReportActivityBase<DayRangeReport>(DayRangeReport.SERIALIZER) {
    private var startDate = 0
    private var endDate = 0

    private lateinit var appComponent: AppComponent

    override fun onCreate(savedInstanceState: Bundle?) {
        val intent = requireIntent()

        startDate = intent.getIntExtra(EXTRA_START_DATE, ShortDate.NONE)
        endDate = intent.getIntExtra(EXTRA_END_DATE, ShortDate.NONE)

        appComponent = DaggerAppComponent
            .builder()
            .appModule(AppModule(this))
            .build()

        actionBar {
            val dateFormatter = appComponent.prettyDateFormatter()

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
        return dataSource.getDayRangeReport(startDate, endDate)
    }

    override fun createChartView(report: DayRangeReport): View {
        val resources = resources
        val theme = theme
        val minColor = ResourcesCompat.getColor(resources, R.color.chartMinColor, theme)
        val maxColor = ResourcesCompat.getColor(resources, R.color.chartMaxColor, theme)

        val ac = appComponent
        val unitFormatter = ac.unitFormatter()
        val prefs = ac.preferences()

        val prefUnits = prefs.units
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
                FORMAT_KIND_MONTH_AND_DAY -> (ShortDate.getMonth(date) * 31 + ShortDate.getDayOfMonth(
                    date
                )).toFloat()
                else -> ShortDate.getDayOfMonth(date).toFloat()
            }

            minTempEntries[i] = Entry.create(x, minTemp)
            maxTempEntries[i] = Entry.create(x, maxTemp)
            minHumEntries[i] = Entry.create(x, reportEntry.minHumidity)
            maxHumEntries[i] = Entry.create(x, reportEntry.maxHumidity)
            minPressEntries[i] = Entry.create(x, minPress)
            maxPressEntries[i] = Entry.create(x, maxPress)
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

        background = GradientFill(
            color.withAlpha(50), Color.TRANSPARENT, GradientFill.VERTICAL
        )

        return this
    }

    private object DayChartFormatter : ValueFormatter(true) {
        private val textCache = CharArray(2)

        override fun formatToCharArray(value: Float): CharArray {
            val text = textCache

            val day = value.toInt()
            StringUtils.writeTwoDigits(text, 0, day)

            return text
        }
    }

    private object DayMonthChartFormatter : ValueFormatter(true) {
        private val textCache = CharArray(5)

        override fun formatToCharArray(value: Float): CharArray {
            val text = textCache

            val monthDay = value.toInt()

            val month = monthDay / 31
            val day = monthDay - month * 31
            StringUtils.writeTwoDigits(text, 0, month)
            text[2] = '.'
            StringUtils.writeTwoDigits(text, 3, day)

            return text
        }
    }

    companion object {
        private const val EXTRA_START_DATE = "WeekReportActivity:startDate"
        private const val EXTRA_END_DATE = "WeekReportActivity:endDate"

        private const val FORMAT_KIND_DATE = 0
        private const val FORMAT_KIND_MONTH_AND_DAY = 1
        private const val FORMAT_KIND_DAY = 2

        fun intent(
            context: Context,
            @ShortDateInt startDate: Int,
            @ShortDateInt endDate: Int
        ): Intent {
            return Intent(context, WeekReportActivity::class.java) {
                putExtra(EXTRA_START_DATE, startDate)
                putExtra(EXTRA_END_DATE, endDate)
            }
        }
    }
}