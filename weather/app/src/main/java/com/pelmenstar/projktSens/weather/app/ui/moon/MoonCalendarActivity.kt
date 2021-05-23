package com.pelmenstar.projktSens.weather.app.ui.moon

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import com.pelmenstar.projktSens.shared.android.ui.*
import com.pelmenstar.projktSens.shared.time.ShortDate
import com.pelmenstar.projktSens.weather.app.R
import com.pelmenstar.projktSens.weather.app.di.AppModule
import com.pelmenstar.projktSens.weather.app.di.DaggerAppComponent
import com.pelmenstar.projktSens.weather.app.formatters.MoonPhaseFormatter
import com.pelmenstar.projktSens.weather.app.ui.MoonView

class MoonCalendarActivity: HomeButtonSupportActivity(), MoonCalendarContract.View {
    override val context: Context
        get() = this

    private var presenter: MoonCalendarContract.Presenter? = null

    private lateinit var moonView: MoonView
    private lateinit var moonPhaseView: PrefixTextView
    private lateinit var moonPhaseFormatter: MoonPhaseFormatter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val resources = resources
        actionBar {
            title = resources.getText(R.string.moonCalendar)

            setDisplayHomeAsUpEnabled(true)
        }

        content {
            val density = resources.displayMetrics.density
            val dp5 = (5f * density).toInt()
            val dp10 = (10f * density).toInt()
            val dp200 = (200f * density).toInt()
            val body1 = TextAppearance(this, R.style.TextAppearance_MaterialComponents_Body1)

            LinearLayout(this) {
                orientation = LinearLayout.VERTICAL


                PrefixTextView {
                    linearLayoutParams(MATCH_PARENT, WRAP_CONTENT) {
                        leftMargin = dp5
                    }

                    moonPhaseView = this
                    prefix = resources.getString(R.string.moonPhase)
                    applyTextAppearance(body1)
                }

                MoonView {
                    linearLayoutParams(dp200, dp200) {
                        gravity = Gravity.CENTER_HORIZONTAL

                        topMargin = dp10
                        bottomMargin = dp10
                    }

                    moonView = this
                }

                ScrollableCalendarView {
                    linearLayoutParams(MATCH_PARENT, MATCH_PARENT)

                    setOnDateChangeListener { _, year, month, dayOfMonth ->
                        presenter?.onDateSelected(ShortDate.of(year, month + 1, dayOfMonth))
                    }
                }
            }
        }

        val component = DaggerAppComponent
            .builder()
            .appModule(AppModule(this))
            .build()

        moonPhaseFormatter = component.moonPhaseFormatter()
        presenter = component.moonCalendarPresenter().also {
            it.attach(this)

            if(savedInstanceState != null) {
                it.restoreState(savedInstanceState)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        presenter?.saveState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()

        presenter?.detach()
        presenter = null
    }

    override fun setMoonPhase(phase: Float) {
        moonView.moonPhase = phase
        moonPhaseView.value = moonPhaseFormatter.format(phase)
    }

    companion object {
        @JvmStatic
        fun intent(context: Context): Intent {
            return Intent(context, MoonCalendarActivity::class.java)
        }

    }
}