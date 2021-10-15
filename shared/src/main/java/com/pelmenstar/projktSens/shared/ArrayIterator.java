package com.pelmenstar.projktSens.shared;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public final class ArrayIterator<T> implements Iterator<T> {
    private final T @NotNull [] elements;
    private int index;

    public ArrayIterator(T @NotNull [] elements) {
        this.elements = elements;
    }

    @Override
    public boolean hasNext() {
        return index < elements.length;
    }

    @Override
    public T next() {
        return elements[index++];
    }
}
