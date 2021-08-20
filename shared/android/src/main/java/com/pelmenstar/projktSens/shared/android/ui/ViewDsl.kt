@file:Suppress("NOTHING_TO_INLINE", "FunctionName")

package com.pelmenstar.projktSens.shared.android.ui

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatSpinner
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

inline fun frameLayoutParams(width: Int, height: Int): FrameLayout.LayoutParams {
    return FrameLayout.LayoutParams(width, height)
}

inline fun frameLayoutParams(
    width: Int,
    height: Int,
    block: FrameLayout.LayoutParams.() -> Unit
): FrameLayout.LayoutParams {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return frameLayoutParams(width, height).apply(block)
}

inline fun View.frameLayoutParams(width: Int, height: Int) {
    layoutParams = FrameLayout.LayoutParams(width, height)
}

inline fun View.frameLayoutParams(
    width: Int,
    height: Int,
    block: FrameLayout.LayoutParams.() -> Unit
) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    layoutParams = FrameLayout.LayoutParams(width, height).apply(block)
}

inline fun linearLayoutParams(width: Int, height: Int): LinearLayout.LayoutParams {
    return LinearLayout.LayoutParams(width, height)
}

inline fun linearLayoutParams(
    width: Int,
    height: Int,
    block: LinearLayout.LayoutParams.() -> Unit
): LinearLayout.LayoutParams {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return linearLayoutParams(width, height).apply(block)
}

inline fun View.linearLayoutParams(width: Int, height: Int) {
    layoutParams = LinearLayout.LayoutParams(width, height)
}

inline fun View.linearLayoutParams(
    width: Int,
    height: Int,
    block: LinearLayout.LayoutParams.() -> Unit
) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    layoutParams = LinearLayout.LayoutParams(width, height).apply(block)
}

inline fun View.gridLayoutParams(rowSpec: GridLayout.Spec, columnSpec: GridLayout.Spec) {
    layoutParams = GridLayout.LayoutParams(rowSpec, columnSpec)
}

inline fun View.gridLayoutParams(
    rowSpec: GridLayout.Spec,
    columnSpec: GridLayout.Spec,
    block: GridLayout.LayoutParams.() -> Unit
) {
    layoutParams = GridLayout.LayoutParams(rowSpec, columnSpec).apply(block)
}

inline fun ViewGroup.View(block: View.() -> Unit): View {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return addApply(View(context), block)
}

inline fun ViewGroup.ScrollableCalendarView(block: ScrollableCalendarView.() -> Unit): ScrollableCalendarView {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return addApply(ScrollableCalendarView(context), block)
}

inline fun ViewGroup.TimeTextView(block: TimeTextView.() -> Unit): TimeTextView {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return addApply(TimeTextView(context), block)
}

inline fun ViewGroup.TimePrefixTextView(block: TimePrefixTextView.() -> Unit): TimePrefixTextView {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return addApply(TimePrefixTextView(context), block)
}

inline fun ViewGroup.Button(block: MaterialButton.() -> Unit): MaterialButton {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return addApply(MaterialButton(context), block)
}

inline fun ViewGroup.Button(defStyleAttr: Int, block: MaterialButton.() -> Unit): MaterialButton {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return addApply(MaterialButton(context, null, defStyleAttr), block)
}

inline fun ViewGroup.FrameLayout(block: FrameLayout.() -> Unit): FrameLayout {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return addApply(FrameLayout(context), block)
}

inline fun ViewGroup.ScrollView(block: ScrollView.() -> Unit): ScrollView {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return addApply(ScrollView(context), block)
}

inline fun ViewGroup.LinearLayout(block: LinearLayout.() -> Unit): LinearLayout {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return addApply(LinearLayout(context), block)
}

inline fun ViewGroup.GridLayout(block: GridLayout.() -> Unit): GridLayout {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return addApply(GridLayout(context), block)
}

inline fun ViewGroup.TextView(block: MaterialTextView.() -> Unit): TextView {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return addApply(MaterialTextView(context), block)
}

inline fun ViewGroup.PrefixTextView(block: PrefixTextView.() -> Unit): PrefixTextView {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return addApply(PrefixTextView(context), block)
}

inline fun ViewGroup.TransitionView(block: TransitionView.() -> Unit): TransitionView {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return addApply(TransitionView(context), block)
}

inline fun ViewGroup.EditText(block: AppCompatEditText.() -> Unit): EditText {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return addApply(AppCompatEditText(context), block)
}

inline fun ViewGroup.Spinner(block: AppCompatSpinner.() -> Unit): AppCompatSpinner {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return addApply(AppCompatSpinner(context), block)
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

inline fun EditText(context: Context, block: AppCompatEditText.() -> Unit): AppCompatEditText {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return AppCompatEditText(context).apply(block)
}

inline fun <TView : View> ViewGroup.addApply(view: TView, block: TView.() -> Unit): TView {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    view.block()
    addView(view)

    return view
}