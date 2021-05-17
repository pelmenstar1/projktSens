@file:JvmName("ReportViewHelper")
@file:Suppress("DEPRECATION")

package com.pelmenstar.projktSens.weather.app.ui.report

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import com.pelmenstar.projktSens.chartLite.LineChart
import com.pelmenstar.projktSens.chartLite.data.ChartData
import com.pelmenstar.projktSens.shared.android.ui.*
import com.pelmenstar.projktSens.weather.app.PreferredUnits
import com.pelmenstar.projktSens.weather.app.R
import com.pelmenstar.projktSens.weather.app.di.DaggerAppComponent
import com.pelmenstar.projktSens.weather.app.ui.MaterialChart
import com.pelmenstar.projktSens.weather.models.ReportStats
import com.pelmenstar.projktSens.weather.models.UnitValue
import com.pelmenstar.projktSens.weather.models.ValueUnit
import com.pelmenstar.projktSens.weather.models.ValueUnitsPacked

fun createChartView(
    context: Context,
    stats: ReportStats,
    temperatureData: ChartData,
    humidityData: ChartData,
    pressureData: ChartData,
    chartOptions: (LineChart) -> Unit
): View {
    val component = DaggerAppComponent.create()
    val unitFormatter = component.unitFormatter()

    val headerDefUnits = stats.units
    val headerTempUnit = ValueUnitsPacked.getTemperatureUnit(headerDefUnits)
    val headerPressUnit = ValueUnitsPacked.getPressureUnit(headerDefUnits)

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

    val minStr = res.getString(R.string.min)
    val maxStr = res.getString(R.string.max)
    val avgStr = res.getString(R.string.avg)

    return ScrollView(context) {
        LinearLayout {
            linearLayoutParams(MATCH_PARENT, WRAP_CONTENT)

            orientation = LinearLayout.VERTICAL

            // temperature
            TextView {
                linearLayoutParams(WRAP_CONTENT, WRAP_CONTENT) {
                    gravity = Gravity.CENTER_HORIZONTAL
                }

                text = res.getText(R.string.temperature)
                applyTextAppearance(headline4)
            }

            PrefixTextView {
                linearLayoutParams(WRAP_CONTENT, WRAP_CONTENT) {
                    leftMargin = textLeftMargin
                }

                prefix = minStr
                value = unitFormatter.formatValue(
                    UnitValue.getValue(
                        stats.temperature.min,
                        headerTempUnit, prefTempUnit
                    ),
                    prefTempUnit
                )

                applyTextAppearance(body1)
            }

            PrefixTextView {
                linearLayoutParams (WRAP_CONTENT, WRAP_CONTENT) {
                    leftMargin = textLeftMargin
                }

                prefix = maxStr
                value = unitFormatter.formatValue(
                    UnitValue.getValue(stats.temperature.max,
                        headerTempUnit,
                        prefTempUnit
                    ),
                    prefTempUnit
                )

                applyTextAppearance(body1)
            }

            PrefixTextView {
                linearLayoutParams(WRAP_CONTENT, WRAP_CONTENT) {
                    leftMargin = textLeftMargin
                }

                prefix = avgStr
                value = unitFormatter.formatValue(
                    UnitValue.getValue(stats.temperature.avg, headerTempUnit, prefTempUnit),
                    prefTempUnit
                )

                applyTextAppearance(body1)
            }

            MaterialChart {
                linearLayoutParams(MATCH_PARENT, chartHeight) {
                    leftMargin = chartSideMargin
                    rightMargin = chartSideMargin
                }

                chartOptions(this)
                data = temperatureData
            }

            // humidity
            TextView {
                linearLayoutParams (WRAP_CONTENT, WRAP_CONTENT) {
                    gravity = Gravity.CENTER_HORIZONTAL
                    topMargin = blockTopMargin
                }

                text = res.getText(R.string.humidity)

                applyTextAppearance(headline4)
            }

            PrefixTextView {
                linearLayoutParams(WRAP_CONTENT, WRAP_CONTENT) {
                    leftMargin = textLeftMargin
                }

                prefix = minStr
                value = unitFormatter.formatValue(stats.humidity.min, ValueUnit.HUMIDITY)

                applyTextAppearance(body1)
            }

            PrefixTextView {
                linearLayoutParams (WRAP_CONTENT, WRAP_CONTENT) {
                    leftMargin = textLeftMargin
                }

                prefix = maxStr
                value = unitFormatter.formatValue(stats.humidity.max, ValueUnit.HUMIDITY)

                applyTextAppearance(body1)
            }

            PrefixTextView {
                linearLayoutParams (WRAP_CONTENT, WRAP_CONTENT) {
                    leftMargin = textLeftMargin
                }

                prefix = avgStr
                value = unitFormatter.formatValue(stats.humidity.avg, ValueUnit.HUMIDITY)

                applyTextAppearance(body1)
            }

            MaterialChart {
                linearLayoutParams (MATCH_PARENT, chartHeight) {
                    leftMargin = chartSideMargin
                    rightMargin = chartSideMargin
                }

                chartOptions(this)
                data = humidityData
            }

            // pressure
            TextView {
                linearLayoutParams (WRAP_CONTENT, WRAP_CONTENT) {
                    gravity = Gravity.CENTER_HORIZONTAL
                    topMargin = blockTopMargin
                }

                text = res.getText(R.string.pressure)
                applyTextAppearance(headline4)
            }

            PrefixTextView {
                linearLayoutParams (WRAP_CONTENT, WRAP_CONTENT) {
                    leftMargin = textLeftMargin
                }

                prefix = minStr
                value = unitFormatter.formatValue(
                    UnitValue.getValue(
                        stats.pressure.min,
                        headerPressUnit, prefPressUnit
                    ),
                    prefPressUnit
                )

                applyTextAppearance(body1)
            }

            PrefixTextView {
                linearLayoutParams (WRAP_CONTENT, WRAP_CONTENT) {
                    leftMargin = textLeftMargin
                }

                prefix = maxStr
                value = unitFormatter.formatValue(
                    UnitValue.getValue(
                        stats.pressure.max,
                        headerPressUnit, prefPressUnit
                    ),
                    prefPressUnit
                )

                applyTextAppearance(body1)
            }

            PrefixTextView {
                linearLayoutParams (WRAP_CONTENT, WRAP_CONTENT) {
                    leftMargin = textLeftMargin
                }

                prefix = avgStr
                value = unitFormatter.formatValue(
                    UnitValue.getValue(stats.pressure.avg, headerPressUnit, prefPressUnit),
                    prefPressUnit
                )

                applyTextAppearance(body1)
            }

            MaterialChart {
                linearLayoutParams (MATCH_PARENT, chartHeight) {
                    leftMargin = chartSideMargin
                    rightMargin = chartSideMargin
                    bottomMargin = textLeftMargin
                }

                chartOptions(this)
                data = pressureData
            }
        }
    }
}

