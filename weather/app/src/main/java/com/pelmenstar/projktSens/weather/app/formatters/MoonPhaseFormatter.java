package com.pelmenstar.projktSens.weather.app.formatters;

import org.jetbrains.annotations.NotNull;

public final class MoonPhaseFormatter {
    @NotNull
    private final String[] moonPhases;

    public MoonPhaseFormatter(@NotNull String[] moonPhases) {
        this.moonPhases = moonPhases;
    }

    @NotNull
    public String format(float phase) {
        int phaseIndex = (int)(phase * 8f);
        String phaseStr = moonPhases[phaseIndex];

        int phase100 = (int)(phase * 100f);

        int phase100Length;
        char[] buffer;

        if(phase100 < 10) {
            buffer = new char[phaseStr.length() + 6];
            phase100Length = 1;

            buffer[0] = (char)('0' + phase100);
        } else if(phase100 < 100) {
            buffer = new char[phaseStr.length() + 7];
            phase100Length = 2;

            int d2 = phase100 / 10;

            buffer[0] = (char)('0' + d2);
            buffer[1] = (char)('0' + (phase100 - (d2 * 10)));
        } else if(phase100 == 100) {
            buffer = new char[phaseStr.length() + 8];
            phase100Length = 3;

            buffer[0] = '1';
            buffer[1] = '0';
            buffer[2] = '0';
        } else {
            throw new RuntimeException("Something goes wrong");
        }

        buffer[phase100Length] = '%';
        buffer[phase100Length + 1] = ' ';
        buffer[phase100Length + 2] = '(';
        phaseStr.getChars(0, phaseStr.length(), buffer, phase100Length + 3);
        buffer[phaseStr.length() + phase100Length + 4] = ')';

        return new String(buffer, 0, buffer.length);
    }
}
