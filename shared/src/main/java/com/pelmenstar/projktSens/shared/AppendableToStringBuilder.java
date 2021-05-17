package com.pelmenstar.projktSens.shared;

import org.jetbrains.annotations.NotNull;

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

    public abstract void append(@NotNull StringBuilder sb);
}
