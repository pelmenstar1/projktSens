package com.pelmenstar.projktSens.weather.app.formatters;

import com.pelmenstar.projktSens.chartLite.formatter.ValueFormatter;

import org.jetbrains.annotations.NotNull;

public final class UnitChartValueFormatter implements ValueFormatter {
    @NotNull
    private final UnitFormatter unitFormatter;
    private final int unit;

    public UnitChartValueFormatter(@NotNull UnitFormatter unitFormatter, int unit) {
        this.unit = unit;
        this.unitFormatter = unitFormatter;
    }

    @NotNull
    @Override
    public String format(float value) {
        return unitFormatter.formatValue((int) value, unit);
    }
}
