package com.pelmenstar.projktSens.weather.app.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.Gravity
import android.view.View
import com.pelmenstar.projktSens.shared.android.Message
import com.pelmenstar.projktSens.shared.android.ui.*
import com.pelmenstar.projktSens.weather.app.GeolocationCache
import com.pelmenstar.projktSens.weather.app.R
import com.pelmenstar.projktSens.weather.app.di.AppModule
import com.pelmenstar.projktSens.weather.app.di.DaggerAppComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class LocationDependentActivity: HomeButtonSupportActivity() {
    private var transitionView: TransitionView? = null
    private var loadingContent: View? = null
    private var failedToGetContent: View? = null

    private val mainThread = MainThreadHandler(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createStateViews()

        val location = GeolocationCache.get()
        if(location == null) {
            startLoadingLocation()
        } else {
            setContentView(createMainContent())
            onLocationPresent()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        mainThread.removeCallbacksAndMessages(null)
        mainThread.activity = null
    }

    private fun createStateViews() {
        val density = resources.displayMetrics.density
        val context = this

        loadingContent = FrameLayout(context) {
            TransitionView {
                val size = (200 * density).toInt()
                frameLayoutParams(size, size) {
                    gravity = Gravity.CENTER
                }

                transition = LinearColorTransition.fromArrayRes(context, R.array.defaultTransitionColors)
                transitionView = this
            }
        }

        failedToGetContent = LinearLayout(context) {
            orientation = android.widget.LinearLayout.VERTICAL

            TextView {
                linearLayoutParams(WRAP_CONTENT, WRAP_CONTENT) {
                    gravity = Gravity.CENTER_HORIZONTAL
                }

                val textSizePx = resources.getDimensionPixelSize(R.dimen.locationDependentActivity_errorTextSize)

                textSize = textSizePx.toFloat()
                setText(R.string.failedToGetLocation)
            }

            Button {
                val size = (40 * density).toInt()
                linearLayoutParams(size, size) {
                    gravity = Gravity.CENTER_HORIZONTAL
                }

                setBackgroundResource(R.drawable.ic_retry)
                setOnClickListener {
                    startLoadingLocation()
                }
            }
        }
    }

    private fun startLoadingLocation() {
        setState(STATE_LOADING)

        val context = this
        scope.launch {
            val component = DaggerAppComponent.builder().appModule(AppModule(context)).build()
            val locationProvider = component.geolocationProvider()

            try {
                val location = locationProvider.getLastLocation()
                GeolocationCache.set(location)

                postSetState(STATE_MAIN_CONTENT)
                postOnLocationPresent()
            } catch (e: Exception) {
                postSetState(STATE_FAILED_TO_GET)
            }
        }
    }

    private fun setState(state: Int) {
        val transitionView = transitionView
        val loadingContent = loadingContent
        val failedToGetContent = failedToGetContent

        if(transitionView == null || loadingContent == null || failedToGetContent == null) {
            Log.w(TAG, "Views except main content are already recycled")
            return
        }

        if(state != STATE_LOADING) {
            transitionView.stopAnimation()
        }

        when(state) {
            STATE_LOADING -> {
                transitionView.startAnimation()
                setContentView(loadingContent)
            }
            STATE_FAILED_TO_GET -> {
                setContentView(failedToGetContent)
            }
            STATE_MAIN_CONTENT -> {
                this.transitionView = null
                this.failedToGetContent = null
                this.transitionView = null

                setContentView(createMainContent())
            }
        }
    }

    private fun postSetState(state: Int) {
        mainThread.sendMessage(Message {
            what = MSG_SET_STATE
            arg1 = state
        })
    }

    private fun postOnLocationPresent() {
        mainThread.sendMessage(Message {
            what = MSG_CALL_ON_LOCATION_PRESENT
        })
    }

    protected abstract fun createMainContent(): View
    protected abstract fun onLocationPresent()

    private class MainThreadHandler(
        @JvmField var activity: LocationDependentActivity?
        ): Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            val a = activity ?: return

            when(msg.what) {
                MSG_SET_STATE -> {
                    a.setState(msg.arg1)
                }
                MSG_CALL_ON_LOCATION_PRESENT -> {
                    a.onLocationPresent()
                }
            }
        }
    }
    companion object {
        private const val TAG = "LocationDepActivity"

        private const val STATE_LOADING = 0
        private const val STATE_FAILED_TO_GET = 1
        private const val STATE_MAIN_CONTENT = 2

        private const val MSG_SET_STATE = 0
        private const val MSG_CALL_ON_LOCATION_PRESENT = 1

        private val scope = CoroutineScope(Dispatchers.Default)
    }
}