@file:Suppress("NOTHING_TO_INLINE")

package com.pelmenstar.projktSens.shared

inline fun String.getChars(srcBegin: Int, srcEnd: Int, buffer: CharArray, dstBegin: Int) {
    (this as java.lang.String).getChars(srcBegin, srcEnd, buffer, dstBegin)
}

/**
 * Appends [Array] of ordinary [Object]'s to the end of [StringBuilder] in format:
 *
 * - if array is `null`, appends 'null' string
 *
 * - if array is not `null`, appends [[`1st element`, `2nd element`, `3rd element`, ...]]
 */
inline fun StringBuilder.appendArray(array: Array<out Any>?) {
    StringUtils.appendArray(array, this)
}

inline fun StringBuilder.appendArray(array: Array<out AppendableToStringBuilder>?) {
    StringUtils.appendArray(array, this)
}