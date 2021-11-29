package com.pelmenstar.projktSens.weather.app.ui.home

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.widget.Button
import android.widget.CalendarView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView
import com.pelmenstar.projktSens.shared.android.ext.Message
import com.pelmenstar.projktSens.shared.android.ui.*
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
    private var transitionView: TransitionView? = null
    private var failedToLoadTextView: TextView? = null
    private var noDataTextView: TextView? = null
    private var retryButton: Button? = null

    private val mainThread = MainThreadHandler()

    init {
        // for STATE_FAILED_TO_LOAD state.
        // another states have single view
        orientation = VERTICAL

        calendarView = ScrollableCalendarView(context).apply {
            linearLayoutParams(MATCH_PARENT, MATCH_PARENT)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        loadMinMax()
    }

    private fun createTransitionView(): TransitionView {
        return TransitionView(context).apply {
            val size = resources.getDimensionPixelSize(R.dimen.lazyCalendar_transitionViewSize)
            linearLayoutParams(size, size) {
                gravity = Gravity.CENTER
            }

            colorTransition = LinearColorTransition.fromArrayRes(
                context,
                R.array.defaultTransitionColors
            )
        }
    }

    private fun createFailedToLoadTextView(): TextView {
        return MaterialTextView(context).apply {
            linearLayoutParams(WRAP_CONTENT, WRAP_CONTENT) {
                gravity = Gravity.CENTER_HORIZONTAL
            }

            setText(R.string.lazyLoadingCalendar_failedToLoad)
        }
    }

    private fun createRetryButton(): Button {
        return MaterialButton(context).apply {
            val size = resources.getDimensionPixelSize(R.dimen.lazyCalendar_retryButtonSize)
            linearLayoutParams(size, size) {
                gravity = Gravity.CENTER_HORIZONTAL
            }

            setBackgroundResource(R.drawable.ic_retry)
            setOnClickListener { loadMinMax() }
        }
    }

    private fun createNoDataView(): TextView {
        return MaterialTextView(context).apply {
            linearLayoutParams(WRAP_CONTENT, WRAP_CONTENT) {
                gravity = Gravity.CENTER_HORIZONTAL
            }

            setText(R.string.noData)
        }
    }

    private fun loadMinMax() {
        setState(STATE_LOADING)
        val handler = loadMinMaxHandler
        if (handler != null) {
            scope.launch(Dispatchers.IO) {
                try {
                    val range = handler.load()

                    val newState = if (range != null) {
                        postSetMinMax(range.start, range.endInclusive)

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

    fun setState(state: Int) {
        removeAllViewsInLayout()
        if (state != STATE_LOADING) {
            transitionView?.stopTransition()
        }

        when (state) {
            STATE_LOADED -> {
                transitionView = null
                failedToLoadTextView = null
                retryButton = null
                noDataTextView = null

                addView(calendarView)
            }
            STATE_LOADING -> {
                var tView = transitionView
                if (tView == null) {
                    tView = createTransitionView()
                    transitionView = tView
                }

                tView.startTransition()
                addView(tView)
            }
            STATE_FAILED_TO_LOAD -> {
                if (failedToLoadTextView == null) {
                    failedToLoadTextView = createFailedToLoadTextView()
                }

                if (retryButton == null) {
                    retryButton = createRetryButton()
                }

                addView(failedToLoadTextView)
                addView(retryButton)
            }
            STATE_NO_DATA -> {
                if (noDataTextView == null) {
                    noDataTextView = createNoDataView()
                }

                addView(noDataTextView)
            }
        }
    }

    fun postSetState(state: Int) {
        mainThread.sendMessage(Message {
            what = MSG_SET_STATE
            obj = this@LazyLoadingCalendarView
            arg1 = state
        })
    }

    fun setMinMax(@ShortDateInt minDate: Int, @ShortDateInt maxDate: Int) {
        calendarView.minDate = ShortDateTime.startOfDayToEpochSecond(minDate) * 1000
        calendarView.maxDate = ShortDateTime.endOfDayToEpochSecond(maxDate) * 1000
    }

    fun postSetMinMax(@ShortDateInt minDate: Int, @ShortDateInt maxDate: Int) {
        mainThread.sendMessage(Message {
            what = MSG_SET_CALENDAR_MIN_MAX
            obj = this@LazyLoadingCalendarView
            arg1 = minDate
            arg2 = maxDate
        })
    }

    fun setOnDateChangeListener(listener: CalendarView.OnDateChangeListener) {
        calendarView.setOnDateChangeListener(listener)
    }

    private class MainThreadHandler : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_SET_STATE -> {
                    val v = msg.obj as LazyLoadingCalendarView
                    v.setState(msg.arg1)
                }
                MSG_SET_CALENDAR_MIN_MAX -> {
                    val v = msg.obj as LazyLoadingCalendarView
                    v.setMinMax(msg.arg1, msg.arg2)
                }
            }
        }
    }

    companion object {
        private val scope = CoroutineScope(Dispatchers.Default)
        private const val TAG = "LazyLdCalendar"

        const val STATE_LOADING = 0
        const val STATE_LOADED = 1
        const val STATE_FAILED_TO_LOAD = 2
        const val STATE_NO_DATA = 3

        private const val MSG_SET_STATE = 0
        private const val MSG_SET_CALENDAR_MIN_MAX = 1
    }
}