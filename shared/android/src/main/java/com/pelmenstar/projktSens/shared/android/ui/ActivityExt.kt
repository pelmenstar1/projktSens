package com.pelmenstar.projktSens.shared.android.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import androidx.annotation.ColorInt
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

@get:ColorInt
val Activity.surfaceBackgroundColor: Int
    get() {
        val window = window
        if (window != null) {
            val background = window.decorView.background
            if (background is ColorDrawable) {
                return background.color
            }
        }

        return Color.TRANSPARENT
    }
