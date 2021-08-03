package com.pelmenstar.projktSens.shared.serialization;

import org.jetbrains.annotations.NotNull;

final class PrimitiveSerializers {
    static final class Int32 implements ObjectSerializer<Integer> {
        @Override
        public int getSerializedObjectSize(@NotNull Integer value) {
            return 4;
        }

        @Override
        public void writeObject(@NotNull Integer value, @NotNull ValueWriter writer) {
            writer.emitInt32(value);
        }

        @Override
        public @NotNull Integer readObject(@NotNull ValueReader reader) throws ValidationException {
            return reader.readInt32();
        }
    }

    static final class Int64 implements ObjectSerializer<Long> {
        @Override
        public int getSerializedObjectSize(@NotNull Long value) {
            return 8;
        }

        @Override
        public void writeObject(@NotNull Long value, @NotNull ValueWriter writer) {
            writer.emitInt64(value);
        }

        @Override
        public @NotNull Long readObject(@NotNull ValueReader reader) throws ValidationException {
            return reader.readInt64();
        }
    }
}
