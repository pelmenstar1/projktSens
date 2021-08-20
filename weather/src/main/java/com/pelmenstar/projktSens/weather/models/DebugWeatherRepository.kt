package com.pelmenstar.projktSens.weather.models

import com.pelmenstar.projktSens.shared.time.ShortDateTime
import com.pelmenstar.projktSens.shared.time.TimeConstants
import java.util.*

suspend fun WeatherRepository.debugGenDb(startDate: Int, hours: Int) {
    clear()

    var current = ShortDateTime.of(startDate, 0)
    val random = Random(0)

    repeat(hours) {
        put(WeatherInfo.random(random, current))
        current = ShortDateTime.plusSeconds(current, TimeConstants.SECONDS_IN_HOUR.toLong())
    }

}