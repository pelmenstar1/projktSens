package com.pelmenstar.projktSens.weather.app.di

import android.content.Context
import com.pelmenstar.projktSens.weather.app.formatters.UnitFormatter
import com.pelmenstar.projktSens.weather.app.formatters.MoonPhaseFormatter
import com.pelmenstar.projktSens.weather.app.astro.MoonInfoProvider
import com.pelmenstar.projktSens.weather.app.astro.AstroMoonInfoProvider
import com.pelmenstar.projktSens.weather.app.ui.moon.MoonCalendarPresenter
import com.pelmenstar.projktSens.weather.app.ui.home.HomePresenter
import com.pelmenstar.projktSens.weather.app.ui.sunriseSunset.SunriseSunsetCalendarPresenter
import com.pelmenstar.projktSens.weather.app.astro.SunInfoProvider
import com.pelmenstar.projktSens.weather.app.astro.AstroSunInfoProvider
import com.pelmenstar.projktSens.shared.geo.GeolocationProvider
import com.pelmenstar.projktSens.shared.geo.ConstGeolocationProvider
import com.pelmenstar.projktSens.shared.time.PrettyDateFormatter
import com.pelmenstar.projktSens.weather.app.formatters.ResourcesPrettyDateFormatter
import com.pelmenstar.projktSens.serverProtocol.ProtoConfig
import com.pelmenstar.projktSens.shared.InetAddressUtils
import com.pelmenstar.projktSens.serverProtocol.ContractType
import com.pelmenstar.projktSens.weather.app.*
import com.pelmenstar.projktSens.weather.app.ui.home.HomeContract
import com.pelmenstar.projktSens.weather.app.ui.moon.MoonCalendarContract
import com.pelmenstar.projktSens.weather.app.ui.sunriseSunset.SunriseSunsetCalendarContract
import com.pelmenstar.projktSens.weather.models.RandomWeatherDataSource
import com.pelmenstar.projktSens.weather.models.WeatherFlowDataSource
import dagger.Module
import dagger.Provides
import java.net.InetSocketAddress

@Module
class AppModule(private val context: Context) {
    private val unitFormatter: UnitFormatter by lazy {
        UnitFormatter(context, prettyDateFormatter())
    }

    private val moonPhaseFormatter: MoonPhaseFormatter by lazy {
        MoonPhaseFormatter(context)
    }

    @Provides
    fun moonInfoProvider(): MoonInfoProvider {
        return AstroMoonInfoProvider()
    }

    @Provides
    fun moonCalendarPresenter(): MoonCalendarContract.Presenter {
        return MoonCalendarPresenter(moonInfoProvider())
    }

    @Provides
    fun homePresenter(): HomeContract.Presenter {
        val protoConfig = protoConfig()
        return HomePresenter(
            sunInfoProvider(),
            geolocationProvider(),
            dataSource(protoConfig),
            protoConfig
        )
    }

    @Provides
    fun sunriseSunsetCalendarPresenter(): SunriseSunsetCalendarContract.Presenter {
        return SunriseSunsetCalendarPresenter(sunInfoProvider())
    }

    @Provides
    fun dataSource(protoConfig: ProtoConfig): WeatherFlowDataSource {
        return NetworkDataSource(protoConfig)
    }

    @Provides
    fun sunInfoProvider(): SunInfoProvider {
        return AstroSunInfoProvider()
    }

    @Provides
    fun geolocationProvider(): GeolocationProvider {
        return ConstGeolocationProvider(50.4500f, 30.5233f)
    }

    @Provides
    fun prettyDateFormatter(): PrettyDateFormatter {
        return ResourcesPrettyDateFormatter(context.resources)
    }

    @Provides
    fun unitFormatter(): UnitFormatter {
        return unitFormatter
    }

    @Provides
    fun moonPhaseFormatter(): MoonPhaseFormatter {
        return moonPhaseFormatter
    }

    @Provides
    fun preferences(): AppPreferences {
        return AppPreferencesImpl.of(context)
    }

    @Provides
    fun protoConfig(): ProtoConfig {
        val prefs = preferences()
        val inetAddress = InetAddressUtils.parseInt(prefs.serverHostInt)

        return ProtoConfig(
            InetSocketAddress(inetAddress, prefs.serverPort),
            prefs.weatherReceiveInterval,
            ContractType.toObject(prefs.contractType)
        )
    }
}