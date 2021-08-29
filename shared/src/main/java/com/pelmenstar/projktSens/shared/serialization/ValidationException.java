package com.pelmenstar.projktSens.shared.serialization;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Signals that argument or else data was not valid.
 * <p>
 * It had better to use factory methods, but it is still possible to create {@link ValidationException} through constructors
 */
public final class ValidationException extends RuntimeException {
    public ValidationException() {
        super();
    }

    public ValidationException(@Nullable String message) {
        super(message);
    }

    /**
     * Creates {@link ValidationException} which signals that some value of input is invalid, value type is {@code int}.
     *
     * @param valueName name of value
     * @param value     integer value of input value
     * @return {@link ValidationException} with appropriate message
     */
    @NotNull
    public static ValidationException invalidValue(@NotNull String valueName, int value) {
        return invalidValue(valueName, Integer.toString(value));
    }

    /**
     * Creates {@link ValidationException} which signals that some value of input is invalid, value type is {@code float}.
     *
     * @param valueName name of value
     * @param value     float value of input value
     * @return {@link ValidationException} with appropriate message
     */
    @NotNull
    public static ValidationException invalidValue(@NotNull String valueName, float value) {
        return invalidValue(valueName, Float.toString(value));
    }

    /**
     * Creates {@link ValidationException} which signals that some value of input is invalid, value type is {@code long}.
     *
     * @param valueName name of value
     * @param value     long value of input value
     * @return {@link ValidationException} with appropriate message
     */
    @NotNull
    public static ValidationException invalidValue(@NotNull String valueName, long value) {
        return invalidValue(valueName, Long.toString(value));
    }

    @NotNull
    public static ValidationException invalidValue(@NotNull String valueName, @NotNull String value) {
        return new ValidationException("Invalid '" + valueName + "': " + value);
    }

    /**
     * Creates {@link ValidationException} which signals that some value of input is invalid.
     * Despite the similar to this methods, actual value is not specified
     *
     * @return {@link ValidationException} with appropriate message
     */
    @NotNull
    public static ValidationException invalidValue(@NotNull String valueName) {
        return new ValidationException("Invalid '" + valueName + "' value");
    }
}
