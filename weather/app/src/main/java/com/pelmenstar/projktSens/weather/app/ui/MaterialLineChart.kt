package com.pelmenstar.projktSens.weather.app.ui

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes
import com.pelmenstar.projktSens.chartLite.LineChart
import com.pelmenstar.projktSens.chartLite.components.XAxis
import com.pelmenstar.projktSens.chartLite.components.YAxis
import com.pelmenstar.projktSens.chartLite.data.ChartData
import com.pelmenstar.projktSens.shared.android.ext.obtainStyledAttributes
import com.pelmenstar.projktSens.weather.app.R

class MaterialLineChart @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0
) : LineChart(context, attrs, defStyleAttr, defStyleRes) {
    @ColorInt
    private val materialTextColor: Int

    private val valueTextSize: Float
    private val circleRadius: Float
    private val lineWidth: Float

    init {
        val theme = context.theme
        val res = context.resources
        val labelColor: Int

        theme.obtainStyledAttributes(android.R.style.Theme, android.R.attr.textColorPrimary) { a ->
            labelColor = a.getColor(0, Color.BLACK)
        }

        materialTextColor = labelColor

        valueTextSize = res.getDimension(R.dimen.materialChart_textSize)
        circleRadius = res.getDimension(R.dimen.materialChart_circleRadius)
        lineWidth = res.getDimension(R.dimen.materialChart_lineWidth)

        isAutoAnimated = true
        xAxis.apply {
            textColor = labelColor
            position = XAxis.POSITION_BOTTOM
            setDrawGridLines(true)
        }

        yAxis.apply {
            textColor = labelColor
            position = YAxis.POSITION_LEFT
        }
    }

    override fun setData(data: ChartData?) {
        super.setData(data)
        if (data == null) {
            return
        }

        for (dataSet in data.dataSets) {
            dataSet.valueTextSize = valueTextSize
            dataSet.circleRadius = circleRadius
            dataSet.valueTextColor = materialTextColor
            dataSet.lineWidth = lineWidth
        }
    }
}