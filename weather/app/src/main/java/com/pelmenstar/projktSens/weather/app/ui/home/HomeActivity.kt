package com.pelmenstar.projktSens.weather.app.ui.home

import android.animation.LayoutTransition
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.CalendarView
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
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
import com.pelmenstar.projktSens.weather.app.ui.WeatherInitScreen
import com.pelmenstar.projktSens.weather.app.ui.moon.MoonCalendarActivity
import com.pelmenstar.projktSens.weather.app.ui.settings.SETTINGS
import com.pelmenstar.projktSens.weather.app.ui.sunriseSunset.SunriseSunsetCalendarActivity
import com.pelmenstar.projktSens.weather.models.WeatherInfo

class HomeActivity : HomeButtonSupportActivity(), HomeContract.View {
    override val context: Context
        get() = this

    private lateinit var mainContent: View
    private lateinit var serverUnavailableView: View

    private lateinit var weatherView: ComplexWeatherView

    private lateinit var gotoTodayReportButton: Button
    private lateinit var gotoYesterdayReportButton: Button
    private lateinit var gotoThisWeekReportButton: Button
    private lateinit var gotoPrevWeekReportButton: Button
    private lateinit var gotoThisMonthReportButton: Button
    private lateinit var gotoPrevMonthReportButton: Button

    private lateinit var calendarView: CalendarView

    private lateinit var prettyDateFormatter: PrettyDateFormatter
    private lateinit var unitFormatter: UnitFormatter

    private var greaterThanZeroColor: Int = 0
    private var lessThanZeroColor: Int = 0
    private var zeroColor: Int = 0

    private var presenter: HomeContract.Presenter? = null
    private var firstStart: Boolean = true

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

        mainContent = createContent()
        content { View(this) }

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

            TextView {
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

                serverUnavailableView = this
            }

            ScrollView {
                linearLayoutParams(MATCH_PARENT, MATCH_PARENT)

                LinearLayout {
                    orientation = LinearLayout.VERTICAL


                    ComplexWeatherView {
                        linearLayoutParams(MATCH_PARENT, dp200)

                        weatherView = this
                    }

                    // day
                    Button(R.attr.materialButtonOutlinedStyle) {
                        linearLayoutParams(MATCH_PARENT, WRAP_CONTENT) {
                            leftMargin = dp3
                            rightMargin = dp3
                        }

                        setOnClickListener {
                            presenter?.startTodayReportView()
                        }

                        applyGoToButtonStyle()

                        text = res.getText(R.string.today)
                        gotoTodayReportButton = this
                    }

                    Button(R.attr.materialButtonOutlinedStyle) {
                        linearLayoutParams(MATCH_PARENT, WRAP_CONTENT) {
                            leftMargin = dp3
                            rightMargin = dp3
                        }

                        setOnClickListener {
                            presenter?.startYesterdayReportView()
                        }

                        applyGoToButtonStyle()

                        text = res.getText(R.string.yesterday)
                        gotoYesterdayReportButton = this
                    }

                    // week

                    Button(R.attr.materialButtonOutlinedStyle) {
                        linearLayoutParams(MATCH_PARENT, WRAP_CONTENT) {
                            leftMargin = dp3
                            rightMargin = dp3
                        }

                        setOnClickListener {
                            presenter?.startThisWeekReportView()
                        }

                        applyGoToButtonStyle()

                        text = res.getText(R.string.thisWeek)
                        gotoThisWeekReportButton = this
                    }

                    Button(R.attr.materialButtonOutlinedStyle) {
                        linearLayoutParams(MATCH_PARENT, WRAP_CONTENT) {
                            leftMargin = dp3
                            rightMargin = dp3
                        }

                        setOnClickListener {
                            presenter?.startPreviousWeekReportView()
                        }

                        applyGoToButtonStyle()

                        text = res.getText(R.string.prevWeek)
                        gotoPrevWeekReportButton = this
                    }

                    // month

                    Button(R.attr.materialButtonOutlinedStyle) {
                        linearLayoutParams(MATCH_PARENT, WRAP_CONTENT) {
                            leftMargin = dp3
                            rightMargin = dp3
                        }

                        setOnClickListener {
                            presenter?.startThisMonthReportView()
                        }

                        applyGoToButtonStyle()

                        text = res.getText(R.string.thisMonth)
                        gotoThisMonthReportButton = this
                    }

                    Button(R.attr.materialButtonOutlinedStyle) {
                        linearLayoutParams(MATCH_PARENT, WRAP_CONTENT) {
                            leftMargin = dp3
                            rightMargin = dp3
                        }

                        setOnClickListener {
                            presenter?.startPreviousMonthReportView()
                        }

                        applyGoToButtonStyle()

                        text = res.getText(R.string.prevMonth)
                        gotoPrevMonthReportButton = this
                    }

                    ScrollableCalendarView {
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

    @Suppress("NOTHING_TO_INLINE")
    private inline fun MaterialButton.applyGoToButtonStyle() {
        gravity = Gravity.START or Gravity.CENTER_VERTICAL
    }

    override fun onStart() {
        super.onStart()

        if(firstStart) {
            firstStart = false
            startPresenterInit()
        }

        presenter?.connectToWeatherChannel()
    }

    override fun onStop() {
        super.onStop()

        presenter?.disconnectFromWeatherChannel()
    }

    private fun onInitEnded() {
        setContentView(mainContent)
        presenter?.onInitEnded()
    }

    private fun startPresenterInit() {
        val presenter = presenter ?: return

        WeatherInitScreen(presenter.initContext).run {
            onInitEnded = Runnable { onInitEnded() }

            showNow(supportFragmentManager, null)
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

    override fun setCurrentTime(time: Int) {
        weatherView.setTime(time)
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

    override fun setCalendarMinDate(millis: Long) {
        calendarView.minDate = millis
    }

    override fun setCalendarMaxDate(millis: Long) {
        calendarView.maxDate = millis
    }
}