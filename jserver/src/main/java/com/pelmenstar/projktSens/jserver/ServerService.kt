package com.pelmenstar.projktSens.jserver

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.pelmenstar.projktSens.jserver.di.AppModule
import com.pelmenstar.projktSens.jserver.di.DaggerAppComponent

class ServerService : Service() {
    private lateinit var server: Server
    private lateinit var weatherMonitor: WeatherMonitor

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        val component = DaggerAppComponent.builder().appModule(AppModule(this)).build()
        val wm = component.weatherMonitor()
        weatherMonitor = wm
        server = component.server().apply {
            weatherMonitor = wm
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())

        weatherMonitor.start()
        server.startOnNewThread()

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        stopForeground(true)

        weatherMonitor.stop()
        server.stop()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val name = getString(R.string.serverNotification_channelName)
            val description = getString(R.string.serverNotification_channelDescription)

            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                name,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                setDescription(description)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        createNotificationChannel()

        val intent = MainActivity.intent(this, serverStarted = true).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        }

        var pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT
        if (Build.VERSION.SDK_INT >= 23) {
            pendingIntentFlags = pendingIntentFlags or PendingIntent.FLAG_IMMUTABLE
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            pendingIntentFlags
        )

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID).run {
            priority = NotificationCompat.PRIORITY_DEFAULT
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setContentIntent(pendingIntent)

            setSmallIcon(R.drawable.ic_launcher_foreground)
            setContentTitle(getText(R.string.serverNotification_title))
            setContentText(getText(R.string.serverNotification_content))

            build()
        }
    }

    companion object {
        private const val NOTIFICATION_ID = 10000
        private const val NOTIFICATION_CHANNEL_ID =
            "com.pelmenstar.projktSens.server.serverNotification"

        fun intent(context: Context): Intent {
            return Intent(context, ServerService::class.java)
        }
    }
}