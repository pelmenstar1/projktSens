package com.pelmenstar.projktSens.shared.android.ext

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Creates [Intent]. Created instance can be modified in [block]
 */
inline fun Intent(context: Context, c: Class<*>, block: Intent.() -> Unit): Intent {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return Intent(context, c).apply(block)
}

private inline fun <T> Intent.fromExtras(key: String, method: Bundle.(String) -> T?): T {
    return extras?.method(key) ?: throw NullPointerException("Key '$key' is null or not exists")
}

fun <T : Parcelable> Intent.getParcelableExtraNotNull(key: String): T {
    return fromExtras(key) { getParcelable(it) }
}

fun Intent.getStringArrayNotNull(key: String): Array<String> {
    return fromExtras(key) { getStringArray(it) }
}

fun Intent.getStringNotNull(key: String): String {
    return fromExtras(key) { getString(key) }
}