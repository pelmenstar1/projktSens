package com.pelmenstar.projktSens.shared.android.ext

import android.os.Message
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Creates [Message]. Created instance can be modified in [block]
 */
inline fun Message(block: Message.() -> Unit): Message {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return Message.obtain().apply(block)
}