package com.pelmenstar.projktSens.jserver

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.pelmenstar.projktSens.shared.android.ui.Button
import com.pelmenstar.projktSens.shared.android.ui.LinearLayout
import com.pelmenstar.projktSens.shared.android.ui.content

@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {
    private lateinit var startButton: Button
    private lateinit var stopButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val controller = Controller(MainConfig(this))

        content {
            LinearLayout(this) {
                orientation = LinearLayout.VERTICAL

                Button {
                    startButton = this
                    text = "Start"

                    setOnClickListener {
                        isEnabled = false
                        stopButton.isEnabled = true

                        controller.startAll()
                    }
                }

                Button {
                    stopButton = this
                    text = "Stop"
                    isEnabled = false

                    setOnClickListener {
                        isEnabled = false
                        startButton.isEnabled = true

                        controller.stopAll()
                    }
                }

                if(BuildConfig.DEBUG) {
                    Button {
                        text = "Clear db"

                        setOnClickListener { controller.clearRepository() }
                    }

                    Button {
                        text = "Gen db"

                        setOnClickListener { controller.debugGenDb() }
                    }
                }
            }
        }
    }
}