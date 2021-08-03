package com.pelmenstar.projktSens.weather.app.ui.home

import android.animation.LayoutTransition
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.button.MaterialButton
import com.pelmenstar.projktSens.shared.android.obtainStyledAttributes
import com.pelmenstar.projktSens.shared.android.ui.*
import com.pelmenstar.projktSens.shared.android.ui.settings.SettingsActivity
import com.pelmenstar.projktSens.shared.android.ui.settings.SettingsContext
import com.pelmenstar.projktSens.shared.time.PrettyDateFormatter
import com.pelmenstar.projktSens.shared.time.ShortDate
import com.pelmenstar.projktSens.weather.app.R
import com.pelmenstar.projktSens.weather.app.di.AppComponent
import com.pelmenstar.projktSens.weather.app.di.AppModule
import com.pelmenstar.projktSens.weather.app.di.DaggerAppComponent
import com.pelmenstar.projktSens.weather.app.formatters.UnitFormatter
import com.pelmenstar.projktSens.weather.app.ui.ComplexWeatherView
import com.pelmenstar.projktSens.weather.app.ui.LazyLoadingCalendarView
import com.pelmenstar.projktSens.weather.app.ui.home.weatherView.ComplexWeatherView
import com.pelmenstar.projktSens.weather.app.ui.moon.MoonCalendarActivity
import com.pelmenstar.projktSens.weather.app.ui.settings.SETTINGS
import com.pelmenstar.projktSens.weather.app.ui.sunriseSunset.SunriseSunsetCalendarActivity
import com.pelmenstar.projktSens.weather.models.WeatherInfo

class HomeActivity : HomeButtonSupportActivity(), HomeContract.View {
    override val context: Context
        get() = this

    private lateinit var serverUnavailableView: View

    private lateinit var weatherView: ComplexWeatherView

    private lateinit var gotoTodayReportButton: Button
    private lateinit var gotoYesterdayReportButton: Button
    private lateinit var gotoThisWeekReportButton: Button
    private lateinit var gotoPrevWeekReportButton: Button
    private lateinit var gotoThisMonthReportButton: Button
    private lateinit var gotoPrevMonthReportButton: Button

    private lateinit var calendarView: LazyLoadingCalendarView

    private lateinit var prettyDateFormatter: PrettyDateFormatter
    private lateinit var unitFormatter: UnitFormatter

    private var greaterThanZeroColor: Int = 0
    private var lessThanZeroColor: Int = 0
    private var zeroColor: Int = 0

    private var presenter: HomeContract.Presenter? = null

    private val startSettingActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        val data = it.data
        if(data == null || data.getBooleanExtra(SettingsActivity.RETURN_DATA_STATE_CHANGED, true)) {
            val intent = intent
            finish()
            overridePendingTransition(0, 0)
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)

        super.onCreate(savedInstanceState)

        actionBar {
            title = getText(R.string.weather)
        }

        setContentView(createContent())

        initRes()

        val component: AppComponent = DaggerAppComponent
            .builder()
            .appModule(AppModule(this))
            .build()

        prettyDateFormatter = component.prettyDateFormatter()
        unitFormatter = component.unitFormatter()

