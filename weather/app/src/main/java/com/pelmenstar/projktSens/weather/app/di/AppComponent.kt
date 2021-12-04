package com.pelmenstar.projktSens.weather.app.di

import com.pelmenstar.projktSens.serverProtocol.ProtoConfig
import com.pelmenstar.projktSens.shared.geo.GeolocationProvider
import com.pelmenstar.projktSens.shared.time.PrettyDateFormatter
import com.pelmenstar.projktSens.weather.app.AppPreferences
import com.pelmenstar.projktSens.weather.app.formatters.MoonPhaseFormatter
import com.pelmenstar.projktSens.weather.app.formatters.UnitFormatter
import com.pelmenstar.projktSens.weather.app.ui.firstStart.FirstStartContract
import com.pelmenstar.projktSens.weather.app.ui.home.HomeContract
import com.pelmenstar.projktSens.weather.app.ui.moon.MoonCalendarContract
import com.pelmenstar.projktSens.weather.app.ui.sunriseSunset.SunriseSunsetCalendarContract
import com.pelmenstar.projktSens.weather.app.astro.MoonInfoProvider
import com.pelmenstar.projktSens.weather.app.astro.SunInfoProvider
import com.pelmenstar.projktSens.weather.models.WeatherFlowDataSource
import dagger.Component

@Component(modules = [AppModule::class])
interface AppComponent {
    fun homePresenter(): HomeContract.Presenter
    fun sunriseSunsetCalendarPresenter(): SunriseSunsetCalendarContract.Presenter
    fun moonCalendarPresenter(): MoonCalendarContract.Presenter
    fun firstStartPresenter(): FirstStartContract.Presenter

    fun dataSource(): WeatherFlowDataSource

    fun sunInfoProvider(): SunInfoProvider
    fun moonInfoProvider(): MoonInfoProvider

    fun geolocationProvider(): GeolocationProvider

    fun prettyDateFormatter(): PrettyDateFormatter
    fun unitFormatter(): UnitFormatter
    fun moonPhaseFormatter(): MoonPhaseFormatter

    fun protoConfig(): ProtoConfig
    fun preferences(): AppPreferences
}