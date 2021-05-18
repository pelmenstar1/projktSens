package com.pelmenstar.projktSens.shared

/**
 * Contains types of result, value of which is represented in [Unit].
 * The main reason to use [UnitResult.SUCCESS] and not to use [Result.success] is that
 * all the same [Unit] contains no value,
 * so constant reference can be passed to methods that require [Result] of [Unit] type
 */
object UnitResult {
    /**
     * Read-only reference to [Result] of [Unit] type
     */
    @JvmField
    val SUCCESS = Result.success(Unit)
}