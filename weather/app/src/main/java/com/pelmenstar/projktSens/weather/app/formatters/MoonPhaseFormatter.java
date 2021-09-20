package com.pelmenstar.projktSens.weather.app.formatters;

import android.content.Context;

import com.pelmenstar.projktSens.shared.MyMath;
import com.pelmenstar.projktSens.weather.app.R;

import org.jetbrains.annotations.NotNull;

public final class MoonPhaseFormatter {
    private final String[] moonPhases;

    public MoonPhaseFormatter(@NotNull Context context) {
        this.moonPhases = context.getResources().getStringArray(R.array.moonPhases);
    }

    @NotNull
    public String format(float phase) {
        int phaseIndex = (int) (phase * 8f);
        String phaseStr = moonPhases[phaseIndex];

        //noinspection StringBufferReplaceableByString
        StringBuilder sb = new StringBuilder(phaseStr.length() + 8);
        sb.append(MyMath.round(phase * 100f));
        sb.append('%');
        sb.append(' ');
        sb.append('(');
        sb.append(phaseStr);
        sb.append(')');

        return sb.toString();
    }
}
