package com.pelmenstar.projktSens.weather.app.ui.home

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import com.pelmenstar.projktSens.shared.android.Message
import com.pelmenstar.projktSens.shared.android.mvp.BasePresenter
import com.pelmenstar.projktSens.shared.geo.Geolocation
import com.pelmenstar.projktSens.shared.geo.GeolocationProvider
import com.pelmenstar.projktSens.shared.intBitsToFloat
import com.pelmenstar.projktSens.shared.time.ShortDate
import com.pelmenstar.projktSens.shared.time.ShortDateInt
import com.pelmenstar.projktSens.shared.time.ShortDateTime
import com.pelmenstar.projktSens.weather.app.GeolocationCache
import com.pelmenstar.projktSens.weather.app.ui.report.DayReportActivity
import com.pelmenstar.projktSens.weather.app.ui.report.MonthReportActivity
import com.pelmenstar.projktSens.weather.app.ui.report.WeekReportActivity
import com.pelmenstar.projktSens.weather.models.WeatherChannelInfoProvider
import com.pelmenstar.projktSens.weather.models.WeatherDataSource
import com.pelmenstar.projktSens.weather.models.WeatherInfo
import com.pelmenstar.projktSens.weather.models.astro.MoonInfoProvider
import com.pelmenstar.projktSens.weather.models.astro.SunInfoProvider
import kotlinx.coroutines.*

