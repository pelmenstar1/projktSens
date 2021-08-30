package com.pelmenstar.projktSens.weather.app.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import com.pelmenstar.projktSens.shared.android.ext.Message
import com.pelmenstar.projktSens.shared.android.ui.*
import com.pelmenstar.projktSens.shared.geo.GeolocationProvider
import com.pelmenstar.projktSens.weather.app.AppPreferences
import com.pelmenstar.projktSens.weather.app.GeolocationCache
import com.pelmenstar.projktSens.weather.app.PermissionUtils
import com.pelmenstar.projktSens.weather.app.R
import com.pelmenstar.projktSens.weather.app.di.AppModule
import com.pelmenstar.projktSens.weather.app.di.DaggerAppComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class LocationDependentActivity : HomeButtonSupportActivity() {
    private var transitionView: TransitionView? = null
    private var loadingContent: View? = null
    private var failedToGetContent: View? = null
    private var gpsNotGrantedContent: View? = null
    private var gpsNeverShowAgainContent: View? = null

    private val mainThread = MainThreadHandler(this)

    private lateinit var prefs: AppPreferences
    private lateinit var locationProvider: GeolocationProvider

    private var isAppDetailsInfoSettingExists = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // No sense to detect whether app-details activity exists
        // if this information isn't in use.
        if (Build.VERSION.SDK_INT >= 23) {
            isAppDetailsInfoSettingExists = detectAppDetailsSettingsExists()
        }

        val component = DaggerAppComponent.builder().appModule(AppModule(this)).build()
        prefs = component.preferences()
        locationProvider = component.geolocationProvider()

        val location = GeolocationCache.get()
        if (location == null) {
            if (Build.VERSION.SDK_INT < 23 || PermissionUtils.isLocationGranted(this)) {
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

        if (Build.VERSION.SDK_INT >= 23) {
            // In the case of 'Never show again', an user should go to the settings to manually enable location
            // permission.
            // So after the user returns, we need to check whether the user actually changes access to location
            if (PermissionUtils.isLocationGranted(this)) {
                startLoadingLocation()
            }
        }
    }

    private fun createLoadingContent(): View {
        return FrameLayout(this) {
            transitionView = TransitionView {
                val size =
                    resources.getDimensionPixelSize(R.dimen.locationDependentActivity_transitionViewSize)
                frameLayoutParams(size, size) {
                    gravity = Gravity.CENTER
                }

                colorTransition = LinearColorTransition.fromArrayRes(
                    context,
                    R.array.defaultTransitionColors
                )
            }
        }
    }

    private fun createFailedToGetContent(): View {
        val res = resources
        return LinearLayout(this) {
            orientation = LinearLayout.VERTICAL

            TextView {
                linearLayoutParams(WRAP_CONTENT, WRAP_CONTENT) {
                    gravity = Gravity.CENTER_HORIZONTAL
                }

                applyTextAppearance(R.style.TextAppearance_MaterialComponents_Headline5)
                setText(R.string.failedToGetLocation)
            }

            Button {
                val size =
                    res.getDimensionPixelSize(R.dimen.locationDependentActivity_retryButtonSize)
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

    @RequiresApi(23)
    private fun createGpsNotGrantedContent(): View {
        return LinearLayout(this) {
            orientation = LinearLayout.VERTICAL

            val params = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                gravity = Gravity.CENTER_HORIZONTAL
            }

            TextView {
                layoutParams = params

                setText(R.string.gpsIsNotGranted)
            }

            Button(R.attr.materialButtonOutlinedStyle) {
                layoutParams = params
                setText(R.string.allowGps)

                setOnClickListener {
                    requestGps()
                }
            }
        }
    }

    @RequiresApi(23)
    private fun createGpsNeverShowAgainContent(): View {
        return LinearLayout(this) {
            orientation = LinearLayout.VERTICAL

            val params = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                gravity = Gravity.CENTER_HORIZONTAL
            }

            TextView {
                layoutParams = params

                setText(R.string.locDependentActivity_requestLocPerm_gpsNeverShowAgain)
            }

            if (isAppDetailsInfoSettingExists) {
                Button(R.attr.materialButtonOutlinedStyle) {
                    layoutParams = params

                    setText(R.string.goToSettings)

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

    private fun detectAppDetailsSettingsExists(): Boolean {
        val intent = createAppDetailsSettingsIntent()

        val resolveFlags = if (Build.VERSION.SDK_INT >= 24) {
            PackageManager.MATCH_SYSTEM_ONLY
        } else {
            PackageManager.MATCH_DEFAULT_ONLY
        }

        return packageManager.resolveActivity(intent, resolveFlags) != null
    }

    private fun createAppDetailsSettingsIntent(): Intent {
        val appUri = Uri.fromParts("package", packageName, null)
        return Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, appUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    private fun goToSettingPermissions() {
        if (isAppDetailsInfoSettingExists) {
            val intent = createAppDetailsSettingsIntent()
            startActivity(intent)
        }
    }

    private fun startLoadingLocation() {
        setState(STATE_LOADING)

        scope.launch {
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
        if (state != STATE_LOADING) {
            transitionView?.stopTransition()
        }

        when (state) {
            STATE_LOADING -> {
                if (loadingContent == null) {
                    loadingContent = createLoadingContent()
                }

                transitionView?.startTransition()
                setContentView(loadingContent)
            }
            STATE_FAILED_TO_GET -> {
                if (failedToGetContent == null) {
                    failedToGetContent = createFailedToGetContent()
                }

                setContentView(failedToGetContent)
            }
            STATE_GPS_NOT_GRANTED -> {
                if (Build.VERSION.SDK_INT < 23) {
                    Log.e(TAG, "setState(STATE_GPS_NOT_GRANTED) was called, but SDK < 23")
                    return
                }

                if (gpsNotGrantedContent == null) {
                    gpsNotGrantedContent = createGpsNotGrantedContent()
                }

                setContentView(gpsNotGrantedContent)
            }
            STATE_GPS_NEVER_SHOW_AGAIN -> {
                if (Build.VERSION.SDK_INT < 23) {
                    Log.e(TAG, "setState(STATE_GPS_NEVER_SHOW_AGAIN) was called, but SDK < 23")
                    return
                }

                if (gpsNeverShowAgainContent == null) {
                    gpsNeverShowAgainContent = createGpsNeverShowAgainContent()
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

        if (Build.VERSION.SDK_INT < 23) {
            Log.e(TAG, "Sdk < 23")
            return
        }

        if (grantResults.any { it == PackageManager.PERMISSION_GRANTED }) {
            // now gps isn't denied
            prefs.isGpsPermissionDenied = false

            startLoadingLocation()
        } else if (PermissionUtils.isNeverShowAgainOnLocation(this)) {
            setState(STATE_GPS_NEVER_SHOW_AGAIN)
        }
    }

    protected abstract fun createMainContent(): View
    protected abstract fun onLocationPresent()

    private class MainThreadHandler(
        @JvmField var activity: LocationDependentActivity?
    ) : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            val a = activity ?: return

            when (msg.what) {
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