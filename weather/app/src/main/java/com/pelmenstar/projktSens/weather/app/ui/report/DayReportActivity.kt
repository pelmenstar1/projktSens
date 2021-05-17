package com.pelmenstar.projktSens.weather.app.ui.report

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.pelmenstar.projktSens.chartLite.LineChart
import com.pelmenstar.projktSens.chartLite.data.ChartData
import com.pelmenstar.projktSens.chartLite.data.DataSet
import com.pelmenstar.projktSens.chartLite.data.Entry
import com.pelmenstar.projktSens.shared.android.Intent
import com.pelmenstar.projktSens.shared.android.charts.TimeChartFormatter
import com.pelmenstar.projktSens.shared.android.ui.actionBar
import com.pelmenstar.projktSens.shared.android.ui.requireIntent
import com.pelmenstar.projktSens.shared.time.ShortDateInt
import com.pelmenstar.projktSens.shared.time.TimeConstants
import com.pelmenstar.projktSens.weather.app.PreferredUnits
import com.pelmenstar.projktSens.weather.app.R
import com.pelmenstar.projktSens.weather.app.di.DaggerAppComponent
import com.pelmenstar.projktSens.weather.app.formatters.UnitChartValueFormatter
import com.pelmenstar.projktSens.weather.models.*

class DayReportActivity : ReportActivityBase<DayReport>(DayReport.SERIALIZER) {
    @ShortDateInt
    private var date = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        val intent = requireIntent()
        date = intent.getIntExtra(EXTRA_DATE, 0)

        actionBar {
            val component = DaggerAppComponent.create()

            setDisplayHomeAsUpEnabled(true)
            title = component.prettyDateFormatter().prettyFormat(date)
        }

        super.onCreate(savedInstanceState)
    }

    override suspend fun loadReport(dataSource: WeatherDataSource): DayReport? {
        return dataSource.getDayReport(date)
    }

    override fun createChartView(report: DayReport): View {
        val theme = theme
        val res = resources

        val colorPrimary = ResourcesCompat.getColor(res, R.color.colorPrimary, theme)
        val minColor = ResourcesCompat.getColor(res, R.color.chartMinColor, theme)
        val maxColor = ResourcesCompat.getColor(res, R.color.chartMaxColor, theme)

        val unitFormatter = DaggerAppComponent.create().unitFormatter()

        val prefUnits = PreferredUnits.getUnits()
        val prefTempUnit = ValueUnitsPacked.getTemperatureUnit(prefUnits)
        val prefPressUnit = ValueUnitsPacked.getPressureUnit(prefUnits)

        val units = report.stats.units
        val tempUnit = ValueUnitsPacked.getTemperatureUnit(units)
        val pressUnit = ValueUnitsPacked.getPressureUnit(units)

        val dayEntries = report.entries
        val entriesSize = dayEntries.size
        val tempEntries = LongArray(entriesSize)
        val humEntries = LongArray(entriesSize)
        val pressEntries = LongArray(entriesSize)

        for (i in 0 until entriesSize) {
            val dayEntry = dayEntries[i]

            val time = dayEntry.time
            val temp = UnitValue.getValue(dayEntry.temperature, tempUnit, prefTempUnit).toFloat()
            val press = UnitValue.getValue(dayEntry.pressure, pressUnit, prefPressUnit).toFloat()
            val x = time.toFloat()

            tempEntries[i] = Entry.of(x, temp)
            humEntries[i] = Entry.of(x, dayEntry.humidity.toFloat())
            pressEntries[i] = Entry.of(x, press)
        }

        val tempDataSet = DataSet(tempEntries, UnitChartValueFormatter(unitFormatter, prefTempUnit)).apply {
            circleColor = colorPrimary
            color = colorPrimary

            yMinColor = minColor
            yMaxColor = maxColor
        }

        val humDataSet = DataSet(humEntries, UnitChartValueFormatter(unitFormatter, ValueUnit.HUMIDITY)).apply {
            circleColor = colorPrimary
            color = colorPrimary

            yMinColor = minColor
            yMaxColor = maxColor
        }

        val pressDataSet = DataSet(pressEntries, UnitChartValueFormatter(unitFormatter, prefPressUnit)).apply {
            circleColor = colorPrimary
            color = colorPrimary

            yMinColor = minColor
            yMaxColor = maxColor
        }

        val tempData = ChartData(tempDataSet)
        val humData = ChartData(humDataSet)
        val pressData = ChartData(pressDataSet)

        return createChartView(this, report.stats, tempData, humData, pressData, CHART_OPTIONS)
    }

    companion object {
        private val CHART_OPTIONS: (LineChart) -> Unit = { chart ->
            chart.xAxis.apply {
                axisMinimum = 0f
                axisMaximum = (TimeConstants.SECONDS_IN_DAY - 1).toFloat()
                granularity = 60f
                valueFormatter = TimeChartFormatter.INSTANCE
            }

            chart.yAxis.granularity = 1f
        }

        private const val EXTRA_DATE = "DayReportActivity:date"

        fun intent(context: Context, @ShortDateInt date: Int): Intent {
            return Intent(context, DayReportActivity::class.java) {
                putExtra(EXTRA_DATE, date)
            }
        }
    }
}