package com.pelmenstar.projktSens.shared.android.ui

import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

inline fun Activity.content(block: () -> View) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    setContentView(block())
}

inline fun AppCompatActivity.actionBar(block: ActionBar.() -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    supportActionBar?.block()
}

fun Activity.requireIntent(): Intent {
    return intent ?: throw IllegalStateException("Intent is null")
}

