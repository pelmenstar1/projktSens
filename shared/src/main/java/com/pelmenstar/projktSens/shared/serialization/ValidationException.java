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
     * Creates {@link ValidationException} which signals that some input has invalid size.
     *
     * @param actual   actual size of input
     * @param expected expected size of input
     * @return {@link ValidationException} with appropriate message
     */
    @NotNull
    public static ValidationException invalidSize(int actual, int expected) {
        return new ValidationException("Invalid size. Actual: " + actual + ", but expected: " + expected);
    }

    /**
     * Creates {@link ValidationException} which signals that some input has invalid size.
     * Despite the {@link ValidationException#invalidSize(int, int)}, expected size of input is not indicated
     *
     * @param actual            actual size of input
     * @param additionalMessage some message
     * @return {@link ValidationException} with appropriate message
     */
    @NotNull
    public static ValidationException invalidSize(int actual, @NotNull String additionalMessage) {
        return new ValidationException("Invalid size. Actual: " + actual + ", but expected: " + additionalMessage);
    }

    /**
     * Creates {@link ValidationException} which signals that some input is invalid
     *
     * @return {@link ValidationException} with appropriate message
     */
    @NotNull
    public static ValidationException invalidContent() {
        return new ValidationException("Invalid content");
    }

    /**
     * Creates {@link ValidationException} which signals that some input is invalid
     * Despite the {@link ValidationException#invalidContent()} method, additional message can be indicated
     *
     * @return {@link ValidationException} with appropriate message
     */
    @NotNull
    public static ValidationException invalidContent(@NotNull String additionalMessage) {
        return new ValidationException("Invalid content. " + additionalMessage);
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
        return new ValidationException("Invalid '" + valueName + "': " + value);
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
        return new ValidationException("Invalid '" + valueName + "': " + value);
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
