package com.pelmenstar.projktSens.weather.app.formatters;

import com.pelmenstar.projktSens.shared.StringUtils;
import com.pelmenstar.projktSens.shared.time.PrettyDateFormatter;
import com.pelmenstar.projktSens.weather.models.UnitValue;
import com.pelmenstar.projktSens.weather.models.UnitValueWithDate;

import org.jetbrains.annotations.NotNull;

public final class UnitFormatter {
    @NotNull
    private final String[] units;

    @NotNull
    private final PrettyDateFormatter dateFormatter;

    public UnitFormatter(@NotNull String[] units, @NotNull PrettyDateFormatter dateFormatter) {
        this.units = units;
        this.dateFormatter = dateFormatter;
    }

    @NotNull
    public String formatValue(long unitValue) {
        return formatValue(UnitValue.getAbsoluteValue(unitValue), UnitValue.getUnit(unitValue));
    }

    @NotNull
    public String formatValue(float value, int unit) {
        return value + getUnitString(unit);
    }

    @NotNull
    public String formatValueAndDelta(float value, float delta, int unit) {
        String unitStr = getUnitString(unit);
        StringBuilder sb = new StringBuilder();

        sb.append(value);
        sb.append(unitStr);
        sb.append(' ');
        sb.append('(');
        StringUtils.appendSigned(delta, sb);
        sb.append(unitStr);
        sb.append(')');

        return sb.toString();
    }

    @NotNull
    public String formatValueWithDate(@NotNull UnitValueWithDate ud) {
        long value = ud.value;

        StringBuilder sb = new StringBuilder(32);

        sb.append(UnitValue.getAbsoluteValue(value));
        sb.append(getUnitString(UnitValue.getUnit(value)));
        sb.append(' ');
        sb.append('(');
        dateFormatter.appendPrettyDateTime(ud.dateTime, sb);
        sb.append(')');

        return sb.toString();
    }

    @NotNull
    public String getUnitString(int unit) {
        return units[unit];
    }
}
