package com.pelmenstar.projktSens.weather.app.ui.sunriseSunset

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.style.StyleSpan
import android.view.View
import android.widget.LinearLayout
import com.pelmenstar.projktSens.shared.android.ui.*
import com.pelmenstar.projktSens.shared.time.ShortDate
import com.pelmenstar.projktSens.weather.app.R
import com.pelmenstar.projktSens.weather.app.di.AppModule
import com.pelmenstar.projktSens.weather.app.di.DaggerAppComponent
import com.pelmenstar.projktSens.weather.app.ui.LocationDependentActivity

class SunriseSunsetCalendarActivity : LocationDependentActivity(),
    SunriseSunsetCalendarContract.View {
    override val context: Context
        get() = this

    private var presenter: SunriseSunsetCalendarContract.Presenter? = null

    private lateinit var sunriseTimeView: TimePrefixTextView
    private lateinit var sunsetTimeView: TimePrefixTextView
    private lateinit var dayLengthView: TimePrefixTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)

        super.onCreate(savedInstanceState)

        actionBar {
            title = resources.getText(R.string.sunsetSunriseActionBarTitle)

            setDisplayHomeAsUpEnabled(true)
        }

        val component = DaggerAppComponent
            .builder()
            .appModule(AppModule(this))
            .build()

        presenter = component.sunriseSunsetCalendarPresenter().also {
            it.attach(this)
            if (savedInstanceState != null) {
                it.restoreState(savedInstanceState)
            }
        }
    }

    override fun createMainContent(): View {
        val res = resources
        val body1 = TextAppearance(this, R.style.TextAppearance_MaterialComponents_Body1)
        val boundsMargin = res.getDimensionPixelOffset(R.dimen.sunriseSunsetCalendar_boundsMargin)

        return LinearLayout(this) {
            orientation = LinearLayout.VERTICAL

            val textViewLayoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                leftMargin = boundsMargin
            }

            val boldStyle = StyleSpan(Typeface.BOLD)
            sunriseTimeView = TimePrefixTextView {
                layoutParams = textViewLayoutParams

                timeStyle = boldStyle
                prefix = res.getString(R.string.sunrise)

                applyTextAppearance(body1)
            }

            sunsetTimeView = TimePrefixTextView {
                layoutParams = textViewLayoutParams

                timeStyle = boldStyle
                prefix = res.getString(R.string.sunset)

                applyTextAppearance(body1)
            }

            dayLengthView = TimePrefixTextView {
                layoutParams = textViewLayoutParams

                timeStyle = boldStyle
                prefix = res.getString(R.string.dayLength)

                applyTextAppearance(body1)
            }

            ScrollableCalendarView {
                linearLayoutParams(MATCH_PARENT, WRAP_CONTENT) {
                    topMargin =
                        res.getDimensionPixelOffset(R.dimen.sunriseSunsetCalendar_calendarTopMargin)
                }

                setOnDateChangeListener { _, year, month, dayOfMonth ->
                    val date = ShortDate.of(year, month + 1, dayOfMonth)

                    presenter?.onDaySelected(ShortDate.getDayOfYear(date))
                }
            }
        }
    }

    override fun onLocationPresent() {
        presenter?.onLocationPresent()
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