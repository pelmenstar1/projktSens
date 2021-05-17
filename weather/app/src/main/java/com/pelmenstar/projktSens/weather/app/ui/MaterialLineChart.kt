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
import com.pelmenstar.projktSens.shared.android.obtainStyledAttributes

class MaterialLineChart @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0
) : LineChart(context, attrs, defStyleAttr, defStyleRes) {
    @ColorInt
    private val materialTextColor: Int

    init {
        val theme = context.theme
        val labelColor: Int

        theme.obtainStyledAttributes(android.R.style.Theme, android.R.attr.textColorPrimary) { a ->
            labelColor = a.getColor(0, Color.BLACK)
        }

        materialTextColor = labelColor

        flags = flags or FLAG_AUTO_ANIMATED
        xAxis.apply {
            textColor = labelColor
            position = XAxis.POSITION_BOTTOM
            setDrawGridLines(true)
        }

        yAxis.apply {
            textColor = labelColor
            position = YAxis.POSITION_LEFT
        }

        setTouchEnabled(false)
    }

    override fun setData(data: ChartData?) {
        super.setData(data)
        if (data == null) {
            return
        }
        for (dataSet in data.dataSets) {
            dataSet.valueTextSize = 15f
            dataSet.circleRadius = 6f
            dataSet.valueTextColor = materialTextColor
            dataSet.lineWidth = 1.5f
        }
    }


}