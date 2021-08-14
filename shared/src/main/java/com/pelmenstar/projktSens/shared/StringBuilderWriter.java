package com.pelmenstar.projktSens.shared;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Writer;

public final class StringBuilderWriter extends Writer {
    private final StringBuilder sb;

    public StringBuilderWriter(@NotNull StringBuilder sb) {
        this.sb = sb;
    }

    @Override
    public void write(char @NotNull [] chars, int off, int len) throws IOException {
        sb.append(chars, off, len);
    }

    @Override
    public void write(@NotNull String str, int off, int len) throws IOException {
        sb.append(str, off, len);
    }

    @Override
    public Writer append(char c) throws IOException {
        sb.append(c);
        return this;
    }

    @Override
    public Writer append(CharSequence csq) throws IOException {
        sb.append(csq);
        return this;
    }

    @Override
    public Writer append(CharSequence csq, int start, int end) throws IOException {
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
