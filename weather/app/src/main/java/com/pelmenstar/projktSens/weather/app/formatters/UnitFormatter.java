package com.pelmenstar.projktSens.weather.app.formatters;

import com.pelmenstar.projktSens.shared.StringUtils;
import com.pelmenstar.projktSens.shared.time.PrettyDateFormatter;
import com.pelmenstar.projktSens.weather.models.UnitValue;
import com.pelmenstar.projktSens.weather.models.ValueWithDate;

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
        String unitString = getUnitString(unit);

        int sbLength = StringUtils.getRound1Length(value) + unitString.length();
        StringBuilder sb = new StringBuilder(sbLength);

        StringUtils.appendRound1(value, sb);
        sb.append(unitString);

        return sb.toString();
    }

    @NotNull
    public String formatValueAndDelta(float value, float delta, int unit) {
        String unitStr = getUnitString(unit);

        int sbLength = StringUtils.getRound1Length(value) +
                StringUtils.getSignedRound1Length(delta) +
                unitStr.length() * 2 +
                3;
        StringBuilder sb = new StringBuilder(sbLength);

        StringUtils.appendRound1(value, sb);
        sb.append(unitStr);
        sb.append(' ');
        sb.append('(');
        StringUtils.appendSignedRound1(delta, sb);
        sb.append(unitStr);
        sb.append(')');

        return sb.toString();
    }

    @NotNull
    public String formatValueWithDate(@NotNull ValueWithDate vd, int currentUnit, int prefUnit) {
        long dateTime = vd.dateTime;
        float value = UnitValue.getValue(vd.value, currentUnit, prefUnit);
        String unitString = getUnitString(prefUnit);

        int sbLength = StringUtils.getRound1Length(value) +
                unitString.length() + dateFormatter.approximatePrettyDateTimeLength(dateTime) + 5;

        StringBuilder sb = new StringBuilder(sbLength);

        StringUtils.appendRound1(value, sb);
        sb.append(unitString);
        sb.append(' ');
        sb.append('(');
        dateFormatter.appendPrettyDateTime(dateTime, sb);
        sb.append(')');

        return sb.toString();
    }

    @NotNull
    public String getUnitString(int unit) {
        return units[unit];
    }
}
