package com.pelmenstar.projktSens.shared.serialization;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a light interface to convert Java object to bytes and vise-versa through helper classes
 * {@link ValueWriter} and {@link ValueReader} which is delegating job of writing and reading the data.
 * This class is not recommended to have mutual state.
 * Also, there are some limitations or requirements for serializable object: <br/>
 * - The same object must have the same raw byte valuesLength. <br/>
 * - Class must have: public static final ObjectSerializer<YourClass> SERIALIZER = new ...
 * - Written and read byte valuesLength on same object must be the same and equal to
 * {@link ObjectSerializer#getSerializedObjectSize(T)}
 */
public interface ObjectSerializer<T> {
    int getSerializedObjectSize(@NotNull T value);

    void writeObject(@NotNull T value, @NotNull ValueWriter writer);

    @Contract("_ -> new")
    @NotNull
    T readObject(@NotNull ValueReader reader) throws ValidationException;
}
