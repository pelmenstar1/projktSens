package com.pelmenstar.projktSens.weather.app.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import android.util.Log
import android.view.Gravity
import android.view.View
import androidx.annotation.RequiresApi
import com.pelmenstar.projktSens.shared.android.Message
import com.pelmenstar.projktSens.shared.android.ui.*
import com.pelmenstar.projktSens.weather.app.GeolocationCache
import com.pelmenstar.projktSens.weather.app.PermissionUtils
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
    private var gpsNotGrantedContent: View? = null
    private var gpsNeverShowAgainContent: View? = null

    private val mainThread = MainThreadHandler(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createStateViews()

        val location = GeolocationCache.get()
        if(location == null) {
            if(Build.VERSION.SDK_INT < 23 || PermissionUtils.isLocationGranted(this)) {
                startLoadingLocation()
            } else {
                setState(STATE_GPS_NOT_GRANTED)
            }
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

    override fun onResume() {
        super.onResume()
        
        if(Build.VERSION.SDK_INT >= 23) {
            // In the case of 'Never show again', an user should go to the settings to manually enable location
            // permission.
            // So after the user returns, we need to check whether the user actually changes access to location
            if(PermissionUtils.isLocationGranted(this)) {
                startLoadingLocation()
            }
        }
    }

    private fun createStateViews() {
        val res = resources
        val context = this

        loadingContent = FrameLayout(context) {
            TransitionView {
                val size = res.getDimensionPixelSize(R.dimen.locationDependentActivity_transitionViewSize)
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

                val textSizePx = res.getDimensionPixelSize(R.dimen.locationDependentActivity_errorTextSize)

                textSize = textSizePx.toFloat()
                setText(R.string.failedToGetLocation)
            }

            Button {
                val size = res.getDimensionPixelSize(R.dimen.locationDependentActivity_retryButtonSize)
                linearLayoutParams(size, size) {
                    gravity = Gravity.CENTER_HORIZONTAL
                }

                setBackgroundResource(R.drawable.ic_retry)
                setOnClickListener {
                    startLoadingLocation()
                }
            }
        }

        if(Build.VERSION.SDK_INT >= 23) {
            gpsNotGrantedContent = LinearLayout(context) {
                orientation = android.widget.LinearLayout.VERTICAL

                TextView {
                    linearLayoutParams(WRAP_CONTENT, WRAP_CONTENT) {
                        gravity = Gravity.CENTER_HORIZONTAL
                    }

                    setText(R.string.gpsIsNotGranted)
                }

                Button(R.attr.materialButtonOutlinedStyle) {
                    setText(R.string.allowGps)

                    linearLayoutParams(WRAP_CONTENT, WRAP_CONTENT) {
                        gravity = Gravity.CENTER_HORIZONTAL
                    }

                    setOnClickListener {
                        requestGps()
                    }
                }
            }

            gpsNeverShowAgainContent = LinearLayout(context) {
                orientation = android.widget.LinearLayout.VERTICAL

                TextView {
                    linearLayoutParams(WRAP_CONTENT, WRAP_CONTENT) {
                        gravity = Gravity.CENTER_HORIZONTAL
                    }

                    setText(R.string.locDependentActivity_requestLocPerm_gpsNeverShowAgain)
                }

                Button(R.attr.materialButtonOutlinedStyle) {
                    setText(R.string.goToSettings)

                    linearLayoutParams(WRAP_CONTENT, WRAP_CONTENT) {
                        gravity = Gravity.CENTER_HORIZONTAL
                    }

                    setOnClickListener {
                        goToSettingPermissions()
                    }
                }
            }
        }
    }

    @RequiresApi(23)
    private fun requestGps() {
        requestPermissions(PermissionUtils.LOCATION_PERMISSIONS, GPS_PERMISSION_REQUEST_CODE)
    }

    private fun goToSettingPermissions() {
        val appUri = Uri.fromParts("package", packageName, null)
        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, appUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)
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
        val gpsNotGrantedContent = gpsNotGrantedContent
        val gpsNeverShowAgainContent = gpsNeverShowAgainContent

        if(transitionView == null ||
            loadingContent == null ||
            failedToGetContent == null) {
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
            STATE_GPS_NOT_GRANTED -> {
                if(Build.VERSION.SDK_INT < 23) {
                    Log.e(TAG, "setState(STATE_GPS_NOT_GRANTED) was called, but SDK < 23")
                    return
                }

                if(gpsNotGrantedContent == null) {
                    Log.e(TAG, "gpsNotGrantedContent was recycled")
                    return
                }

                setContentView(gpsNotGrantedContent)
            }
            STATE_GPS_NEVER_SHOW_AGAIN -> {
                if(Build.VERSION.SDK_INT < 23) {
                    Log.e(TAG, "setState(STATE_GPS_NEVER_SHOW_AGAIN) was called, but SDK < 23")
                    return
                }

                if(gpsNeverShowAgainContent == null) {
                    Log.e(TAG, "gpsNeverShowAgainContent was recycled")
                    return
                }

                setContentView(gpsNeverShowAgainContent)
            }

            STATE_MAIN_CONTENT -> {
                this.transitionView = null
                this.failedToGetContent = null
                this.transitionView = null
                this.gpsNotGrantedContent = null
                this.gpsNeverShowAgainContent = null

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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(Build.VERSION.SDK_INT < 23) {
            Log.e(TAG, "Sdk < 23")
            return
        }

        if(grantResults.any { it == PackageManager.PERMISSION_GRANTED }) {
            // now gps isn't denied
            val component = DaggerAppComponent.builder().appModule(AppModule(this)).build()
            val prefs = component.preferences()
            prefs.isGpsPermissionDenied = false

            startLoadingLocation()
        } else if(PermissionUtils.isNeverShowAgainOnLocation(this)) {
            setState(STATE_GPS_NEVER_SHOW_AGAIN)
        }
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

        private const val GPS_PERMISSION_REQUEST_CODE = 1

        private const val STATE_LOADING = 0
        private const val STATE_FAILED_TO_GET = 1
        private const val STATE_MAIN_CONTENT = 2
        private const val STATE_GPS_NOT_GRANTED = 3
        private const val STATE_GPS_NEVER_SHOW_AGAIN = 4

        private const val MSG_SET_STATE = 0
        private const val MSG_CALL_ON_LOCATION_PRESENT = 1

        private val scope = CoroutineScope(Dispatchers.Default)
    }
}