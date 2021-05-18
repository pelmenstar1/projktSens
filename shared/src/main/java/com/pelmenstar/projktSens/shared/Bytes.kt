@file:Suppress("NOTHING_TO_INLINE")

package com.pelmenstar.projktSens.shared

import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Creates [ByteArray] with specified [size] and allows to mutate it in [builder].
 */
inline fun buildByteArray(size: Int, builder: ByteArray.() -> Unit): ByteArray {
    contract {
        callsInPlace(builder, InvocationKind.EXACTLY_ONCE)
    }

    return ByteArray(size).apply(builder)
}

/**
 * Writes [Int] to [ByteArray] as 4 bytes starting from [offset]. Little endian
 */
inline fun ByteArray.writeInt(offset: Int, value: Int) {
    this[offset] = value.toByte()
    this[offset + 1] = (value shr 8).toByte()
    this[offset + 2] = (value shr 16).toByte()
    this[offset + 3] = (value shr 24).toByte()
}

/**
 * Reads [Int] from [ByteArray] starting from [offset]. Little endian
 */
inline fun ByteArray.getInt(offset: Int): Int {
    return this[offset].toInt() and 0xFF or
            (this[offset + 1].toInt() and 0xFF shl 8) or
            (this[offset + 2].toInt() and 0xFF shl 16) or
            (this[offset + 3].toInt() and 0xFF shl 24)
}

/**
 * Writes [Long] to [ByteArray] as 8 bytes starting from [offset]. Little endian
 */
inline fun ByteArray.writeLong(offset: Int, value: Long) {
    this[offset] = value.toByte()
    this[offset + 1] = (value shr 8).toByte()
    this[offset + 2] = (value shr 16).toByte()
    this[offset + 3] = (value shr 24).toByte()
    this[offset + 4] = (value shr 32).toByte()
    this[offset + 5] = (value shr 40).toByte()
    this[offset + 6] = (value shr 48).toByte()
    this[offset + 7] = (value shr 56).toByte()
}

/**
 * Reads [Long] from [ByteArray] starting from [offset]. Little endian
 */
inline fun ByteArray.getLong(offset: Int): Long {
    return this[offset].toLong() and 0xFF or
            (this[offset + 1].toLong() and 0xFF shl 8) or
            (this[offset + 2].toLong() and 0xFF shl 16) or
            (this[offset + 3].toLong() and 0xFF shl 24) or
            (this[offset + 4].toLong() and 0xFF shl 32) or
            (this[offset + 5].toLong() and 0xFF shl 40) or
            (this[offset + 6].toLong() and 0xFF shl 48) or
            (this[offset + 7].toLong() and 0xFF shl 56)
}

/**
 * Writes [Short] to [ByteArray] as 2 bytes starting from [offset]. Little endian
 */
inline fun ByteArray.writeShort(offset: Int, value: Short) {
    this[offset] = value.toByte()
    this[offset + 1] = (value.toInt() shr 8).toByte()
}

/**
 * Reads [Short] from [ByteArray] starting from [offset]. Little endian
 */
inline fun ByteArray.getShort(offset: Int): Short {
    return (this[offset].toInt() and 0xFF or (this[offset + 1].toInt() and 0xFF shl 8)).toShort()
}

/**
 * Writes [Float] (float will be interpreted as int) to [ByteArray] starting from [offset]. Little endian
 */
inline fun ByteArray.writeFloat(offset: Int, value: Float) {
    writeInt(offset, value.toBits())
}

/**
 * Reads [Float] from [ByteArray] starting from [offset]. Little endian
 */
inline fun ByteArray.getFloat(offset: Int): Float {
    return getInt(offset).intBitsToFloat()
}

/**
 * Interprets int bits as float. Note that it's not the same as `intVal.toFloat()`
 */
inline fun Int.intBitsToFloat(): Float {
    return java.lang.Float.intBitsToFloat(this)
}
