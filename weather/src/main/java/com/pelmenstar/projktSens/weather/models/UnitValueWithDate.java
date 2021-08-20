package com.pelmenstar.projktSens.weather.models;

import com.pelmenstar.projktSens.shared.AppendableToStringBuilder;
import com.pelmenstar.projktSens.shared.serialization.ValidationException;
import com.pelmenstar.projktSens.shared.time.ShortDateTime;
import com.pelmenstar.projktSens.shared.time.ShortDateTimeLong;

import org.jetbrains.annotations.NotNull;

public final class UnitValueWithDate extends AppendableToStringBuilder {
    @ShortDateTimeLong
    public final long dateTime;
    public final long value;

    public UnitValueWithDate(@ShortDateTimeLong long dateTime, long value) {
        if (!ShortDateTime.isValid(dateTime)) {
            throw ValidationException.invalidValue("dateTime", dateTime);
        }

        if (!UnitValue.isValid(value)) {
            throw ValidationException.invalidValue("value", value);
        }

        this.dateTime = dateTime;
        this.value = value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        UnitValueWithDate o = (UnitValueWithDate) other;

        return dateTime == o.dateTime && value == o.value;
    }

    @Override
    public int hashCode() {
        int result = (int) (dateTime ^ (dateTime >>> 32));
        result = 31 * result + (int) (value ^ (value >>> 32));
        return result;
    }

    @Override
    public void append(@NotNull StringBuilder sb) {
        sb.append("{dateTime=");
        ShortDateTime.append(dateTime, sb);
        sb.append(", value=");
        UnitValue.appendString(value, sb);
        sb.append('}');
    }
}
