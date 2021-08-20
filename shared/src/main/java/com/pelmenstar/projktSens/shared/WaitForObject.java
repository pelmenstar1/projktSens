package com.pelmenstar.projktSens.shared;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicInteger;

public final class WaitForObject<T> {
    @Nullable
    private volatile T value;

    private final AtomicInteger isLocked = new AtomicInteger();

    private final Object lock = new Object();
    private final Object waitLock = new Object();

    @NotNull
    public T get() {
        synchronized (lock) {
            T v = value;
            if (v != null) {
                return v;
            }
        }

        synchronized (waitLock) {
            isLocked.set(1);

            try {
                waitLock.wait();
            } catch (InterruptedException e) {
                // why to handle it?
            }
        }

        //noinspection ConstantConditions
        return value;
    }

    public void set(@NotNull T value) {
        synchronized (lock) {
            this.value = value;
        }

        if (isLocked.compareAndSet(1, 0)) {
            synchronized (waitLock) {
                waitLock.notifyAll();
            }
        }
    }
}
