@file:Suppress("FunctionName")

package com.pelmenstar.projktSens.weather.app.ui

import android.view.ViewGroup
import com.pelmenstar.projktSens.shared.android.ui.addApply
import com.pelmenstar.projktSens.weather.app.ui.home.weatherView.ComplexWeatherView
import com.pelmenstar.projktSens.weather.app.ui.moon.MoonView
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

inline fun ViewGroup.ComplexWeatherView(block: ComplexWeatherView.() -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    addApply(
        ComplexWeatherView(
            context
        ), block)
}

inline fun ViewGroup.MaterialChart(block: MaterialLineChart.() -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    addApply(MaterialLineChart(context), block)
}

inline fun ViewGroup.MoonView(block: MoonView.() -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    addApply(MoonView(context), block)
}