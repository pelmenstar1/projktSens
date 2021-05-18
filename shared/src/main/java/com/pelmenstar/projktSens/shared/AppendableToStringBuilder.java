package com.pelmenstar.projktSens.shared;

import org.jetbrains.annotations.NotNull;

/**
 * Abstract class that gives a capability to append object to {@link StringBuilder}. Created for
 * performance reasons, because {@link StringBuilder#append(Object)} calls {@link Object#toString()},
 * which creates new string, and appends it to {@link StringBuilder}.
 * So this class helps to avoid needless allocation,
 * when it needed just to append {@link Object} to {@link StringBuilder}.
 */
public abstract class AppendableToStringBuilder {
    protected AppendableToStringBuilder() {
    }

    @Override
    @NotNull
    public String toString() {
        StringBuilder sb = new StringBuilder();
        append(sb);

        return sb.toString();
    }

    /**
     * Appends current state of {@link Object} to {@link StringBuilder}.
     *
     * @param sb output {@link StringBuilder}
     */
    public abstract void append(@NotNull StringBuilder sb);
}
