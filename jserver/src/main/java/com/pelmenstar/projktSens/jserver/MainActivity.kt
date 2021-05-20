package com.pelmenstar.projktSens.jserver

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.pelmenstar.projktSens.shared.android.ui.Button
import com.pelmenstar.projktSens.shared.android.ui.LinearLayout
import com.pelmenstar.projktSens.shared.android.ui.content
import com.pelmenstar.projktSens.shared.time.ShortDate
import com.pelmenstar.projktSens.weather.models.debugGenDb
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {
    private lateinit var startButton: Button
    private lateinit var stopButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val cfg = MainConfig(this)
        serverConfig = cfg
        val shared = cfg.sharedRepo

        val repoServer = RepoServer()
        val checkAvailabilityServer = StatusServer()
        val weatherChannelInfoServer = WeatherChannelInfoServer()

        content {
            LinearLayout(this) {
                orientation = LinearLayout.VERTICAL

                Button {
                    startButton = this
                    text = "Start"

                    setOnClickListener {
                        isEnabled = false
                        stopButton.isEnabled = true

                        WeatherMonitor.start()
                        repoServer.start()
                        checkAvailabilityServer.start()
                        weatherChannelInfoServer.start()
                    }
                }

                Button {
                    stopButton = this
                    text = "Stop"
                    isEnabled = false

                    setOnClickListener {
                        isEnabled = false
                        startButton.isEnabled = true

                        WeatherMonitor.stop()
                        repoServer.stop()
                        checkAvailabilityServer.stop()
                        weatherChannelInfoServer.stop()
                    }
                }

                if(BuildConfig.DEBUG) {
                    Button {
                        text = "Clear db"

                        setOnClickListener {
                            GlobalScope.launch { shared.clear() }
                        }
                    }

                    Button {
                        text = "Gen db"

                        setOnClickListener {
                            GlobalScope.launch {
                                val startDate = ShortDate.minusDays(ShortDate.now(), 31)

                                shared.debugGenDb(
                                    startDate,
                                    89280
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}