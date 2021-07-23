package com.pelmenstar.projktSens.weather.app.ui.home

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.widget.CalendarView
import android.widget.LinearLayout
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import com.pelmenstar.projktSens.shared.android.Message
import com.pelmenstar.projktSens.shared.android.ui.LinearColorTransition
import com.pelmenstar.projktSens.shared.android.ui.ScrollableCalendarView
import com.pelmenstar.projktSens.shared.android.ui.TransitionView
import com.pelmenstar.projktSens.shared.time.ShortDateInt
import com.pelmenstar.projktSens.shared.time.ShortDateRange
import com.pelmenstar.projktSens.shared.time.ShortDateTime
import com.pelmenstar.projktSens.weather.app.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LazyLoadingCalendarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0
) : LinearLayout(context, attrs, defStyleAttr, defStyleRes) {
    fun interface LoadMinMaxHandler {
        suspend fun load(): ShortDateRange?
    }

    var loadMinMaxHandler: LoadMinMaxHandler? = null

    private val calendarView: ScrollableCalendarView
    private val transitionView: TransitionView
    private val failedToLoadTextView: MaterialTextView
    private val noDataTextView: MaterialTextView
    private val retryButton: MaterialButton

    private val mainThread = MainThreadHandler(this)

    init {
        val res = context.resources

        // for STATE_FAILED_TO_LOAD state.
        // another states have single view
        orientation = VERTICAL

        calendarView = ScrollableCalendarView(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            )
        }
        transitionView = TransitionView(context).apply {
            val size = res.getDimensionPixelSize(R.dimen.lazyCalendar_transitionViewSize)
            layoutParams = LayoutParams(size, size).apply {
                gravity = Gravity.CENTER
            }

            transition = LinearColorTransition.fromArrayRes(context, R.array.defaultTransitionColors)
        }

        val wrapContentCenterH = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT,
        ).apply {
            gravity = Gravity.CENTER_HORIZONTAL
        }

        failedToLoadTextView = MaterialTextView(context).apply {
            layoutParams = wrapContentCenterH

            setText(R.string.lazyLoadingCalendar_failedToLoad)
        }

        retryButton = MaterialButton(context).apply {
            val size = res.getDimensionPixelSize(R.dimen.lazyCalendar_retryButtonSize)
            layoutParams = LayoutParams(size, size).apply {
                gravity = Gravity.CENTER_HORIZONTAL
            }

            setBackgroundResource(R.drawable.ic_retry)
            setOnClickListener { loadMinMax() }
        }
        noDataTextView = MaterialTextView(context).apply {
            layoutParams = wrapContentCenterH
            setText(R.string.noData)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        loadMinMax()
    }

    private fun loadMinMax() {
        setState(STATE_LOADING)
        val handler = loadMinMaxHandler
        if (handler != null) {
            scope.launch {
                try {
                    val range = handler.load()

                    val newState = if (range != null) {
                        postSetCalendarMinMax(range.start, range.endInclusive)

                        STATE_LOADED
                    } else {
                        STATE_NO_DATA
                    }

                    postSetState(newState)
                } catch (e: Exception) {
                    Log.e(TAG, null, e)
                    postSetState(STATE_FAILED_TO_LOAD)
                }
            }
        } else {
            Log.w(TAG, "Loading min-max range was started but LoadMinMaxHandler was null")
        }
    }

    private fun setState(state: Int) {
        removeAllViewsInLayout()
        if (state != STATE_LOADING) {
            transitionView.stopAnimation()
        }

        when (state) {
            STATE_LOADED -> {
                addView(calendarView)
            }
            STATE_LOADING -> {
                transitionView.startAnimation()
                addView(transitionView)
            }
            STATE_FAILED_TO_LOAD -> {
                addView(failedToLoadTextView)
                addView(retryButton)
            }
            STATE_NO_DATA -> {
                addView(noDataTextView)
            }
        }
    }

    private fun postSetState(state: Int) {
        mainThread.sendMessage(Message {
            what = MSG_SET_STATE
            arg1 = state
        })
    }

    private fun setCalendarMinMax(@ShortDateInt minDate: Int, @ShortDateInt maxDate: Int) {
        calendarView.minDate = ShortDateTime.toEpochSecond(ShortDateTime.startOfDay(minDate)) * 1000
        calendarView.maxDate = ShortDateTime.toEpochSecond(ShortDateTime.endOfDay(maxDate)) * 1000
    }

    private fun postSetCalendarMinMax(@ShortDateInt minDate: Int, @ShortDateInt maxDate: Int) {
        mainThread.sendMessage(Message {
            what = MSG_SET_CALENDAR_MIN_MAX
            arg1 = minDate
            arg2 = maxDate
        })
    }

    fun setOnDateChangeListener(listener: CalendarView.OnDateChangeListener) {
        calendarView.setOnDateChangeListener(listener)
    }

    private class MainThreadHandler(@JvmField var view: LazyLoadingCalendarView?): Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            val v = view ?: return

            when(msg.what) {
                MSG_SET_STATE -> {
                    v.setState(msg.arg1)
                }
                MSG_SET_CALENDAR_MIN_MAX -> {
                    v.setCalendarMinMax(msg.arg1, msg.arg2)
                }
            }
        }
    }

    companion object {
        private val scope = CoroutineScope(Dispatchers.Default)
        private const val TAG = "LazyLdCalendar"

        private const val STATE_LOADING = 0
        private const val STATE_LOADED = 1
        private const val STATE_FAILED_TO_LOAD = 2
        private const val STATE_NO_DATA = 3

        private const val MSG_SET_STATE = 0
        private const val MSG_SET_CALENDAR_MIN_MAX = 1
    }
}