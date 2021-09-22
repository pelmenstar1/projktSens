package com.pelmenstar.projktSens.shared

import kotlin.coroutines.Continuation


private val UNIT_RESULT = Result.success(Unit)

fun unitResult(): Result<Unit> = UNIT_RESULT

fun Continuation<Unit>.resumeWithSuccess() {
    resumeWith(UNIT_RESULT)
}