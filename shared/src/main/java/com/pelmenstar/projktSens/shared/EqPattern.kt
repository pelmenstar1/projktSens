package com.pelmenstar.projktSens.shared

import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Compares receiver object to [other] by ordinary pattern:
 * ```
 * if(this === other) return true
 * if(other == null || other.javaClass !== javaClass) return false
 *
 * return ...
 * ```
 *
 * Actual compare is happened in [objEquals] lambda
 */
inline fun <reified T : Any> T.equalsPattern(other: Any?, objEquals: (T) -> Boolean): Boolean {
    contract {
        callsInPlace(objEquals, InvocationKind.AT_MOST_ONCE)
    }

    if (this === other) return true
    if (other === null || other.javaClass !== T::class.java) return false

    //@Suppress("UNCHECKED_CAST")
    return objEquals(other as T)
}