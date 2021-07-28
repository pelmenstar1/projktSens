package com.pelmenstar.projktSens.weather.models;

import com.pelmenstar.projktSens.shared.AppendableToStringBuilder;
import com.pelmenstar.projktSens.shared.MyMath;
import com.pelmenstar.projktSens.shared.serialization.ObjectSerializer;
import com.pelmenstar.projktSens.shared.serialization.Serializable;
import com.pelmenstar.projktSens.shared.serialization.ValidationException;
import com.pelmenstar.projktSens.shared.serialization.ValueReader;
import com.pelmenstar.projktSens.shared.serialization.ValueWriter;

import org.jetbrains.annotations.NotNull;

public final class ParameterStats extends AppendableToStringBuilder {
    public static final int SERIALIZED_OBJECT_SIZE = 16;
    public static final ObjectSerializer<ParameterStats> SERIALIZER;

    public final float min;
    public final float max;
    public final float avg;
    public final float median;

    static {
        SERIALIZER = new Serializer();
        Serializable.registerSerializer(ParameterStats.class, SERIALIZER);
    }

    public ParameterStats(
            float min,
            float max,
            float avg,
            float median
    ) {
        this.min = min;
        this.max = max;
        this.avg = avg;
        this.median = median;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        ParameterStats o = (ParameterStats) other;

        return min == o.min &&
                max == o.max &&
                avg == o.avg &&
                median == o.median;
    }

    @Override
    public int hashCode() {
        int result = Float.floatToIntBits(min);
        result = 31 * result + Float.floatToIntBits(max);
        result = 31 * result + Float.floatToIntBits(avg);
        result = 31 * result + Float.floatToIntBits(median);

        return result;
    }

    @Override
    public void append(@NotNull StringBuilder sb) {
        sb.append("{min=");
        sb.append(min);
        sb.append(", max=");
        sb.append(max);
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
            writer.emitFloat(value.min);
            writer.emitFloat(value.max);
            writer.emitFloat(value.avg);
            writer.emitFloat(value.median);
        }

        @NotNull
        @Override
        public ParameterStats readObject(@NotNull ValueReader reader) throws ValidationException {
            float min = reader.readFloat();
            float max = reader.readFloat();
            float avg = reader.readFloat();
            float median = reader.readFloat();

            return new ParameterStats(
                    min,
                    max,
                    avg,
                    median
            );
        }
    }
}
