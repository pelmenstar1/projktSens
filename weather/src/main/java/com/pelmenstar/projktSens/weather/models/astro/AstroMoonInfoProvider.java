package com.pelmenstar.projktSens.weather.models.astro;

import com.pelmenstar.projktSens.shared.time.ShortDate;
import com.pelmenstar.projktSens.shared.time.ShortDateInt;

public final class AstroMoonInfoProvider implements MoonInfoProvider {
    private static final float D2R = (float)(Math.PI / 180.0);

    private static final float EPOCH = 2444238.5f;
    private static final float ELONGE = 278.833540f;
    private static final float ELONGP = 282.596403f;
    private static final float ECCENT = 0.016718f;
    private static final float MMLONG = 64.975464f;
    private static final float MMLONGP = 349.383063f;

    private static float fixAngle(float a) {
        return a - (360.0f * (float)Math.floor(a / 360.0f));
    }

    private static float kepler(float m) {
        float e, delta;

        e = m = D2R * m;

        do {
            delta = e - ECCENT * (float)Math.sin(e) - m;
            e -= delta / (1.0f - ECCENT * (float)Math.cos(e));
        } while (Math.abs(delta) > 0.000001f);

        return e;
    }

    @Override
    public float getMoonPhase(@ShortDateInt int date) {
        if(!ShortDate.isValid(date)) {
            throw new IllegalArgumentException("date");
        }

        float dayF = ShortDate.toJulianDate(date) - EPOCH;

        float n = fixAngle((360.0f / 365.2422f) * dayF);
        float m = fixAngle(n + ELONGE - ELONGP);
        float sinM = (float)Math.sin(m * D2R);

        float ec = kepler(m);
        ec = 1.01686011182f * (float)Math.tan(ec * 0.5f);
        ec = 2f * D2R * (float)Math.atan(ec);

        float lambdaSun = fixAngle(ec + ELONGP);
        float ml = fixAngle(13.1763966f * dayF + MMLONG);
        float mm = fixAngle(ml - 0.1114041f * dayF - MMLONGP);
        float ev = 1.2739f * (float)Math.sin(D2R * (2f * (ml - lambdaSun) - mm));

        float ae = 0.1858f * sinM;
        float a3 = 0.37f * sinM;

        float mmp = mm + ev - ae - a3;
        float mEc = 6.2886f * (float)Math.sin(D2R * mmp);
        float a4 = 0.214f * (float)Math.sin(D2R * 2f * mmp);
        float lP = ml + ev + mEc - ae + a4;
        float v = 0.6583f * (float)Math.sin(2f * D2R * (lP - lambdaSun));
        float lPP = lP + v;

        float moonAge = lPP - lambdaSun;

        return (1.0f - (float)Math.cos(moonAge * D2R)) * 0.5f;
    }
}
