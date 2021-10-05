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
    Bytes.writeInt(value, this, offset)
}

/**
 * Reads [Int] from [ByteArray] starting from [offset]. Little endian
 */
inline fun ByteArray.getInt(offset: Int): Int {
    return Bytes.readInt(this, offset)
}

/**
 * Writes [Long] to [ByteArray] as 8 bytes starting from [offset]. Little endian
 */
inline fun ByteArray.writeLong(offset: Int, value: Long) {
    Bytes.writeLong(value, this, offset)
}

/**
 * Reads [Long] from [ByteArray] starting from [offset]. Little endian
 */
inline fun ByteArray.getLong(offset: Int): Long {
    return Bytes.readLong(this, offset)
}

/**
 * Writes [Short] to [ByteArray] as 2 bytes starting from [offset]. Little endian
 */
inline fun ByteArray.writeShort(offset: Int, value: Short) {
    Bytes.writeShort(value, this, offset)
}

/**
 * Reads [Short] from [ByteArray] starting from [offset]. Little endian
 */
inline fun ByteArray.getShort(offset: Int): Short {
    return Bytes.readShort(this, offset)
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
