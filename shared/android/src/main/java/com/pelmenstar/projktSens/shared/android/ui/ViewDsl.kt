@file:Suppress("NOTHING_TO_INLINE", "FunctionName")

package com.pelmenstar.projktSens.shared.android.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * The same as [ViewGroup.LayoutParams.MATCH_PARENT]
 */
const val MATCH_PARENT = ViewGroup.LayoutParams.MATCH_PARENT

/**
 * The same as [ViewGroup.LayoutParams.WRAP_CONTENT]
 */
const val WRAP_CONTENT = ViewGroup.LayoutParams.WRAP_CONTENT

inline fun View.frameLayoutParams(width: Int, height: Int) {
    layoutParams = FrameLayout.LayoutParams(width, height)
}

inline fun View.frameLayoutParams(width: Int, height: Int, block: FrameLayout.LayoutParams.() -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    layoutParams =  FrameLayout.LayoutParams(width, height).apply(block)
}

inline fun View.linearLayoutParams(width: Int, height: Int) {
    layoutParams = LinearLayout.LayoutParams(width, height)
}

inline fun View.linearLayoutParams(width: Int, height: Int, block: LinearLayout.LayoutParams.() -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    layoutParams = LinearLayout.LayoutParams(width, height).apply(block)
}

inline fun View.coordinatorLayoutParams(width: Int, height: Int) {
    layoutParams = CoordinatorLayout.LayoutParams(width, height)
}

inline fun View.coordinatorLayoutParams(width: Int, height: Int, block: CoordinatorLayout.LayoutParams.() -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    layoutParams = CoordinatorLayout.LayoutParams(width, height).apply(block)
}

inline fun ViewGroup.View(block: View.() -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    addApply(View(context), block)
}

inline fun ViewGroup.CoordinatorLayout(block: CoordinatorLayout.() -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    addApply(CoordinatorLayout(context), block)
}

inline fun ViewGroup.ScrollableCalendarView(block: ScrollableCalendarView.() -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    addApply(ScrollableCalendarView(context), block)
}

inline fun ViewGroup.TimeTextView(block: TimeTextView.() -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    addApply(TimeTextView(context), block)
}

inline fun ViewGroup.TimePrefixTextView(block: TimePrefixTextView.() -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    addApply(TimePrefixTextView(context), block)
}

inline fun ViewGroup.Button(block: MaterialButton.() -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    addApply(MaterialButton(context), block)
}

inline fun ViewGroup.Button(defStyleAttr: Int, block: MaterialButton.() -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    addApply(MaterialButton(context, null, defStyleAttr), block)
}

inline fun ViewGroup.FrameLayout(block: FrameLayout.() -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    addApply(FrameLayout(context), block)
}

inline fun ViewGroup.ScrollView(block: ScrollView.() -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    addApply(ScrollView(context), block)
}

inline fun ViewGroup.LinearLayout(block: LinearLayout.() -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    addApply(LinearLayout(context), block)
}

inline fun ViewGroup.TextView(block: MaterialTextView.() -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    addApply(MaterialTextView(context), block)
}

inline fun ViewGroup.PrefixTextView(block: PrefixTextView.() -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    addApply(PrefixTextView(context), block)
}

inline fun ViewGroup.TransitionView(block: TransitionView.() -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    addApply(TransitionView(context), block)
}

inline fun ViewGroup.EditText(block: AppCompatEditText.() -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    addApply(AppCompatEditText(context), block)
}

inline fun LinearLayout(context: Context, block: LinearLayout.() -> Unit): LinearLayout {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return LinearLayout(context).apply(block)
}

inline fun FrameLayout(context: Context, block: FrameLayout.() -> Unit): FrameLayout {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return FrameLayout(context).apply(block)
}

inline fun ScrollView(context: Context, block: ScrollView.() -> Unit): ScrollView {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return ScrollView(context).apply(block)
}

inline fun<TView:View> ViewGroup.addApply(view: TView, block: TView.() -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    view.apply(block)
    addView(view)
}

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

inline fun AppCompatActivity.requireIntent(): Intent {
    return intent ?: throw IllegalStateException("Intent is null")
}