        presenter = component.homePresenter().also {
            it.attach(this)

            if (savedInstanceState != null) {
                it.restoreState(savedInstanceState)
            }

            calendarView.loadMinMaxHandler = it.getLoadMinMaxCalendarHandler()
            weatherView.run {
                onRetryGetLocationListener = it.getOnRetryGetLocationListener()
                requestLocationPermissionHandler = it.getRequestLocationPermissionHandler()
            }
        }
    }

    private fun initRes() {
        val res = resources
        val theme = theme

        greaterThanZeroColor = ResourcesCompat.getColor(res, R.color.tempDeltaGreaterThanZero, theme)
        lessThanZeroColor = ResourcesCompat.getColor(res, R.color.tempDeltaLessThanZero, theme)

        theme.obtainStyledAttributes(android.R.style.Theme, android.R.attr.textColorPrimary) { a ->
            zeroColor = a.getColor(0, 0)
        }
    }

    private class GoToButtonContext(context: Context) {
        private val dp3 = (3 * context.resources.displayMetrics.density).toInt()

        @JvmField val layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
            leftMargin = dp3
            rightMargin = dp3
        }
    }

    private fun createContent(): View {
        val res = resources

        val body1 = TextAppearance(this, R.style.TextAppearance_MaterialComponents_Body1)
        val density = res.displayMetrics.density

        val dp1 = density.toInt()
        val dp10 = (10f * density).toInt()
        val dp3 = (3f * density).toInt()
        val dp20 = (20f * density).toInt()
        val dp200 = (200f * density).toInt()

        return LinearLayout(this) {
            orientation = LinearLayout.VERTICAL
            layoutTransition = LayoutTransition()

            serverUnavailableView = TextView {
                linearLayoutParams(MATCH_PARENT, WRAP_CONTENT) {
                    gravity = Gravity.CENTER_HORIZONTAL

                    leftMargin = dp3
                    rightMargin = dp3

                    topMargin = dp1
                    bottomMargin = dp10
                }

                applyTextAppearance(body1)
                visibility = View.GONE
                text = res.getText(R.string.serverUnavailable)
            }

            ScrollView {
                linearLayoutParams(MATCH_PARENT, MATCH_PARENT)

                LinearLayout {
                    orientation = LinearLayout.VERTICAL

                    weatherView = ComplexWeatherView {
                        linearLayoutParams(MATCH_PARENT, dp200)
                    }

                    val goToContext = GoToButtonContext(context)

                    // day
                    gotoTodayReportButton = GoToButton(goToContext, R.string.today) {
                        presenter?.startTodayReportView()
                    }

                    gotoYesterdayReportButton = GoToButton(goToContext, R.string.yesterday) {
                        presenter?.startYesterdayReportView()
                    }

                    // week
                    gotoThisWeekReportButton = GoToButton(goToContext, R.string.thisWeek) {
                        presenter?.startThisWeekReportView()
                    }

                    gotoPrevWeekReportButton = GoToButton(goToContext, R.string.prevWeek) {
                        presenter?.startYesterdayReportView()
                    }

                    // month
                    gotoThisMonthReportButton = GoToButton(goToContext, R.string.thisMonth) {
                        presenter?.startThisMonthReportView()
                    }

                    gotoPrevMonthReportButton = GoToButton(goToContext, R.string.prevMonth) {
                        presenter?.startPreviousMonthReportView()
                    }

                    LazyLoadingCalendarView {
                        linearLayoutParams(MATCH_PARENT, WRAP_CONTENT) {
                            topMargin = dp20
                        }

                        setOnDateChangeListener { _, year, month, dayOfMonth ->
                            presenter?.startDayReportView(
                                ShortDate.of(
                                    year,
                                    month + 1,
                                    dayOfMonth
                                )
                            )
                        }

                        calendarView = this
                    }
                }
            }
        }
    }

    private inline fun ViewGroup.GoToButton(
        goToButtonContext: GoToButtonContext,
        @StringRes textRes: Int,
        crossinline onClick: () -> Unit
    ): MaterialButton {
        return Button(R.attr.materialButtonOutlinedStyle) {
            layoutParams = goToButtonContext.layoutParams

            gravity = Gravity.START or Gravity.CENTER_VERTICAL

            text = resources.getText(textRes)

            setOnClickListener { onClick() }
        }
    }

    override fun onStart() {
        super.onStart()

        presenter?.connectToWeatherChannel()
    }

    override fun onStop() {
        super.onStop()

        presenter?.disconnectFromWeatherChannel()
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val context = this
        val component = DaggerAppComponent.builder().appModule(AppModule(context)).build()
        val prefsClass = component.preferences().javaClass
        val settingsContext = SettingsContext(*SETTINGS)

        menu.add {
            item(
                titleRes = (R.string.settings),
                showsAsAction = MenuItem.SHOW_AS_ACTION_NEVER,
                iconRes = R.drawable.ic_settings
            ) {
                val intent = SettingsActivity.intent(
                    context,
                    settingsContext,
                    prefsClass
                )
                startSettingActivity.launch(intent)
                true
            }
            item(
                titleRes = R.string.sunCalendar,
                showsAsAction = MenuItem.SHOW_AS_ACTION_NEVER,
                iconRes = R.drawable.ic_calendar
            ) {
                startActivity(SunriseSunsetCalendarActivity.intent(context))
                true
            }
            item(
                titleRes = R.string.moonCalendar,
                showsAsAction = MenuItem.SHOW_AS_ACTION_NEVER,
                iconRes = R.drawable.ic_moon
            ) {
                startActivity(MoonCalendarActivity.intent(context))
                true
            }
        }

        return true
    }

    override fun setLocationLoaded(value: Boolean) {
        weatherView.isLocationLoaded = value
    }

    override fun setCanLoadLocation(value: Boolean) {
        weatherView.setCanLoadLocation(value)
    }

    override fun setCurrentTime(time: Int) {
        weatherView.time = time
    }

    override fun setSunriseSunset(sunrise: Int, sunset: Int) {
        weatherView.setSunriseSunset(sunrise, sunset)
    }

    override fun setMoonPhase(phase: Float) {
        weatherView.setMoonPhase(phase)
    }

    override fun setWeather(value: WeatherInfo) {
        weatherView.setWeather(value)
    }

    override fun onServerUnavailable() {
        gotoTodayReportButton.isEnabled = false
        gotoYesterdayReportButton.isEnabled = false
        gotoThisWeekReportButton.isEnabled = false
        gotoPrevWeekReportButton.isEnabled = false
        gotoThisMonthReportButton.isEnabled = false
        gotoPrevMonthReportButton.isEnabled = false

        val suView = serverUnavailableView

        if(suView.background == null) {
            suView.background = ResourcesCompat.getDrawable(resources, R.drawable.server_unavailable_bg, theme)
        }

        suView.visibility = View.VISIBLE
    }

    override fun onServerAvailable() {
        gotoTodayReportButton.isEnabled = true
        gotoYesterdayReportButton.isEnabled = true
        gotoThisWeekReportButton.isEnabled = true
        gotoPrevWeekReportButton.isEnabled = true
        gotoThisMonthReportButton.isEnabled = true
        gotoPrevMonthReportButton.isEnabled = true

        serverUnavailableView.visibility = View.GONE
    }
}