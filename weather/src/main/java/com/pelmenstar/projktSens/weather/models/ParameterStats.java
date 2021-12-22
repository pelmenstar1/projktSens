package com.pelmenstar.projktSens.weather.models;

import com.pelmenstar.projktSens.shared.AppendableToStringBuilder;
import com.pelmenstar.projktSens.shared.serialization.ObjectSerializer;
import com.pelmenstar.projktSens.shared.serialization.Serializable;
import com.pelmenstar.projktSens.shared.serialization.ValidationException;
import com.pelmenstar.projktSens.shared.serialization.ValueReader;
import com.pelmenstar.projktSens.shared.serialization.ValueWriter;

import org.jetbrains.annotations.NotNull;

public final class ParameterStats extends AppendableToStringBuilder {
    public static final int SERIALIZED_OBJECT_SIZE = 8 + 2 * ValueWithDate.SERIALIZED_OBJECT_SIZE;

    @NotNull
    public static final ObjectSerializer<ParameterStats> SERIALIZER;

    @NotNull
    public final ValueWithDate min;

    @NotNull
    public final ValueWithDate max;
    public final float avg;
    public final float median;
    public final float amplitude;

    static {
        SERIALIZER = new Serializer();
        Serializable.registerSerializer(ParameterStats.class, SERIALIZER);
    }

    public ParameterStats(
            @NotNull ValueWithDate min,
            @NotNull ValueWithDate max,
            float avg,
            float median
    ) {
        this.min = min;
        this.max = max;
        this.avg = avg;
        this.median = median;
        this.amplitude = Math.abs(max.value - min.value);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        ParameterStats o = (ParameterStats) other;

        return min.equals(o.min) &&
                max.equals(o.max) &&
                avg == o.avg &&
                median == o.median;
    }

    @Override
    public int hashCode() {
        int result = min.hashCode();
        result = 31 * result + max.hashCode();
        result = 31 * result + Float.floatToIntBits(avg);
        result = 31 * result + Float.floatToIntBits(median);

        return result;
    }

    @Override
    public void append(@NotNull StringBuilder sb) {
        sb.append("{min=");
        min.append(sb);
        sb.append(", max=");
        max.append(sb);
        sb.append(", avg=");
        sb.append(avg);
        sb.append(", median=");
        sb.append(median);
        sb.append('}');
    }

    private static final class Serializer implements ObjectSerializer<ParameterStats> {
        @Override
        public int getSerializedObjectSize(@NotNull ParameterStats value) {
            return SERIALIZED_OBJECT_SIZE;
        }

        @Override
        public void writeObject(@NotNull ParameterStats value, @NotNull ValueWriter writer) {
            ValueWithDate.SERIALIZER.writeObject(value.min, writer);
            ValueWithDate.SERIALIZER.writeObject(value.max, writer);
            writer.float32(value.avg);
            writer.float32(value.median);
        }

        @NotNull
        @Override
        public ParameterStats readObject(@NotNull ValueReader reader) throws ValidationException {
            ValueWithDate min = ValueWithDate.SERIALIZER.readObject(reader);
            ValueWithDate max = ValueWithDate.SERIALIZER.readObject(reader);
            float avg = reader.float32();
            float median = reader.float32();

            return new ParameterStats(min, max, avg, median);
        }
    }
}
