package com.pelmenstar.projktSens.weather.models;

import com.pelmenstar.projktSens.shared.AppendableToStringBuilder;
import com.pelmenstar.projktSens.shared.MyMath;
import com.pelmenstar.projktSens.shared.serialization.ValueReader;
import com.pelmenstar.projktSens.shared.serialization.ValueWriter;
import com.pelmenstar.projktSens.shared.serialization.ObjectSerializer;
import com.pelmenstar.projktSens.shared.serialization.Serializable;
import com.pelmenstar.projktSens.shared.serialization.ValidationException;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public final class ParameterStats extends AppendableToStringBuilder {
    public static final int SERIALIZED_OBJECT_SIZE = 24;
    public static final ObjectSerializer<ParameterStats> SERIALIZER;

    public final float min;
    public final float max;
    public final float avg;
    public final float median;
    public final float stdDev;
    public final float stdErr;

    static {
        SERIALIZER = new Serializer();
        Serializable.registerSerializer(ParameterStats.class, SERIALIZER);
    }

    public ParameterStats(
            float min, float max,
            float avg,
            float median,
            float stdDev, float stdErr) {
        this.min = MyMath.round(min);
        this.max = MyMath.round(max);
        this.avg = MyMath.round(avg);
        this.median = MyMath.round(median);
        this.stdDev = MyMath.round(stdDev);
        this.stdErr = MyMath.round(stdErr);
    }

    @Contract("_ -> new")
    @NotNull
    public static ParameterStats create(@NotNull float[] values) {
        Arrays.sort(values);

        float min = values[0];
        float max = values[values.length - 1];

        float sum = 0;
        int length = values.length;

        // calc avg
        for (float value : values) {
            sum += value;
        }

        float invLength = 1f / length;
        float avg = sum * invLength;

        // cals median
        float median;
        int mid = length / 2;
        if(mid * 2 == length) {
            median = (values[mid] + values[mid + 1]) * 0.5f;
        } else {
            median = values[mid];
        }

        // calc std dev

        float sumSqDiff = 0;
        for(float value: values) {
            float diff = Math.abs(value - avg);
            sumSqDiff += diff * diff;
        }

        float stdDev = (float)Math.sqrt(sumSqDiff * invLength);
        float stdErr = stdDev * invLength;

        return new ParameterStats(
                min,
                max,
                avg,
                median,
                stdDev, stdErr
        );
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        ParameterStats o = (ParameterStats) other;

        return min == o.min &&
                max == o.max &&
                avg == o.avg &&
                median == o.median &&
                stdDev == o.stdDev &&
                stdErr == o.stdErr;
    }

    @Override
    public int hashCode() {
        int result = Float.floatToIntBits(min);
        result = 31 * result + Float.floatToIntBits(max);
        result = 31 * result + Float.floatToIntBits(avg);
        result = 31 * result + Float.floatToIntBits(median);
        result = 31 * result + Float.floatToIntBits(stdDev);
        result = 31 * result + Float.floatToIntBits(stdErr);

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
        sb.append(", stdDev=");
        sb.append(stdDev);
        sb.append(", stdErr=");
        sb.append(stdErr);
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
            writer.emitFloat(value.stdDev);
            writer.emitFloat(value.stdErr);
        }

        @NotNull
        @Override
        public ParameterStats readObject(@NotNull ValueReader reader) throws ValidationException {
            float min = reader.readFloat();
            float max = reader.readFloat();
            float avg = reader.readFloat();
            float median = reader.readFloat();
            float stdDev = reader.readFloat();
            float stdErr = reader.readFloat();

            return new ParameterStats(
                    min,
                    max,
                    avg,
                    median,
                    stdDev, stdErr
            );
        }
    }
}
