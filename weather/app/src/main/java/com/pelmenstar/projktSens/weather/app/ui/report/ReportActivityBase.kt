package com.pelmenstar.projktSens.weather.app.ui.report

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.Gravity
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.pelmenstar.projktSens.shared.android.Message
import com.pelmenstar.projktSens.shared.android.ui.*
import com.pelmenstar.projktSens.shared.serialization.ObjectSerializer
import com.pelmenstar.projktSens.shared.serialization.Serializable
import com.pelmenstar.projktSens.weather.app.R
import com.pelmenstar.projktSens.weather.app.di.DaggerAppComponent
import com.pelmenstar.projktSens.weather.models.WeatherDataSource
import kotlinx.coroutines.*

abstract class ReportActivityBase<TReport : Any> protected constructor(private val serializer: ObjectSerializer<TReport>) : HomeButtonSupportActivity() {
    private var transView: TransitionView? = null

    @Volatile
    private var report: TReport? = null

    private var loadReportJob: Job? = null

    @Volatile
    private var status = STATUS_NO_DATA
    private val lock = Any()

    @Suppress("LeakingThis") // this reference will be cleared in onDestroy()
    private val mainThread = MainThreadHandler(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            status = savedInstanceState.getByte(STATE_STATUS).toInt()

            val view: View
            when (status) {
                STATUS_NO_DATA -> {
                    view = createNoDataView()
                }
                STATUS_ERROR -> {
                    view = createErrorView()
                }
                STATUS_OK -> {
                    val rawData = savedInstanceState.getByteArray(STATE_REPORT)
                    if (rawData == null) {
                        Log.e(TAG, "Invalid saved state. STATE_REPORT property is null")
                        startLoadingReport()
                        return
                    }

                    val report = Serializable.ofByteArray(rawData, serializer)
                    this.report = report

                    view = createChartView(report)
                }
                else -> {
                    Log.e(TAG, "Invalid status in state")
                    startLoadingReport()
                    return
                }
            }

            setContentView(view)
        }

        startLoadingReport()
    }

    private fun startLoadingReport() {
        if(loadReportJob != null) {
            Log.e(TAG, "loadReportJob is still running")
            return
        }

        setContentView(createAnimationView())

        loadReportJob = GlobalScope.launch(Dispatchers.Default) {
            val component = DaggerAppComponent.create()
            val dataSource = component.dataSource()

            var view: View

            try {
                val report = loadReport(dataSource)

                if (report == null) {
                    status = STATUS_NO_DATA
                    view = createNoDataView()
                } else {
                    status = STATUS_OK
                    view = createChartView(report)
                }

                this@ReportActivityBase.report = report
            } catch (e: Exception) {
                Log.e(TAG, "when loading data", e)

                status = STATUS_ERROR
                view = createErrorView()
            }

            synchronized(lock) {
                transView?.stopAnimation()
                transView = null
            }

            mainThread.sendMessage(Message {
                what = MSG_SET_CONTENT_VIEW
                obj = view
            })
        }.also { job ->
            job.invokeOnCompletion { loadReportJob = null }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putInt(STATE_STATUS, status)

        synchronized(lock) {
            val report = report
            if (report != null) {
                outState.putByteArray(STATE_REPORT, Serializable.toByteArray(report, serializer))
            }
        }
    }

    private fun createNoDataView(): View {
        val context = this

        return FrameLayout(this) {
            TextView {
                frameLayoutParams(WRAP_CONTENT, WRAP_CONTENT) {
                    gravity = Gravity.CENTER_HORIZONTAL or Gravity.TOP
                }

                TextAppearance(context, R.style.TextAppearance_MaterialComponents_Headline5).apply(this)
                text = resources.getText(R.string.noData)
            }
        }
    }

    private fun createErrorView(): View {
        val context = this
        val res = resources

        val retryButtonSize = res.getDimensionPixelSize(R.dimen.reportActivity_errorView_retryButtonSize)
        val retryButtonTopMargin = res.getDimensionPixelSize(R.dimen.reportActivity_errorView_retryButtonTopMargin)

        return LinearLayout(this) {
            TextView {
                linearLayoutParams(WRAP_CONTENT, WRAP_CONTENT) {
                    gravity = Gravity.CENTER_HORIZONTAL
                }

                applyTextAppearance(context, R.style.TextAppearance_MaterialComponents_Headline3)
                text = res.getText(R.string.errorOccurred)
            }

            Button {
                linearLayoutParams(retryButtonSize, retryButtonSize) {
                    gravity = Gravity.CENTER_HORIZONTAL
                    topMargin = retryButtonTopMargin
                }

                background = ResourcesCompat.getDrawable(res, R.drawable.ic_retry, theme)
                setOnClickListener {
                    startLoadingReport()
                }
            }
        }
    }

    private fun createAnimationView(): View {
        val size = resources.getDimensionPixelSize(R.dimen.reportActivity_transitionViewSize)

        return FrameLayout(this) {
            TransitionView {
                frameLayoutParams(size, size) {
                    gravity = Gravity.CENTER
                }

                startAnimation()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        synchronized(lock) {
            transView?.stopAnimation()
        }

        mainThread.removeCallbacksAndMessages(null)
        mainThread.activity = null
    }

    protected abstract fun createChartView(report: TReport): View
    protected abstract suspend fun loadReport(dataSource: WeatherDataSource): TReport?

    private class MainThreadHandler(@JvmField var activity: ReportActivityBase<*>?): Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            val activity = activity
            if(activity == null) {
                Log.e(TAG, "activity in MainThreadHandler is null")
                return
            }

            when(msg.what) {
                MSG_SET_CONTENT_VIEW -> {
                    activity.setContentView(msg.obj as View)
                }
            }
        }
    }

    companion object {
        private const val MSG_SET_CONTENT_VIEW = 0

        private const val STATUS_NO_DATA = 0
        private const val STATUS_ERROR = 1
        private const val STATUS_OK = 2

        private const val TAG = "ReportActivityBase"

        private const val STATE_REPORT = "state:ReportActivityBase:report"
        private const val STATE_STATUS = "state:ReportActivityBase:status"
    }
}