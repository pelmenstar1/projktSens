package com.pelmenstar.projktSens.weather.app.ui.report

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.pelmenstar.projktSens.chartLite.data.ChartData
import com.pelmenstar.projktSens.chartLite.data.DataSet
import com.pelmenstar.projktSens.chartLite.data.Entry
import com.pelmenstar.projktSens.chartLite.formatter.IntValueFormatter
import com.pelmenstar.projktSens.shared.android.Intent
import com.pelmenstar.projktSens.shared.android.ui.actionBar
import com.pelmenstar.projktSens.shared.android.ui.requireIntent
import com.pelmenstar.projktSens.shared.time.ShortDate
import com.pelmenstar.projktSens.shared.time.ShortDateRange
import com.pelmenstar.projktSens.shared.time.TimeUtils
import com.pelmenstar.projktSens.weather.app.PreferredUnits
import com.pelmenstar.projktSens.weather.app.R
import com.pelmenstar.projktSens.weather.app.di.DaggerAppComponent
import com.pelmenstar.projktSens.weather.app.formatters.UnitChartValueFormatter
import com.pelmenstar.projktSens.weather.models.*

class MonthReportActivity : ReportActivityBase<DayRangeReport>(DayRangeReport.SERIALIZER) {
    private var year = 0
    private var month = 0

    private var startDate = 0
    private var endDate = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        val intent = requireIntent()

        val yearMonth = intent.getIntExtra(EXTRA_YEAR_MONTH, 0)
        year = yearMonth / 12
        month = yearMonth - year * 12

        startDate = ShortDate.of(year, month, 1)
        endDate = ShortDate.of(year, month, TimeUtils.getDaysInMonth(year, month))

        actionBar {
            val component = DaggerAppComponent.create()
            val dateFormatter = component.prettyDateFormatter()

            setDisplayHomeAsUpEnabled(true)
            title = dateFormatter.prettyFormat(year, month)
        }

        super.onCreate(savedInstanceState)
    }

    override suspend fun loadReport(dataSource: WeatherDataSource): DayRangeReport? {
        return dataSource.getDayRangeReport(ShortDateRange(startDate, endDate))
    }

    override fun createChartView(report: DayRangeReport): View {
        val res = resources
        val theme = theme

        val minColor = ResourcesCompat.getColor(res, R.color.chartMinColor, theme)
        val maxColor = ResourcesCompat.getColor(res, R.color.chartMaxColor, theme)
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

        for (i in 0 until entriesSize) {
            val reportEntry = monthEntries[i]
            val minTemp =
                UnitValue.getValue(reportEntry.minTemperature, tempUnit, prefTempUnit).toFloat()
            val maxTemp =
                UnitValue.getValue(reportEntry.maxTemperature, tempUnit, prefTempUnit).toFloat()
            val minPress =
                UnitValue.getValue(reportEntry.minPressure, pressUnit, prefPressUnit).toFloat()
            val maxPress =
                UnitValue.getValue(reportEntry.maxPressure, pressUnit, prefPressUnit).toFloat()
            val x = ShortDate.getDayOfMonth(reportEntry.date).toFloat()

            minTempEntries[i] = Entry.of(x, minTemp)
            maxTempEntries[i] = Entry.of(x, maxTemp)
            minHumEntries[i] = Entry.of(x, reportEntry.minHumidity.toFloat())
            maxHumEntries[i] = Entry.of(x, reportEntry.maxHumidity.toFloat())
            minPressEntries[i] = Entry.of(x, minPress)
            maxPressEntries[i] = Entry.of(x, maxPress)
        }

        val tempFormatter = UnitChartValueFormatter(unitFormatter, prefTempUnit)
        val humFormatter = UnitChartValueFormatter(unitFormatter, ValueUnit.HUMIDITY)
        val pressFormatter = UnitChartValueFormatter(unitFormatter, prefPressUnit)

        val minTempDataSet = DataSet(minTempEntries, tempFormatter)
        val maxTempDataSet = DataSet(maxTempEntries, tempFormatter)
        val minHumDataSet = DataSet(minHumEntries, humFormatter)
        val maxHumDataSet = DataSet(maxHumEntries, humFormatter)
        val minPressDataSet = DataSet(minPressEntries, pressFormatter)
        val maxPressDataSet = DataSet(maxPressEntries, pressFormatter)

        minTempDataSet.color = minColor
        minTempDataSet.circleColor = minColor
        minTempDataSet.setDrawValues(false)

        maxTempDataSet.color = maxColor
        maxTempDataSet.circleColor = maxColor
        maxTempDataSet.setDrawValues(false)

        minHumDataSet.color = minColor
        minHumDataSet.circleColor = minColor
        minHumDataSet.setDrawValues(false)

        maxHumDataSet.color = maxColor
        maxHumDataSet.circleColor = maxColor
        maxHumDataSet.setDrawValues(false)

        minPressDataSet.color = minColor
        minPressDataSet.circleColor = minColor
        minPressDataSet.setDrawValues(false)

        maxPressDataSet.color = maxColor
        maxPressDataSet.circleColor = maxColor
        maxPressDataSet.setDrawValues(false)

        val tempData = ChartData(minTempDataSet, maxTempDataSet)
        val humData = ChartData(minHumDataSet, maxHumDataSet)
        val pressData = ChartData(minPressDataSet, maxPressDataSet)

        val daysInMonth = TimeUtils.getDaysInMonth(year, month).toFloat()
        return createChartView(this, report.stats, tempData, humData, pressData) { chart ->
            chart.xAxis.apply {
                axisMinimum = 1f
                axisMaximum = daysInMonth
                valueFormatter = IntValueFormatter.INSTANCE
            }

            chart.yAxis.granularity = 1f
        }
    }

    companion object {
        private const val EXTRA_YEAR_MONTH = "MonthReportActivity:yearMonth"

        fun intent(context: Context, year: Int, month: Int): Intent {
            return Intent(context, MonthReportActivity::class.java) {
                putExtra(EXTRA_YEAR_MONTH, year * 12 + month)
            }
        }
    }
}