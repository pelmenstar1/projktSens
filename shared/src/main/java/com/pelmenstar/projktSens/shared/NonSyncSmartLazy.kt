package com.pelmenstar.projktSens.shared

class NonSyncSmartLazy<T : Any>(private val creator: () -> T) {
    private var value: T? = null

    fun get(): T {
        var v = value
        if(v == null) {
            v = creator()
            value = v
        }

        return v
    }

    fun setNull() {
        value = null
    }
}

fun<T : Any> smartLazy(creator: () -> T): NonSyncSmartLazy<T> {
    return NonSyncSmartLazy(creator)
}