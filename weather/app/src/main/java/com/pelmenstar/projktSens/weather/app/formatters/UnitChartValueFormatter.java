package com.pelmenstar.projktSens.weather.app.formatters;

import com.pelmenstar.projktSens.chartLite.formatter.ValueFormatter;

import org.jetbrains.annotations.NotNull;

public final class UnitChartValueFormatter extends ValueFormatter {
    @NotNull
    private final UnitFormatter unitFormatter;
    private final int unit;

    public UnitChartValueFormatter(@NotNull UnitFormatter unitFormatter, int unit) {
        super(false);

        this.unit = unit;
        this.unitFormatter = unitFormatter;
    }

    @NotNull
    @Override
    public String formatToString(float value) {
        return unitFormatter.formatValue(value, unit);
    }
}
