package com.pelmenstar.projktSens.weather.app.di;

import android.content.Context;
import android.content.res.Resources;

import com.pelmenstar.projktSens.serverProtocol.HostedProtoConfig;
import com.pelmenstar.projktSens.serverProtocol.ProtoConfig;
import com.pelmenstar.projktSens.serverProtocol.ProtoConfigImpl;
import com.pelmenstar.projktSens.serverProtocol.repo.RepoContractType;
import com.pelmenstar.projktSens.shared.geo.ConstGeolocationProvider;
import com.pelmenstar.projktSens.shared.geo.GeolocationProvider;
import com.pelmenstar.projktSens.shared.time.PrettyDateFormatter;
import com.pelmenstar.projktSens.weather.app.NetworkDataSource;
import com.pelmenstar.projktSens.weather.app.NetworkWeatherChannelInfoProvider;
import com.pelmenstar.projktSens.weather.app.Preferences;
import com.pelmenstar.projktSens.weather.app.R;
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

import dagger.Module;
import dagger.Provides;

@Module
public final class AppModule {
    private final Context context;

    private final UnitFormatter unitFormatter;
    private final MoonPhaseFormatter moonPhaseFormatter;

    public AppModule(@NotNull Context context) {
        this.context = context;

        Resources res = context.getResources();

        String[] moonPhases = res.getStringArray(R.array.moonPhases);
        moonPhaseFormatter = new MoonPhaseFormatter(moonPhases);

        String[] units = res.getStringArray(R.array.units);
        unitFormatter = new UnitFormatter(units, prettyDateFormatter());
    }

    @Provides
    @NotNull
    public MoonInfoProvider moonInfoProvider() {
        return new AstroMoonInfoProvider();
    }

    @Provides
    @NotNull
    public MoonCalendarContract.Presenter moonCalendarPresenter() {
        return new MoonCalendarPresenter(moonInfoProvider());
    }

    @Provides
    @NotNull
    public HomeContract.Presenter homePresenter() {
        return new HomePresenter(
                sunInfoProvider(),
                moonInfoProvider(),
                geolocationProvider(),
                dataSource(),
                weatherChannelInfoProvider());
    }

    @Provides
    @NotNull
    public SunriseSunsetCalendarContract.Presenter sunriseSunsetCalendarPresenter() {
        return new SunriseSunsetCalendarPresenter(sunInfoProvider());
    }

    @Provides
    @NotNull
    public WeatherDataSource dataSource() {
        return new NetworkDataSource(hostedProtoConfig());
    }

    @Provides
    @NotNull
    public SunInfoProvider sunInfoProvider() {
        return new AstroSunInfoProvider();
    }

    @Provides
    @NotNull
    public GeolocationProvider geolocationProvider() {
        return new ConstGeolocationProvider(50.585661f, 26.31601f);
    }

    @Provides
    @NotNull
    public PrettyDateFormatter prettyDateFormatter() {
        return new ResourcesPrettyDateFormatter(context.getResources());
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
        return new NetworkWeatherChannelInfoProvider(hostedProtoConfig());
    }

    @Provides
    @NotNull
    public ProtoConfig protoConfig() {
        Preferences prefs = Preferences.of(context);
        int contractType = prefs.getContractType();
        return new ProtoConfigImpl(
                prefs.getRepoPort(),
                prefs.getWciPort(),
                prefs.getWeatherReceiveInterval(),
                RepoContractType.get(contractType)
        );
    }

    @NotNull
    private HostedProtoConfig hostedProtoConfig() {
        Preferences prefs = Preferences.of(context);

        return new HostedProtoConfig(prefs.getServerHost(), protoConfig());
    }
}
