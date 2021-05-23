package com.pelmenstar.projktSens.weather.app.di;

import android.content.Context;
import android.content.res.Resources;

import com.pelmenstar.projktSens.serverProtocol.AvailabilityProvider;
import com.pelmenstar.projktSens.serverProtocol.DefaultProtoConfig;
import com.pelmenstar.projktSens.serverProtocol.HostedProtoConfig;
import com.pelmenstar.projktSens.serverProtocol.ProtoConfig;
import com.pelmenstar.projktSens.shared.geo.ConstGeolocationProvider;
import com.pelmenstar.projktSens.shared.geo.GeolocationProvider;
import com.pelmenstar.projktSens.shared.time.PrettyDateFormatter;
import com.pelmenstar.projktSens.weather.app.LocalHostProtoHostResolver;
import com.pelmenstar.projktSens.weather.app.NetworkDataSource;
import com.pelmenstar.projktSens.weather.app.NetworkWeatherChannelInfoProvider;
import com.pelmenstar.projktSens.weather.app.ProtoHostResolver;
import com.pelmenstar.projktSens.weather.app.R;
import com.pelmenstar.projktSens.weather.app.ServerAvailabilityProvider;
import com.pelmenstar.projktSens.weather.app.formatters.MoonPhaseFormatter;
import com.pelmenstar.projktSens.weather.app.formatters.ResourcesPrettyDateFormatter;
import com.pelmenstar.projktSens.weather.app.formatters.UnitFormatter;
import com.pelmenstar.projktSens.weather.app.ui.home.HomeContract;
import com.pelmenstar.projktSens.weather.app.ui.home.HomePresenter;
import com.pelmenstar.projktSens.weather.app.ui.moon.MoonCalendarContract;
import com.pelmenstar.projktSens.weather.app.ui.moon.MoonCalendarPresenter;
import com.pelmenstar.projktSens.weather.app.ui.sunriseSunset.SunriseSunsetCalendarContract;
import com.pelmenstar.projktSens.weather.app.ui.sunriseSunset.SunriseSunsetCalendarPresenter;
import com.pelmenstar.projktSens.weather.models.WeatherChannelInfoProvider;
import com.pelmenstar.projktSens.weather.models.WeatherDataSource;
import com.pelmenstar.projktSens.weather.models.astro.AstroMoonInfoProvider;
import com.pelmenstar.projktSens.weather.models.astro.AstroSunInfoProvider;
import com.pelmenstar.projktSens.weather.models.astro.MoonInfoProvider;
import com.pelmenstar.projktSens.weather.models.astro.SunInfoProvider;

import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;

import dagger.Module;
import dagger.Provides;

@Module
public final class AppModule {
    private static WeatherDataSource dataSource;
    private static final SunInfoProvider sunInfoProvider = new AstroSunInfoProvider();
    private static final MoonInfoProvider moonInfoProvider = new AstroMoonInfoProvider();

    // 50.58561 26.31601
    private static final GeolocationProvider geoProvider = new ConstGeolocationProvider(50.585661f, 26.31601f);
    private static WeatherChannelInfoProvider weatherChannelInfoProvider;
    private static AvailabilityProvider serverAvailabilityProvider;

    private static PrettyDateFormatter prettyDateFormatter;
    private static UnitFormatter unitFormatter;
    private static MoonPhaseFormatter moonPhaseFormatter;

    public static void initContext(@NotNull Context appContext) {
        Resources resources = appContext.getResources();

        prettyDateFormatter = new ResourcesPrettyDateFormatter(resources);
        unitFormatter = new UnitFormatter(resources.getStringArray(R.array.units), prettyDateFormatter);
        moonPhaseFormatter = new MoonPhaseFormatter(resources.getStringArray(R.array.moonPhases));

        ProtoHostResolver hostResolver = LocalHostProtoHostResolver.INSTANCE;
        InetAddress host = hostResolver.getHost();
        HostedProtoConfig hostedProtoConfig = new HostedProtoConfig(host, DefaultProtoConfig.INSTANCE);

        dataSource = new NetworkDataSource(hostedProtoConfig);
        weatherChannelInfoProvider = new NetworkWeatherChannelInfoProvider(hostedProtoConfig);
        serverAvailabilityProvider = new ServerAvailabilityProvider(hostedProtoConfig);
    }

    @Provides
    @NotNull
    public MoonInfoProvider moonInfoProvider() {
        return moonInfoProvider;
    }

    @Provides
    @NotNull
    public MoonCalendarContract.Presenter moonCalendarPresenter() {
        return new MoonCalendarPresenter(moonInfoProvider);
    }

    @Provides
    @NotNull
    public HomeContract.Presenter homePresenter() {
        return new HomePresenter(
                sunInfoProvider,
                moonInfoProvider,
                geoProvider,
                dataSource,
                weatherChannelInfoProvider,
                serverAvailabilityProvider);
    }

    @Provides
    @NotNull
    public SunriseSunsetCalendarContract.Presenter sunriseSunsetCalendarPresenter() {
        return new SunriseSunsetCalendarPresenter(sunInfoProvider);
    }

    @Provides
    @NotNull
    public WeatherDataSource dataSource() {
        return dataSource;
    }

    @Provides
    @NotNull
    public SunInfoProvider sunInfoProvider() {
        return sunInfoProvider;
    }

    @Provides
    @NotNull
    public GeolocationProvider geolocationProvider() {
        return geoProvider;
    }

    @Provides
    @NotNull
    public PrettyDateFormatter prettyDateFormatter() {
        return prettyDateFormatter;
    }

    @Provides
    @NotNull
    public UnitFormatter unitFormatter() {
        return unitFormatter;
    }

    @Provides
    @NotNull
    public MoonPhaseFormatter moonPhaseFormatter() {
        return moonPhaseFormatter;
    }

    @Provides
    @NotNull
    public WeatherChannelInfoProvider weatherChannelInfoProvider() {
        return weatherChannelInfoProvider;
    }

    @Provides
    @NotNull
    public ProtoConfig protoConfig() {
        return DefaultProtoConfig.INSTANCE;
    }

    @Provides
    @NotNull
    public ProtoHostResolver protoHostResolver() {
        return LocalHostProtoHostResolver.INSTANCE;
    }
}
