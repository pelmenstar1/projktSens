@file:Suppress("FunctionName")

package com.pelmenstar.projktSens.weather.app.ui

import android.view.ViewGroup
import com.pelmenstar.projktSens.shared.android.ui.addApply
import com.pelmenstar.projktSens.weather.app.ui.home.LazyLoadingCalendarView
import com.pelmenstar.projktSens.weather.app.ui.home.weatherView.ComplexWeatherView
import com.pelmenstar.projktSens.weather.app.ui.moon.MoonView
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

inline fun ViewGroup.ComplexWeatherView(block: ComplexWeatherView.() -> Unit): ComplexWeatherView {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return addApply(ComplexWeatherView(context), block)
}

inline fun ViewGroup.MaterialChart(block: MaterialLineChart.() -> Unit): MaterialLineChart {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return addApply(MaterialLineChart(context), block)
}

inline fun ViewGroup.MoonView(block: MoonView.() -> Unit): MoonView {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return addApply(MoonView(context), block)
}

inline fun ViewGroup.LazyLoadingCalendarView(block: LazyLoadingCalendarView.() -> Unit): LazyLoadingCalendarView {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return addApply(LazyLoadingCalendarView(context), block)
}