@file:Suppress("NOTHING_TO_INLINE")

package com.pelmenstar.projktSens.jserver.repo

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.database.sqlite.transaction
import com.pelmenstar.projktSens.shared.android.executeInsertSuspend
import com.pelmenstar.projktSens.shared.android.executeUpdateDeleteSuspend
import com.pelmenstar.projktSens.shared.android.querySuspend
import com.pelmenstar.projktSens.shared.serialization.ValidationException
import com.pelmenstar.projktSens.shared.time.*
import com.pelmenstar.projktSens.weather.models.*
import com.pelmenstar.projktSens.weather.models.ValueUnit
import java.util.*
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String

/**
 * Weather repository implementation that stores data in database
 */
class DbServerWeatherRepository private constructor(private val db: SQLiteDatabase) : WeatherRepository {
    override suspend fun clear() {
        db.compileStatement("DELETE FROM weather").use { statement ->
            statement.executeUpdateDeleteSuspend()
        }
    }

    override suspend fun putMany(values: Array<WeatherInfo>) {
        db.transaction {
            for(weather in values) {
                put(weather)
            }
        }
    }

    override suspend fun put(weather: WeatherInfo) {
        val temp = UnitValue.getValue(
            weather.temperature,
            ValueUnitsPacked.getTemperatureUnit(weather.units),
            ValueUnit.CELSIUS
        )
        val press = UnitValue.getValue(
            weather.pressure,
            ValueUnitsPacked.getPressureUnit(weather.units),
            ValueUnit.MM_OF_MERCURY
        )

        val sql = buildString {
            append("INSERT INTO weather (datetime_epoch,temperature,humidity,pressure) VALUES (")
            append(ShortDateTime.toEpochSecond(weather.dateTime))
            append(',')
            append(temp)
            append(',')
            append(weather.humidity)
            append(',')
            append(press)
            append(')')
        }

        db.compileStatement(sql).use { statement ->
            statement.executeInsertSuspend()
        }
    }

    override suspend fun getDayReport(@ShortDateInt date: Int): DayReport? {
        if(!ShortDate.isValid(date)) {
            throw ValidationException.invalidValue("date", date)
        }

        return db.querySuspend(createDayQuery(date)).use { c ->
            val count = c.count
            if (count == 0) {
                return null
            }

            val timeValues = IntArray(count)
            val tempValues = FloatArray(count)
            val humValues = FloatArray(count)
            val pressValues = FloatArray(count)

            for(i in 0 until count) {
                c.moveToPosition(i)

                timeValues[i] = (c.getLong(0) % 1000).toInt()
                tempValues[i] = c.getFloat(1)
                humValues[i] = c.getFloat(2)
                pressValues[i] = c.getFloat(3)
            }

            DayReport.create(ValueUnitsPacked.CELSIUS_MM_OF_MERCURY,
                timeValues,
                tempValues,
                humValues,
                pressValues
            )
        }
    }

    override suspend fun getDayRangeReport(range: ShortDateRange): DayRangeReport? {
        val sql = createDayRangeQuery(range)

        return db.querySuspend(sql).use { c ->
            val count = c.count

            if (count == 0) {
                return null
            }

            val dateValues = IntArray(count)
            val tempValues = FloatArray(count)
            val humValues = FloatArray(count)
            val pressValues = FloatArray(count)

            for(i in 0 until count) {
                c.moveToPosition(i)

                val epochDay = c.getLong(0) / TimeConstants.SECONDS_IN_DAY
                dateValues[i] = ShortDate.ofEpochDay(epochDay)
                tempValues[i] = c.getFloat(1)
                humValues[i] = c.getFloat(2)
                pressValues[i] = c.getFloat(3)
            }

            DayRangeReport.create(ValueUnitsPacked.CELSIUS_MM_OF_MERCURY,
                dateValues,
                tempValues,
                humValues,
                pressValues
            )
        }
    }

    override suspend fun getAvailableDateRange(): ShortDateRange? {
        db.querySuspend(QUERY_AVAILABLE_DATE_RANGE).use { c ->
            c.moveToPosition(0)

            if (c.isNull(0) || c.isNull(1)) {
                return null
            }

            val minDateTime = ShortDateTime.ofEpochSecond(c.getLong(0))
            val maxDateTime = ShortDateTime.ofEpochSecond(c.getLong(1))

            return ShortDateRange(
                ShortDateTime.getDate(minDateTime),
                ShortDateTime.getDate(maxDateTime)
            )
        }
    }

    override suspend fun getLastWeather(): WeatherInfo? {
        db.querySuspend(QUERY_LAST_WEATHER).use { c ->
            if (c.count == 0) {
                return null
            }

            c.moveToPosition(0)

            val dateTime = ShortDateTime.ofEpochSecond(c.getLong(0))
            val temp = c.getFloat(1)
            val hum = c.getFloat(2)
            val press = c.getFloat(3)

            return WeatherInfo(ValueUnitsPacked.CELSIUS_MM_OF_MERCURY, dateTime, temp, hum, press)
        }
    }

    private class OpenHelper(context: Context, name: String?) : SQLiteOpenHelper(context, name, null, 1) {
        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL("CREATE TABLE weather (datetime_epoch BIGINT,temperature FLOAT,humidity FLOAT,pressure FLOAT)")
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}
    }

    companion object {
        private const val FILE_DB_NAME = "data"

        private const val QUERY_AVAILABLE_DATE_RANGE =
            "SELECT min(datetime_epoch), max(datetime_epoch) FROM weather"

        private const val QUERY_LAST_WEATHER =
            "SELECT datetime_epoch,temperature,humidity,pressure FROM weather ORDER BY datetime_epoch ASC LIMIT 1"

        fun file(context: Context): DbServerWeatherRepository {
            return createDbRepo(context, FILE_DB_NAME)
        }

        fun inMemory(context: Context): DbServerWeatherRepository {
            return createDbRepo(context, null)
        }

        private fun createDbRepo(context: Context, fileName: String?): DbServerWeatherRepository {
            return DbServerWeatherRepository(OpenHelper(context, fileName).writableDatabase)
        }

        private fun createDayQuery(@ShortDateInt date: Int): String {
            val dayEpoch = ShortDate.toEpochDay(date)
            val startDateTime = dayEpoch * TimeConstants.SECONDS_IN_DAY
            val endDateTime = startDateTime + (TimeConstants.SECONDS_IN_DAY - 1)

           return dateRangeQuery(startDateTime, endDateTime)
        }

        private fun createDayRangeQuery(range: ShortDateRange): String {
            val startDateTime = ShortDateTime.toEpochSecond(ShortDateTime.startOfDay(range.start))
            val endDateTime = ShortDateTime.toEpochSecond(ShortDateTime.endOfDay(range.endInclusive))

            return dateRangeQuery(startDateTime, endDateTime)
        }

        private inline fun dateRangeQuery(startEpoch: Long, endEpoch: Long): String {
            return "SELECT datetime_epoch,temperature,humidity,pressure FROM weather WHERE datetime_epoch BETWEEN $startEpoch AND $endEpoch"
        }
    }
}