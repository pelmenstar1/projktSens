package com.pelmenstar.projktSens.weather.app.di;

import android.content.Context;

import com.pelmenstar.projktSens.serverProtocol.ContractType;
import com.pelmenstar.projktSens.serverProtocol.ProtoConfig;
import com.pelmenstar.projktSens.serverProtocol.ProtoConfigImpl;
import com.pelmenstar.projktSens.shared.InetAddressUtils;
import com.pelmenstar.projktSens.shared.geo.ConstGeolocationProvider;
import com.pelmenstar.projktSens.shared.geo.GeolocationProvider;
import com.pelmenstar.projktSens.shared.time.PrettyDateFormatter;
import com.pelmenstar.projktSens.weather.app.AppPreferences;
import com.pelmenstar.projktSens.weather.app.AppPreferencesImpl;
import com.pelmenstar.projktSens.weather.app.NetworkDataSource;
import com.pelmenstar.projktSens.weather.app.NetworkWeatherChannelInfoProvider;
import com.pelmenstar.projktSens.weather.app.R;
import com.pelmenstar.projktSens.weather.app.formatters.MoonPhaseFormatter;
import com.pelmenstar.projktSens.weather.app.formatters.ResourcesPrettyDateFormatter;
import com.pelmenstar.projktSens.weather.app.formatters.UnitFormatter;
import com.pelmenstar.projktSens.weather.app.ui.firstStart.FirstStartContract;
import com.pelmenstar.projktSens.weather.app.ui.firstStart.FirstStartPresenter;
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
import java.net.InetSocketAddress;

import dagger.Module;
import dagger.Provides;

@Module
public final class AppModule {
    private final Context context;

    private UnitFormatter unitFormatter;
    private MoonPhaseFormatter moonPhaseFormatter;

    public AppModule(@NotNull Context context) {
        this.context = context;
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
    public FirstStartContract.Presenter firstStartPresenter() {
        return new FirstStartPresenter(preferences());
    }

    @Provides
    @NotNull
    public WeatherDataSource dataSource() {
        return new NetworkDataSource(protoConfig());
    }

    @Provides
    @NotNull
    public SunInfoProvider sunInfoProvider() {
        return new AstroSunInfoProvider();
    }

    @Provides
    @NotNull
    public GeolocationProvider geolocationProvider() {
        return new ConstGeolocationProvider(50.4500f, 30.5233f);
    }

    @Provides
    @NotNull
    public PrettyDateFormatter prettyDateFormatter() {
        return new ResourcesPrettyDateFormatter(context.getResources());
    }

    @Provides
    @NotNull
    public UnitFormatter unitFormatter() {
        if (unitFormatter == null) {
            String[] units = context.getResources().getStringArray(R.array.units);
            unitFormatter = new UnitFormatter(units, prettyDateFormatter());
        }

        return unitFormatter;
    }

    @Provides
    @NotNull
    public MoonPhaseFormatter moonPhaseFormatter() {
        if (moonPhaseFormatter == null) {
            String[] moonPhases = context.getResources().getStringArray(R.array.moonPhases);
            moonPhaseFormatter = new MoonPhaseFormatter(moonPhases);
        }

        return moonPhaseFormatter;
    }

    @Provides
    @NotNull
    public WeatherChannelInfoProvider weatherChannelInfoProvider() {
        return new NetworkWeatherChannelInfoProvider(protoConfig());
    }

    @NotNull
    @Provides
    public AppPreferences preferences() {
        return AppPreferencesImpl.of(context);
    }

    @Provides
    @NotNull
    public ProtoConfig protoConfig() {
        AppPreferences prefs = preferences();
        int contractType = prefs.getContractType();
        InetAddress inetAddress = InetAddressUtils.parseInt(prefs.getServerHostInt());

        return new ProtoConfigImpl(
                new InetSocketAddress(inetAddress, prefs.getServerPort()),
                prefs.getWeatherReceiveInterval(),
                ContractType.toObject(contractType)
        );
    }
}
