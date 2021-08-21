package com.pelmenstar.projktSens.shared;

import org.jetbrains.annotations.NotNull;

import java.io.Writer;

/**
 * Allows {@link StringBuilder} to be {@link Writer}.
 * Delegates all operations to {@link StringBuilder}
 */
public final class StringBuilderWriter extends Writer {
    private final StringBuilder sb;

    public StringBuilderWriter(@NotNull StringBuilder sb) {
        this.sb = sb;
    }

    @Override
    public void write(char @NotNull [] chars, int off, int len) {
        sb.append(chars, off, len);
    }

    @Override
    public void write(@NotNull String str, int off, int len) {
        sb.append(str, off, len);
    }

    @Override
    public Writer append(char c) {
        sb.append(c);
        return this;
    }

    @Override
    public Writer append(CharSequence csq) {
        sb.append(csq);
        return this;
    }

    @Override
    public Writer append(CharSequence csq, int start, int end) {
        sb.append(csq, start, end);
        return this;
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() {
    }
}
