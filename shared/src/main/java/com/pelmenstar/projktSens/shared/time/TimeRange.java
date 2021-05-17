package com.pelmenstar.projktSens.shared.time;

import com.pelmenstar.projktSens.shared.serialization.ValueReader;
import com.pelmenstar.projktSens.shared.serialization.ValueWriter;
import com.pelmenstar.projktSens.shared.serialization.ObjectSerializer;
import com.pelmenstar.projktSens.shared.serialization.Serializable;
import com.pelmenstar.projktSens.shared.serialization.ValidationException;

import org.jetbrains.annotations.NotNull;

/**
 * The data object which holds the start and end time inclusive
 */
public final class TimeRange {
    public static final int SERIALIZED_OBJECT_SIZE = 8;

    @NotNull
    public static final ObjectSerializer<TimeRange> SERIALIZER;

    /**
     * The start time, inclusive. Can't be greater than {@link TimeRange#endInclusive}
     */
    @TimeInt
    public final int start;

    /**
     * The end time, inclusive. Can't be less than {@link TimeRange#start}
     */
    @TimeInt
    public final int endInclusive;

    static {
        SERIALIZER = new Serializer();
        Serializable.registerSerializer(TimeRange.class, SERIALIZER);
    }

    public TimeRange(@TimeInt int start, @TimeInt int endInclusive) {
        if(!ShortTime.isValid(start)) {
            throw new IllegalArgumentException("start");
        }

        if(!ShortTime.isValid(endInclusive)) {
            throw new IllegalArgumentException("endInclusive");
        }

        if(start > endInclusive) {
            throw new IllegalArgumentException("start > endInclusive");
        }

        this.start = start;
        this.endInclusive = endInclusive;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        TimeRange o = (TimeRange) other;

        return start == o.start && endInclusive == o.endInclusive;
    }

    @Override
    public int hashCode() {
        return start ^ endInclusive;
    }

    @Override
    @NotNull
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        ShortTime.append(start, sb);
        sb.append(';');
        ShortTime.append(endInclusive, sb);
        sb.append(']');

        return sb.toString();
    }

    private static final class Serializer implements ObjectSerializer<TimeRange> {
        @Override
        public int getSerializedObjectSize(@NotNull TimeRange value) {
            return SERIALIZED_OBJECT_SIZE;
        }

        @Override
        public void writeObject(@NotNull TimeRange value, @NotNull ValueWriter writer) {
            writer.emitInt32(value.start);
            writer.emitInt32(value.endInclusive);
        }

        @NotNull
        @Override
        public TimeRange readObject(@NotNull ValueReader reader) throws ValidationException {
            int start = reader.readInt32();
            int end = reader.readInt32();

            return new TimeRange(start, end);
        }
    }
}
