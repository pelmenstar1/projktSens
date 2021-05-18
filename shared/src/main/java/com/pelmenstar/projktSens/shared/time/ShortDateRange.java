package com.pelmenstar.projktSens.shared.time;

import com.pelmenstar.projktSens.shared.serialization.ValueReader;
import com.pelmenstar.projktSens.shared.serialization.ValueWriter;
import com.pelmenstar.projktSens.shared.serialization.ObjectSerializer;
import com.pelmenstar.projktSens.shared.serialization.Serializable;
import com.pelmenstar.projktSens.shared.serialization.ValidationException;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a range between date-ints.
 * Instances of this class can be serialized in format: <br/>
 * - start | 4 bytes <br/>
 * - endInclusive | 4 bytes <br/>
 */
public final class ShortDateRange {
    public static final int SERIALIZED_OBJECT_SIZE = 8;

    /**
     * Serializer of {@link ShortDateRange}
     */
    @NotNull
    public static final ObjectSerializer<ShortDateRange> SERIALIZER;

    /**
     * Start date, inclusive. Always less than {@link ShortDateRange#endInclusive}
     */
    @ShortDateInt
    public final int start;

    /**
     * End date, inclusive. Always equals or greater than {@link ShortDateRange#start}
     */
    @ShortDateInt
    public final int endInclusive;

    static {
        SERIALIZER = new Serializer();
        Serializable.registerSerializer(ShortDateRange.class, SERIALIZER);
    }

    /**
     * Initializes instance of {@link ShortDateRange} using start and end date-ints
     * @param start start of range, represented in date-int
     * @param endInclusive end of range, represented in date-int
     *
     * @throws ValidationException if start or endInclusive are invalid.
     * Also if start is greater than endInclusive.
     */
    public ShortDateRange(@ShortDateInt int start, @ShortDateInt int endInclusive) {
        if (!ShortDate.isValid(start)) {
            throw ValidationException.invalidValue("start", start);
        }

        if (!ShortDate.isValid(endInclusive)) {
            throw ValidationException.invalidValue("endInclusive", endInclusive);
        }

        if (start > endInclusive) {
            throw new ValidationException("start > endInclusive");
        }


        this.start = start;
        this.endInclusive = endInclusive;
    }

    /**
     * Determines whether range includes specified date-int
     */
    public boolean contains(@ShortDateInt int date) {
        return date >= start && date <= endInclusive;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ShortDateRange other = (ShortDateRange) o;

        return start == other.start && endInclusive == other.endInclusive;
    }

    @Override
    public int hashCode() {
        int result = start;
        result = result * 31 + endInclusive;

        return result;
    }

    @Override
    @NotNull
    public String toString() {
        return '[' + ShortDate.toString(start) + ',' + ShortDate.toString(endInclusive)  + ']';
    }

    private static final class Serializer implements ObjectSerializer<ShortDateRange> {
        @Override
        public int getSerializedObjectSize(@NotNull ShortDateRange value) {
            return SERIALIZED_OBJECT_SIZE;
        }

        @Override
        public void writeObject(@NotNull ShortDateRange value, @NotNull ValueWriter writer) {
            writer.emitInt32(value.start);
            writer.emitInt32(value.endInclusive);
        }

        @NotNull
        @Override
        public ShortDateRange readObject(@NotNull ValueReader reader) throws ValidationException {
            int start = reader.readInt32();
            int end = reader.readInt32();

            return new ShortDateRange(start, end);
        }
    }
}
