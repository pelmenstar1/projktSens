package com.pelmenstar.projktSens.shared.android.ui

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.CalendarView
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes

/**
 * [CalendarView] that gives possibility to user to scroll [CalendarView]
 */
class ScrollableCalendarView: CalendarView {
    constructor(context: Context):
            super(context)
    constructor(context: Context, attrs: AttributeSet?):
            super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int):
            super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int, @StyleRes defStyleRes: Int):
            super(context, attrs, defStyleAttr, defStyleRes)

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        val action = ev.actionMasked
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_UP) {
            parent?.requestDisallowInterceptTouchEvent(true)
        }

        return false
    }
}