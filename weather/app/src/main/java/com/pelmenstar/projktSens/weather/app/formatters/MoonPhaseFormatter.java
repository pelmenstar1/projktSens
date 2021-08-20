package com.pelmenstar.projktSens.weather.app.formatters;

import com.pelmenstar.projktSens.shared.MyMath;

import org.jetbrains.annotations.NotNull;

public final class MoonPhaseFormatter {
    private final String[] moonPhases;

    public MoonPhaseFormatter(@NotNull String @NotNull [] moonPhases) {
        this.moonPhases = moonPhases;
    }

    @NotNull
    public String format(float phase) {
        int phaseIndex = (int) (phase * 8f);
        String phaseStr = moonPhases[phaseIndex];

        //noinspection StringBufferReplaceableByString
        StringBuilder sb = new StringBuilder(phaseStr.length() + 8);
        sb.append(MyMath.round(phase));
        sb.append('%');
        sb.append(' ');
        sb.append('(');
        sb.append(phaseStr);
        sb.append(')');

        return sb.toString();
    }
}
