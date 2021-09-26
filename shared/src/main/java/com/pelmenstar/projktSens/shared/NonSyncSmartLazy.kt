package com.pelmenstar.projktSens.shared

abstract class NonSyncSmartLazy<T : Any> {
    private var value: T? = null

    fun get(): T {
        var v = value
        if(v == null) {
            v = create()
            value = v
        }

        return v
    }

    fun setNull() {
        value = null
    }

    protected abstract fun create(): T
}

inline fun<T : Any> smartLazy(crossinline creator: () -> T): NonSyncSmartLazy<T> {
    return object: NonSyncSmartLazy<T>() {
        override fun create(): T = creator()
    }
}