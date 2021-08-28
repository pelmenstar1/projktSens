package com.pelmenstar.projktSens.weather.app.ui.report

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.res.ResourcesCompat
import com.pelmenstar.projktSens.shared.android.NetworkUtils
import com.pelmenstar.projktSens.shared.android.ext.Message
import com.pelmenstar.projktSens.shared.android.ui.*
import com.pelmenstar.projktSens.shared.serialization.ObjectSerializer
import com.pelmenstar.projktSens.shared.serialization.Serializable
import com.pelmenstar.projktSens.weather.app.R
import com.pelmenstar.projktSens.weather.app.di.AppModule
import com.pelmenstar.projktSens.weather.app.di.DaggerAppComponent
import com.pelmenstar.projktSens.weather.models.WeatherDataSource
import kotlinx.coroutines.*

abstract class ReportActivityBase<TReport : Any> protected constructor(private val serializer: ObjectSerializer<TReport>) :
    HomeButtonSupportActivity() {
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
    private var noNetworkView: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        run try_load_from_saved_state@{
            if (savedInstanceState != null) {
                status = savedInstanceState.getByte(STATE_STATUS).toInt()

                if (status == STATUS_OK) {
                    val rawReport = savedInstanceState.getByteArray(STATE_REPORT)
                    if (rawReport == null) {
                        Log.e(TAG, "Invalid saved state. STATE_REPORT property is null")

                        return@try_load_from_saved_state
                    }

                    report = Serializable.ofByteArray(rawReport, serializer)
                }

                setStatus(status)
            }
        }

        startLoadingReport()
    }

    private fun startLoadingReport() {
        if (loadReportJob != null) {
            Log.e(TAG, "loadReportJob is still running")
            return
        }

        val component = DaggerAppComponent
            .builder()
            .appModule(AppModule(this))
            .build()

        val dataSource = component.dataSource()
        val isConnected = NetworkUtils.isConnectedToAnyNetwork(this)
        if(!isConnected) {
            setStatus(STATUS_NO_NETWORK)
            return
        } else {
            setStatus(STATUS_LOADING)
        }

        loadReportJob = scope.launch {
            var newStatus: Int
            try {
                val r = loadReport(dataSource)

                newStatus = if (r == null) {
                    STATUS_NO_DATA
                } else {
                    STATUS_OK
                }

                synchronized(lock) {
                    report = r
                }
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

        if (status != STATUS_LOADING) {
            transitionView?.stopTransition()
        }

        when (status) {
            STATUS_NO_NETWORK -> {
                if(noNetworkView == null) {
                    noNetworkView = createNoNetworkView()
                }

                setContentView(noNetworkView)
            }
            STATUS_LOADING -> {
                if (loadingView == null) {
                    loadingView = createLoadingView()
                }

                transitionView?.startTransition()
                setContentView(loadingView)
            }
            STATUS_NO_DATA -> {
                if (noDataView == null) {
                    noDataView = createNoDataView()
                }

                setContentView(noDataView)
            }
            STATUS_ERROR -> {
                if (errorView == null) {
                    errorView = createErrorView()
                }

                setContentView(errorView)
            }
            STATUS_OK -> {
                val r = synchronized(lock) {
                    val value = report
                    if (value == null) {
                        Log.e(TAG, "setStatus(STATUS_OK), but report is null")
                        return
                    }
                    value
                }

                transitionView = null

                noDataView = null
                errorView = null
                loadingView = null
                noNetworkView = null

                setContentView(createChartView(r))
            }
        }
    }

    private fun createNoDataView(): View {
        return FrameLayout(this) {
            TextView {
                frameLayoutParams(WRAP_CONTENT, WRAP_CONTENT) {
                    gravity = Gravity.CENTER_HORIZONTAL or Gravity.TOP
                }

                applyTextAppearance(R.style.TextAppearance_MaterialComponents_Headline4)
                text = resources.getText(R.string.noData)
            }
        }
    }

    private fun createErrorView(): View {
        val res = resources

        val retryButtonSize =
            res.getDimensionPixelSize(R.dimen.reportActivity_retryButtonSize)
        val retryButtonTopMargin =
            res.getDimensionPixelSize(R.dimen.reportActivity_retryButtonTopMargin)

        return LinearLayout(this) {
            orientation = LinearLayout.VERTICAL

            TextView {
                linearLayoutParams(WRAP_CONTENT, WRAP_CONTENT) {
                    gravity = Gravity.CENTER_HORIZONTAL
                }

                applyTextAppearance(R.style.TextAppearance_MaterialComponents_Headline5)
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

                colorTransition = LinearColorTransition.fromArrayResWithDisplayRefreshRate(
                    context,
                    R.array.defaultTransitionColors
                )
                transitionView = this
            }
        }
    }

    private fun createNoNetworkView(): View {
        val res = resources

        return LinearLayout(this) {
            orientation = LinearLayout.VERTICAL

            View {
                val size = res.getDimensionPixelSize(R.dimen.reportActivity_noNetwork_iconSize)
                linearLayoutParams(size, size) {
                    topMargin = res.getDimensionPixelOffset(R.dimen.reportActivity_noNetwork_iconTopMargin)
                    gravity = Gravity.CENTER_HORIZONTAL
                }

                background = ResourcesCompat.getDrawable(res, R.drawable.ic_wifi_off, theme)
            }
            Button {
                val size = res.getDimensionPixelSize(R.dimen.reportActivity_retryButtonSize)
                linearLayoutParams(size, size) {
                    gravity = Gravity.CENTER_HORIZONTAL
                    topMargin = res.getDimensionPixelSize(R.dimen.reportActivity_retryButtonTopMargin)
                }

                background = ResourcesCompat.getDrawable(res, R.drawable.ic_retry, theme)
                setOnClickListener {
                    startLoadingReport()
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putInt(STATE_STATUS, status)

        synchronized(lock) {
            val report = report
            if (report != null) {
                val buffer = Serializable.toByteArray(report, serializer)

                outState.putByteArray(STATE_REPORT, buffer)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        transitionView?.stopTransition()
        loadReportJob?.cancel()

        mainThread.removeCallbacksAndMessages(null)
        mainThread.activity = null
    }

    protected abstract fun createChartView(report: TReport): View
    protected abstract suspend fun loadReport(dataSource: WeatherDataSource): TReport?

    private class MainThreadHandler(@JvmField var activity: ReportActivityBase<*>?) :
        Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            val activity = activity
            if (activity == null) {
                Log.e(TAG, "activity in MainThreadHandler is null")
                return
            }

            when (msg.what) {
                MSG_SET_STATUS -> {
                    activity.setStatus(msg.arg1)
                }
            }
        }
    }

    companion object {
        private val scope = CoroutineScope(Dispatchers.Default + CoroutineName("ReportScope"))
        private const val MSG_SET_STATUS = 0

        private const val STATUS_NO_NETWORK = 4
        private const val STATUS_LOADING = 3
        private const val STATUS_NO_DATA = 0
        private const val STATUS_ERROR = 1
        private const val STATUS_OK = 2

        private const val TAG = "ReportActivityBase"

        private const val STATE_REPORT = "state:ReportActivityBase:report"
        private const val STATE_STATUS = "state:ReportActivityBase:status"
    }
}