package com.pelmenstar.projktSens.weather.app.formatters;

import com.pelmenstar.projktSens.shared.MyMath;
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
        return new String(formatValueToCharBuffer(value, unit));
    }

    public char @NotNull [] formatValueToCharBuffer(float value, int unit) {
        String unitString = getUnitString(unit);
        int valueLength = StringUtils.getBufferSizeForRoundedFloat(value);
        int bufferLength = unitString.length() + valueLength;

        char[] buffer = new char[bufferLength];

        StringUtils.writeFloatRound1(value, buffer, 0);
        unitString.getChars(0, unitString.length(), buffer, valueLength);

        return buffer;
    }

    @NotNull
    public String formatValueAndDelta(float value, float delta, int unit) {
        return new String(formatValueAndDeltaToCharBuffer(value, delta, unit));
    }

    public char @NotNull [] formatValueAndDeltaToCharBuffer(float value, float delta, int unit) {
        String unitStr = getUnitString(unit);
        int valueLength = StringUtils.getBufferSizeForRoundedFloat(value);
        int deltaLength = StringUtils.getBufferSizeForSignedRoundedFloat(delta);

        int bufferLength = valueLength + deltaLength + unitStr.length() * 2 + 3;
        char[] buffer = new char[bufferLength];

        StringUtils.writeFloatRound1(value, buffer, 0);
        unitStr.getChars(0, unitStr.length(), buffer, valueLength);
        int valueAndUnitPos = valueLength + unitStr.length();
        buffer[valueAndUnitPos] = ' ';
        buffer[valueAndUnitPos + 1] = '(';
        StringUtils.writeSignedFloatRound1(delta, buffer, valueAndUnitPos + 2);
        int deltaEndPos = valueAndUnitPos + deltaLength + 2;
        unitStr.getChars(0, unitStr.length(), buffer, deltaEndPos);
        buffer[deltaEndPos + unitStr.length()] = ')';

        return buffer;
    }

    @NotNull
    public String formatValueWithDate(@NotNull ValueWithDate vd, int currentUnit, int prefUnit) {
        return formatValueWithDateToBuilder(vd, currentUnit, prefUnit).toString();
    }

    @NotNull
    private StringBuilder formatValueWithDateToBuilder(
            @NotNull ValueWithDate vd,
            int currentUnit, int prefUnit
    ) {
        long dateTime = vd.dateTime;
        float value = UnitValue.getValue(vd.value, currentUnit, prefUnit);
        String unitString = getUnitString(prefUnit);

        int sbLength = MyMath.decimalDigitCount((int) value) +
                unitString.length() + dateFormatter.approximatePrettyDateTimeLength(dateTime) + 5;

        StringBuilder sb = new StringBuilder(sbLength);

        sb.append(MyMath.round(value));
        sb.append(unitString);
        sb.append(' ');
        sb.append('(');
        dateFormatter.appendPrettyDateTime(dateTime, sb);
        sb.append(')');

        return sb;
    }

    @NotNull
    public String getUnitString(int unit) {
        return units[unit];
    }
}
