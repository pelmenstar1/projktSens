@file:Suppress("NOTHING_TO_INLINE", "ReplaceJavaStaticMethodWithKotlinAnalog")

package com.pelmenstar.projktSens.shared

/**
 * Non-mathematically rounds receiver [Float] to 1 decimal place.
 * Examples:
 *
 * ```7.698569f.round()``` => 7.6
 *
 * ```43.21f.round()``` => 43.2
 *
 * ```-323.433f.round()``` => -323.4
 */
inline fun Float.round(): Float = (this * 10f).toInt() / 10f