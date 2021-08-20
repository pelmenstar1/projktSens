package com.pelmenstar.projktSens.jserver

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.*
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.pelmenstar.projktSens.jserver.di.AppModule
import com.pelmenstar.projktSens.jserver.di.DaggerAppComponent
import com.pelmenstar.projktSens.serverProtocol.ServerAvailabilityProvider
import com.pelmenstar.projktSens.shared.android.ui.*
import com.pelmenstar.projktSens.shared.android.ui.settings.SettingsActivity
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {
    private lateinit var startButton: Button
    private lateinit var stopButton: Button

    private val mainThread = MainThreadHandler(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        content {
            LinearLayout(this) {
                orientation = LinearLayout.VERTICAL

                startButton = Button {
                    text = "Start"

                    setOnClickListener {
                        isEnabled = false
                        stopButton.isEnabled = true

                        startServerService()
                    }
                }

                stopButton = Button {
                    text = "Stop"
                    isEnabled = false

                    setOnClickListener {
                        isEnabled = false
                        startButton.isEnabled = true

                        stopServerService()
                    }
                }
            }
        }

        val intent = requireIntent()
        if (intent.hasExtra(EXTRA_SERVER_STARTED)) {
            val isStarted = intent.getBooleanExtra(EXTRA_SERVER_STARTED, false)
            setServerStarted(isStarted)
        } else {
            scope.launch {
                postSetServerStarted(isServerActuallyStarted())
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        mainThread.removeCallbacksAndMessages(null)
        mainThread.activity = null
    }

    private fun setServerStarted(state: Boolean) {
        if (state) {
            startButton.isEnabled = false
            stopButton.isEnabled = true
        } else {
            startButton.isEnabled = true
            stopButton.isEnabled = false
        }
    }

    private fun postSetServerStarted(state: Boolean) {
        mainThread.sendMessage(Message.obtain().apply {
            what = MSG_SET_SERVER_STARTED
            arg1 = if (state) 1 else 0
        })
    }

    private suspend fun isServerActuallyStarted(): Boolean {
        val component = DaggerAppComponent.builder().appModule(AppModule(this)).build()
        val provider = ServerAvailabilityProvider(component.protoConfig())

        return provider.isAvailable()
    }

    private fun startServerService() {
        val intent = ServerService.intent(this)

        if (Build.VERSION.SDK_INT >= 26) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun stopServerService() {
        stopService(ServerService.intent(this))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val context = this

        menu.add {
            item(
                titleRes = R.string.main_menu_settings,
                showsAsAction = MenuItem.SHOW_AS_ACTION_IF_ROOM,
                iconRes = R.drawable.ic_settings
            ) {
                val intent = SettingsActivity.intent(
                    context,
                    APP_SETTING_CLASS_NAMES,
                    AppPreferences::class.java
                )
                context.startActivity(intent)
                true
            }
        }
        return true
    }

    private class MainThreadHandler(@JvmField var activity: MainActivity?) :
        Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            val a = activity ?: return

            when (msg.what) {
                MSG_SET_SERVER_STARTED -> {
                    a.setServerStarted(msg.arg1 == 1)
                }
            }
        }
    }

    companion object {
        private val scope = CoroutineScope(Dispatchers.Default + CoroutineName("MainActivity"))

        private const val EXTRA_SERVER_STARTED =
            "com.pelmenstar.projktSens.server.MainActivity.serverStarted"

        private const val MSG_SET_SERVER_STARTED = 0

        fun intent(context: Context, serverStarted: Boolean = false): Intent {
            return Intent(context, MainActivity::class.java).apply {
                putExtra(EXTRA_SERVER_STARTED, serverStarted)
            }
        }
    }
}