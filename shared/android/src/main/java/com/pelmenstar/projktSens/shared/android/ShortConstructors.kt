package com.pelmenstar.projktSens.shared.android

import android.content.Context
import android.content.Intent
import android.os.Handler
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

/**
 * Creates [Intent]. Created instance can be modified in [block]
 */
inline fun Intent(context: Context, c: Class<*>, block: Intent.() -> Unit): Intent {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return Intent(context, c).apply(block)
}