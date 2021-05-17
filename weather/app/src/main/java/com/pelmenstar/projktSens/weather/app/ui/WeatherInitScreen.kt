package com.pelmenstar.projktSens.weather.app.ui

import android.os.Bundle
import com.pelmenstar.projktSens.shared.android.ui.initScreen.InitContext
import com.pelmenstar.projktSens.shared.android.ui.initScreen.InitScreen
import com.pelmenstar.projktSens.weather.app.R

class WeatherInitScreen(initContext: InitContext) : InitScreen(initContext) {
    override fun onCreate(savedInstanceState: Bundle?) {
        setStyle(STYLE_NORMAL, R.style.AppTheme_Dialog)
        super.onCreate(savedInstanceState)
    }
}