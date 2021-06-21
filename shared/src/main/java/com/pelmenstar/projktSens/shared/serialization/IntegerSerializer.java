package com.pelmenstar.projktSens.shared.serialization;

import org.jetbrains.annotations.NotNull;

final class IntegerSerializer implements ObjectSerializer<Integer> {
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
