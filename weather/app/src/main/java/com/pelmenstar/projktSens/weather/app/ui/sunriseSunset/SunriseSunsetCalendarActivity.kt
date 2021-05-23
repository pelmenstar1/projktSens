package com.pelmenstar.projktSens.weather.app.ui.sunriseSunset

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import com.pelmenstar.projktSens.shared.android.ui.*
import com.pelmenstar.projktSens.shared.time.ShortDate
import com.pelmenstar.projktSens.weather.app.R
import com.pelmenstar.projktSens.weather.app.di.AppModule
import com.pelmenstar.projktSens.weather.app.di.DaggerAppComponent

class SunriseSunsetCalendarActivity: HomeButtonSupportActivity(), SunriseSunsetCalendarContract.View {
    override val context: Context
        get() = this

    private var presenter: SunriseSunsetCalendarContract.Presenter? = null

    private lateinit var sunriseTimeView: TimePrefixTextView
    private lateinit var sunsetTimeView: TimePrefixTextView
    private lateinit var dayLengthView: TimePrefixTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)

        super.onCreate(savedInstanceState)

        val resources = resources

        actionBar {
            title = resources.getText(R.string.sunsetSunriseActionBarTitle)

            setDisplayHomeAsUpEnabled(true)
        }

        val component = DaggerAppComponent
            .builder()
            .appModule(AppModule(this))
            .build()

        content {
            val body1 = TextAppearance(this, R.style.TextAppearance_MaterialComponents_Body1)
            val dp5 = (5 * resources.displayMetrics.density).toInt()

            LinearLayout(this) {
                orientation = LinearLayout.VERTICAL

                TimePrefixTextView {
                    linearLayoutParams(WRAP_CONTENT, WRAP_CONTENT) {
                        leftMargin = dp5
                    }

                    sunriseTimeView = this
                    prefix = resources.getString(R.string.sunrise)

                    applyTextAppearance(body1)
                }

                TimePrefixTextView {
                    linearLayoutParams(WRAP_CONTENT, WRAP_CONTENT) {
                        leftMargin = dp5
                    }

                    sunsetTimeView = this
                    prefix = resources.getString(R.string.sunset)

                    applyTextAppearance(body1)
                }

                TimePrefixTextView {
                    linearLayoutParams(WRAP_CONTENT, WRAP_CONTENT) {
                        leftMargin = dp5
                        bottomMargin = dp5
                    }

                    dayLengthView = this
                    prefix = resources.getString(R.string.dayLength)

                    applyTextAppearance(body1)
                }

                ScrollableCalendarView {
                    linearLayoutParams(MATCH_PARENT, WRAP_CONTENT)

                    setOnDateChangeListener { _, year, month, dayOfMonth ->
                        val date = ShortDate.of(year, month + 1, dayOfMonth)

                        presenter?.onDaySelected(ShortDate.getDayOfYear(date))
                    }
                }
            }
        }

        presenter = component.sunriseSunsetCalendarPresenter().also {
            it.attach(this)
            if(savedInstanceState != null) {
                it.restoreState(savedInstanceState)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        presenter?.detach()
        presenter = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        presenter?.saveState(outState)
    }

    override fun setSunriseTime(sunrise: Int) {
       sunriseTimeView.time = sunrise
    }

    override fun setSunsetTime(sunset: Int) {
        sunsetTimeView.time = sunset
    }

    override fun setDayLength(time: Int) {
        dayLengthView.time = time
    }

    companion object {
        @JvmStatic
        fun intent(context: Context): Intent {
            return Intent(context, SunriseSunsetCalendarActivity::class.java)
        }
    }
}