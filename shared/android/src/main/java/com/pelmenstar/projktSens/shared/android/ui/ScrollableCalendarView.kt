package com.pelmenstar.projktSens.shared.android.ui

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.CalendarView

/**
 * [CalendarView] that gives possibility to user to scroll [CalendarView]
 */
class ScrollableCalendarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.calendarViewStyle,
    defStyleRes: Int = 0
) : CalendarView(context, attrs, defStyleAttr, defStyleRes) {
    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        val action = ev.actionMasked
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_UP) {
            parent?.requestDisallowInterceptTouchEvent(true)
        }

        return false
    }
}