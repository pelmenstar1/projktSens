package com.pelmenstar.projktSens.shared;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class InvokeOnFirstSet<T> {
    @Nullable
    private volatile T value;

    @Nullable
    private volatile Runnable callback;

    private boolean isCallbackCalled;

    public synchronized void setCallback(@Nullable Runnable callback) {
        this.callback = callback;
    }

    @Nullable
    public synchronized T get() {
        return value;
    }

    @NotNull
    public synchronized T getOrThrowIfNull() {
        T value = this.value;
        if(value != null) {
            return value;
        } else {
            throw new RuntimeException("Value is null");
        }
    }

    public synchronized void set(@NotNull T value) {
        this.value = value;

        if(!isCallbackCalled) {
            isCallbackCalled = true;

            Runnable callback = this.callback;
            if (callback != null) {
                callback.run();
                this.callback = null;
            }
        }
    }
}
