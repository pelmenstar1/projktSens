@file:Suppress("NOTHING_TO_INLINE")

package com.pelmenstar.projktSens.shared.android

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteStatement
import android.os.CancellationSignal
import com.pelmenstar.projktSens.shared.UnitResult
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.suspendCoroutine

/**
 * Creates [CancellationSignal] from [CancellableContinuation]
 */
inline fun<T:Any> CancellableContinuation<T>.toCancellationSignal(): CancellationSignal {
    val signal = CancellationSignal()
    signal.setOnCancelListener { cancel() }

    return signal
}

/**
 * Works the same as [SQLiteDatabase.query],
 * but it suspends a thread for period of executing [SQLiteDatabase.query]
 */
suspend inline fun SQLiteDatabase.querySuspend(sql: String): Cursor {
    return suspendCancellableCoroutine { cont ->
        val c = rawQueryWithFactory(
            null,
            sql,
            null,
            null,
            cont.toCancellationSignal()
        )

        cont.resumeWith(Result.success(c))
    }
}

/**
 * Works the same as [SQLiteStatement.executeUpdateDelete],
 * but it suspends a thread for period of executing [SQLiteStatement.executeUpdateDelete]
 */
suspend inline fun SQLiteStatement.executeUpdateDeleteSuspend() {
    return suspendCoroutine { cont ->
        executeUpdateDelete()

        cont.resumeWith(UnitResult.SUCCESS)
    }
}

/**
 * Works the same as [SQLiteStatement.executeInsert],
 * but it suspends a thread for period of executing [SQLiteStatement.executeInsert]
 */
suspend inline fun SQLiteStatement.executeInsertSuspend() {
    return suspendCoroutine { cont ->
        executeInsert()

        cont.resumeWith(UnitResult.SUCCESS)
    }
}