class HomePresenter(
    private val sunInfoProvider: SunInfoProvider,
    private val moonInfoProvider: MoonInfoProvider,
    private val geoProvider: GeolocationProvider,
    private val dataSource: WeatherDataSource,
    private val weatherChannelInfoProvider: WeatherChannelInfoProvider
) : BasePresenter<HomeContract.View>(), HomeContract.Presenter {
    private val mainThread = MainThreadHandler(this)
    private val scope = CoroutineScope(Dispatchers.Default)

    private var refreshAstroJob: Job? = null
    private var weatherChannelJob: Job? = null

    @Volatile
    private var isInUnavailableState = false

    private var lastAstroRefreshedDate: Int = ShortDate.NONE

    private var lastGeolocation: Geolocation? = null

    override fun getLoadMinMaxCalendarHandler(): LazyLoadingCalendarView.LoadMinMaxHandler {
        return LazyLoadingCalendarView.LoadMinMaxHandler {
            dataSource.getAvailableDateRange()
        }
    }

    override fun getOnRetryGetLocationListener(): ComplexWeatherView.OnRetryGetLocationListener {
        return ComplexWeatherView.OnRetryGetLocationListener {
            startLoadingLocation()
        }
    }

    override fun attach(view: HomeContract.View) {
        super.attach(view)

        connectToWeatherChannel()
        startLoadingLocation()
    }

    override fun detach() {
        super.detach()

        mainThread.removeCallbacksAndMessages(null)
        mainThread.presenter = null

        scope.cancel()
    }

    private fun startLoadingLocation() {
        scope.launch {
            try {
                val location = geoProvider.getLastLocation()
                lastGeolocation = location
                GeolocationCache.set(location)

                postSetLocationLoaded(true)
                startRefreshingAstro()
            } catch (e: Exception) {
                Log.e(TAG, null, e)

                postSetLocationLoaded(false)
            }
        }
    }

    private fun postSetLocationLoaded(state: Boolean) {
        mainThread.sendMessage(Message {
            what = MSG_SET_LOCATION_LOADED
            arg1 = if(state) 1 else 0
        })
    }

    private fun startRefreshingAstro() {
        if(refreshAstroJob != null) {
            Log.w(TAG, "refreshAstro is already started")
            return
        }

        val location = lastGeolocation ?: throw NullPointerException("lastGeolocation")

        refreshAstroJob = scope.launch {
            while(isActive) {
                val nowDateTime = ShortDateTime.now()
                val nowDate = ShortDateTime.getDate(nowDateTime)
                val nowTime = ShortDateTime.getTime(nowDateTime)

                postSetCurrentTime(nowTime)

                if(nowDate != lastAstroRefreshedDate) {
                    lastAstroRefreshedDate = nowDate

                    val dayOfYear = ShortDate.getDayOfYear(nowDate)

                    val sunrise = sunInfoProvider.getSunriseTime(dayOfYear, location)
                    val sunset = sunInfoProvider.getSunriseTime(dayOfYear, location)
                    val moonPhase = moonInfoProvider.getMoonPhase(nowDate)

                    postSetMoonPhase(moonPhase)
                    postSetSunriseSunset(sunrise, sunset)
                }

                delay(1000)
            }
        }
    }

    private fun postSetMoonPhase(phase: Float) {
        mainThread.sendMessage(Message {
            what = MSG_SET_MOON_PHASE
            arg1 = phase.toBits()
        })
    }

    private fun postSetCurrentTime(time: Int) {
        mainThread.sendMessage(Message {
            what = MSG_SET_CURRENT_TIME
            arg1 = time
        })
    }

    private fun postSetSunriseSunset(sunrise: Int, sunset: Int) {
        mainThread.sendMessage(Message {
            what = MSG_SET_SUNRISE_SUNSET

            arg1 = sunrise
            arg2 = sunset
        })
    }

    override fun connectToWeatherChannel() {
        if (isInUnavailableState) {
            return
        }

        weatherChannelJob?.cancel()
        weatherChannelJob = scope.launch(Dispatchers.IO) {
            try {
                val interval = weatherChannelInfoProvider.receiveInterval
                val waitTime = weatherChannelInfoProvider.getWaitTimeForNextWeather() - 10 // -10 ms
                delay(waitTime)

                while (isActive) {
                    val value = dataSource.getLastWeather()
                    if(value != null) {
                        postOnWeatherReceived(value)
                    }

                    delay(interval)
                }
            } catch (e: Exception) {
                if(isActive) {
                    Log.e(TAG, "in weather channel", e)
                    postOnServerUnavailable()
                }
            }
        }
    }

    override fun disconnectFromWeatherChannel() {
        weatherChannelJob?.cancel()
    }

    override fun startTodayReportView() {
        val now = ShortDate.now()
        startDayReportView(now)
    }

    override fun startYesterdayReportView() {
        startDayReportView(ShortDate.nowAndMinusDays(1))
    }

    override fun startThisWeekReportView() {
        val now = ShortDate.now()
        val startDate = ShortDate.firstDayOfWeek(now)
        val endDate = ShortDate.plusDays(startDate, 7)
        startWeekReportView(startDate, endDate)
    }

    override fun startPreviousWeekReportView() {
        val weekAgoNow = ShortDate.nowAndMinusDays(7)
        val startDate = ShortDate.firstDayOfWeek(weekAgoNow)
        val endDate = ShortDate.plusDays(startDate, 7)
        startWeekReportView(startDate, endDate)
    }

    override fun startThisMonthReportView() {
        val date = ShortDate.now()
        startMonthReportView(ShortDate.getYear(date), ShortDate.getMonth(date))
    }

    override fun startPreviousMonthReportView() {
        val date = ShortDate.now()
        var year = ShortDate.getYear(date)
        var month = ShortDate.getMonth(date)

        month--
        if (month < 1) {
            month = 12
            year--
        }
        startMonthReportView(year, month)
    }

    override fun startDayReportView(@ShortDateInt date: Int) {
        val context = context

        val i = DayReportActivity.intent(context, date)
        context.startActivity(i)
    }

    private fun startMonthReportView(year: Int, month: Int) {
        val context = context

        val i = MonthReportActivity.intent(context, year, month)
        context.startActivity(i)
    }

    private fun startWeekReportView(@ShortDateInt startDate: Int, @ShortDateInt endDate: Int) {
        val context = context

        val i = WeekReportActivity.intent(context, startDate, endDate)
        context.startActivity(i)
    }

    private fun postOnWeatherReceived(value: WeatherInfo) {
        mainThread.sendMessage(Message {
            what = MSG_ON_WEATHER_RECEIVED
            obj = value
        })
    }

    private fun postOnServerUnavailable() {
        mainThread.sendMessage(Message { what = MSG_ON_SERVER_UNAVAILABLE })
    }

    private fun postOnServerAvailable() {
        mainThread.sendMessage(Message { what = MSG_ON_SERVER_AVAILABLE })
    }

    private fun onServerAvailable() {
        isInUnavailableState = false

        connectToWeatherChannel()
        view.onServerAvailable()
    }

    private fun onServerUnavailable() {
        isInUnavailableState = true

        disconnectFromWeatherChannel()
        view.onServerUnavailable()
    }

    private class MainThreadHandler(@JvmField @Volatile var presenter: HomePresenter?) : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            val p = presenter
            if (p == null) {
                Log.e(TAG, "presenter in main-thread handler is null")
                return
            }

            when (msg.what) {
                MSG_ON_SERVER_UNAVAILABLE -> {
                    p.onServerUnavailable()
                }
                MSG_ON_SERVER_AVAILABLE -> {
                    p.onServerAvailable()
                }
                MSG_ON_WEATHER_RECEIVED -> {
                    p.view.setWeather(msg.obj as WeatherInfo)
                }
                MSG_SET_CURRENT_TIME -> {
                    p.view.setCurrentTime(msg.arg1)
                }
                MSG_SET_SUNRISE_SUNSET -> {
                    p.view.setSunriseSunset(msg.arg1, msg.arg2)
                }
                MSG_SET_MOON_PHASE -> {
                    p.view.setMoonPhase(msg.arg1.intBitsToFloat())
                }
                MSG_SET_LOCATION_LOADED -> {
                    p.view.setLocationLoaded( msg.arg1 == 1)
                }
            }
        }
    }

    companion object {
        private const val TAG = "HomePresenter"

        private const val MSG_ON_SERVER_UNAVAILABLE = 0
        private const val MSG_ON_SERVER_AVAILABLE = 1
        private const val MSG_ON_WEATHER_RECEIVED = 2
        private const val MSG_SET_SUNRISE_SUNSET = 4
        private const val MSG_SET_MOON_PHASE = 5
        private const val MSG_SET_CURRENT_TIME = 6
        private const val MSG_SET_LOCATION_LOADED = 7

        const val TASK_GEOLOCATION = 0
        const val TASK_CALENDAR = 1
    }
}