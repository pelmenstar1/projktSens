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
import com.pelmenstar.projktSens.weather.app.di.AppModule
import com.pelmenstar.projktSens.weather.app.di.DaggerAppComponent
import com.pelmenstar.projktSens.weather.models.WeatherDataSource
import kotlinx.coroutines.*

abstract class ReportActivityBase<TReport : Any> protected constructor(private val serializer: ObjectSerializer<TReport>) : HomeButtonSupportActivity() {
    @Volatile
    private var report: TReport? = null

    private var loadReportJob: Job? = null

    private var status = STATUS_NO_DATA
    private val lock = Any()

    @Suppress("LeakingThis") // this reference will be cleared in onDestroy()
    private val mainThread = MainThreadHandler(this)

    private var transitionView: TransitionView? = null

    private var loadingView: View? = null
    private var noDataView: View? = null
    private var errorView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        run try_load_from_saved_state@ {
            if (savedInstanceState != null) {
                status = savedInstanceState.getByte(STATE_STATUS).toInt()

                if (status == STATUS_OK) {
                    val rawData = savedInstanceState.getByteArray(STATE_REPORT)
                    if (rawData == null) {
                        Log.e(TAG, "Invalid saved state. STATE_REPORT property is null")

                        return@try_load_from_saved_state
                    }

                    report = Serializable.ofByteArray(rawData, serializer)
                }

                setStatus(status)
            }
        }

        startLoadingReport()
    }

    private fun startLoadingReport() {
        if(loadReportJob != null) {
            Log.e(TAG, "loadReportJob is still running")
            return
        }

        setStatus(STATUS_LOADING)

        loadReportJob = scope.launch {
            val component = DaggerAppComponent
                .builder()
                .appModule(AppModule(this@ReportActivityBase))
                .build()

            val dataSource = component.dataSource()

            var newStatus: Int
            try {
                val r = loadReport(dataSource)

                newStatus = if (r == null) {
                    STATUS_NO_DATA
                } else {
                    STATUS_OK
                }

                report = r
            } catch (e: Exception) {
                Log.e(TAG, "when loading data", e)

                newStatus = STATUS_ERROR
            }

            mainThread.sendMessage(Message {
                what = MSG_SET_STATUS
                arg1 = newStatus
            })
        }.also { job ->
            job.invokeOnCompletion { loadReportJob = null }
        }
    }

    private fun setStatus(status: Int) {
        this.status = status

        if(status != STATUS_LOADING) {
            transitionView?.stopAnimation()
        }

        when(status) {
            STATUS_LOADING -> {
                if(loadingView == null) {
                    loadingView = createLoadingView()
                }

                transitionView?.startAnimation()
                setContentView(loadingView)
            }
            STATUS_NO_DATA -> {
                if(noDataView == null) {
                    noDataView = createNoDataView()
                }

                setContentView(noDataView)
            }
            STATUS_ERROR -> {
                if(errorView == null) {
                    errorView = createErrorView()
                }

                setContentView(errorView)
            }
            STATUS_OK -> {
                val r = report
                if(r == null) {
                    Log.e(TAG, "setStatus(STATUS_OK), but report is null")
                    return
                }

                transitionView = null

                noDataView = null
                errorView = null
                loadingView = null

                setContentView(createChartView(r))
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

                applyTextAppearance(context, R.style.TextAppearance_MaterialComponents_Headline4)
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
            orientation = android.widget.LinearLayout.VERTICAL

            TextView {
                linearLayoutParams(WRAP_CONTENT, WRAP_CONTENT) {
                    gravity = Gravity.CENTER_HORIZONTAL
                }

                applyTextAppearance(context, R.style.TextAppearance_MaterialComponents_Headline5)
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

    private fun createLoadingView(): View {
        val size = resources.getDimensionPixelSize(R.dimen.reportActivity_transitionViewSize)
        val context = this

        return FrameLayout(context) {
            TransitionView {
                frameLayoutParams(size, size) {
                    gravity = Gravity.CENTER
                }

                transition = LinearColorTransition.fromArrayRes(context, R.array.defaultTransitionColors)
                transitionView = this
            }
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

    override fun onDestroy() {
        super.onDestroy()

        synchronized(lock) {
            transitionView?.stopAnimation()
        }
        loadReportJob?.cancel()

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
                MSG_SET_STATUS -> {
                    activity.setStatus(msg.arg1)
                }
            }
        }
    }

    companion object {
        private val scope = CoroutineScope(Dispatchers.Default + CoroutineName("ReportScope"))
        private const val MSG_SET_STATUS = 0

        private const val STATUS_LOADING = 3
        private const val STATUS_NO_DATA = 0
        private const val STATUS_ERROR = 1
        private const val STATUS_OK = 2

        private const val TAG = "ReportActivityBase"

        private const val STATE_REPORT = "state:ReportActivityBase:report"
        private const val STATE_STATUS = "state:ReportActivityBase:status"
    }
}