@file:Suppress("NOTHING_TO_INLINE")

package com.pelmenstar.projktSens.shared

inline fun String.getChars(srcBegin: Int, srcEnd: Int, buffer: CharArray, dstBegin: Int) {
    (this as java.lang.String).getChars(srcBegin, srcEnd, buffer, dstBegin)
}

