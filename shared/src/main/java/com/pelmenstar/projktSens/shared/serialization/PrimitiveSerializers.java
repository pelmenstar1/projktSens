package com.pelmenstar.projktSens.shared.serialization;

import org.jetbrains.annotations.NotNull;

final class PrimitiveSerializers {
    @NotNull
    public static final ObjectSerializer<Integer> INT_32 = new ObjectSerializer<Integer>() {
        @Override
        public int getSerializedObjectSize(@NotNull Integer value) {
            return 4;
        }

        @Override
        public void writeObject(@NotNull Integer value, @NotNull ValueWriter writer) {
            writer.int32(value);
        }

        @Override
        public @NotNull Integer readObject(@NotNull ValueReader reader) throws ValidationException {
            return reader.int32();
        }
    };

    @NotNull
    public static final ObjectSerializer<Long> INT_64 = new ObjectSerializer<Long>() {
        @Override
        public int getSerializedObjectSize(@NotNull Long value) {
            return 8;
        }

        @Override
        public void writeObject(@NotNull Long value, @NotNull ValueWriter writer) {
            writer.int64(value);
        }

        @Override
        public @NotNull Long readObject(@NotNull ValueReader reader) throws ValidationException {
            return reader.int64();
        }
    };
}
