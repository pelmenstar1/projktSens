package com.pelmenstar.projktSens.weather.models;

import com.pelmenstar.projktSens.shared.AppendableToStringBuilder;
import com.pelmenstar.projktSens.shared.MyMath;
import com.pelmenstar.projktSens.shared.serialization.ValueReader;
import com.pelmenstar.projktSens.shared.serialization.ValueWriter;
import com.pelmenstar.projktSens.shared.serialization.ObjectSerializer;
import com.pelmenstar.projktSens.shared.serialization.Serializable;
import com.pelmenstar.projktSens.shared.serialization.ValidationException;
import com.pelmenstar.projktSens.shared.time.ShortDateTime;
import com.pelmenstar.projktSens.shared.time.ShortDateTimeLong;

import org.jetbrains.annotations.NotNull;

/**
 * Union of short date time and some value.
 * Instances of {@link ValueWithDate} can be serialized in format described below:
 * - dateTime | 8 bytes <br/>
 * - value | 4 bytes <br/>
 * <b>Total: 12 bytes</b>
 */
public final class ValueWithDate extends AppendableToStringBuilder {
    public static final int SERIALIZED_OBJECT_SIZE = 12;

    @NotNull
    public static final ObjectSerializer<ValueWithDate> SERIALIZER;

    @ShortDateTimeLong
    public final long dateTime;

    public final float value;

    static {
        SERIALIZER = new Serializer();

        // not necessary but can affect to performance
        Serializable.registerSerializer(ValueWithDate.class, SERIALIZER);
    }

    ValueWithDate(long dateTime, float value, @SuppressWarnings("unused") boolean ignore) {
        this.dateTime = dateTime;
        this.value = MyMath.round(value);
    }

    public ValueWithDate(long dateTime, float value) {
        if(!ShortDateTime.isValid(dateTime)) {
            throw new IllegalArgumentException("dateTime");
        }

        this.dateTime = dateTime;
        this.value = MyMath.round(value);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        ValueWithDate o = (ValueWithDate) other;

        return dateTime == o.dateTime && value == o.value;
    }

    @Override
    public int hashCode() {
        int result = (int) (dateTime ^ (dateTime >>> 32));
        result = 31 * result + Float.floatToIntBits(value);

        return result;
    }

    @Override
    public void append(@NotNull StringBuilder sb) {
        sb.append("{dateTime=");
        ShortDateTime.append(dateTime, sb);
        sb.append(", value=");
        sb.append(value);
        sb.append('}');
    }

    @NotNull
    public UnitValueWithDate unit(int unit) {
        return new UnitValueWithDate(dateTime, UnitValue.of(value, unit));
    }

    @NotNull
    public UnitValueWithDate unit(int currentUnit, int newUnit) {
        long unitValue = UnitValue.of(value, currentUnit);

        return new UnitValueWithDate(dateTime, UnitValue.withUnit(unitValue, newUnit));
    }

    private static final class Serializer implements ObjectSerializer<ValueWithDate> {
        @Override
        public int getSerializedObjectSize(@NotNull ValueWithDate value) {
            return SERIALIZED_OBJECT_SIZE;
        }

        @Override
        public void writeObject(@NotNull ValueWithDate value, @NotNull ValueWriter writer) {
            writer.emitInt64(value.dateTime);
            writer.emitFloat(value.value);
        }

        @NotNull
        @Override
        public ValueWithDate readObject(@NotNull ValueReader reader) throws ValidationException {
            long dateTime = reader.readInt64();
            float value = reader.readFloat();

            if(!ShortDateTime.isValid(dateTime)) {
                throw ValidationException.invalidValue("dateTime", dateTime);
            }

            return new ValueWithDate(dateTime, value, false);
        }
    }
}
