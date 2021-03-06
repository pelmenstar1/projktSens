@file:Suppress("NOTHING_TO_INLINE")

package com.pelmenstar.projktSens.jserver.repo

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.core.database.sqlite.transaction
import com.pelmenstar.projktSens.shared.serialization.ValidationException
import com.pelmenstar.projktSens.shared.time.*
import com.pelmenstar.projktSens.weather.models.*

/**
 * Weather repository implementation that stores data in database
 */
class DbServerWeatherRepository private constructor(private val db: SQLiteDatabase) :
    WeatherRepository {
    override suspend fun clear() {
        db.compileStatement("DELETE FROM weather").use { statement ->
            statement.executeUpdateDelete()
        }
    }

    override suspend fun putMany(values: Array<WeatherInfo>) {
        db.transaction {
            for (weather in values) {
                putBlocking(weather)
            }
        }
    }

    override suspend fun put(weather: WeatherInfo) {
        putBlocking(weather)
    }

    private fun putBlocking(weather: WeatherInfo) {
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
            statement.executeInsert()
        }
    }

    override suspend fun getDayReport(@ShortDateInt date: Int): DayReport? {
        if (!ShortDate.isValid(date)) {
            throw ValidationException.invalidValue("date", date)
        }

        return query(createDayQuery(date)) { c ->
            if (c.count == 0) {
                return@query null
            }

            DayReport.create(CursorWeatherPropertyIterable(c))
        }
    }

    override suspend fun getDayRangeReport(@ShortDateInt start: Int, @ShortDateInt end: Int): DayRangeReport? {
        val sql = createDayRangeQuery(start, end)

        return query(sql) { c ->
            if (c.count == 0) {
                return@query null
            }

            DayRangeReport.create(CursorWeatherPropertyIterable(c))
        }
    }

    override suspend fun getAvailableDateRange(): ShortDateRange? {
        return query(QUERY_AVAILABLE_DATE_RANGE) { c ->
            c.moveToPosition(0)
            if (c.isNull(0) || c.isNull(1)) {
                return@query null
            }

            val minDate = ShortDate.ofEpochSecond(c.getLong(0))
            val maxDate = ShortDate.ofEpochSecond(c.getLong(1))

            ShortDateRange(minDate, maxDate)
        }
    }

    override suspend fun getLastWeather(): WeatherInfo? {
        return query(QUERY_LAST_WEATHER) { c ->
            if (c.count == 0) {
                return@query null
            }

            c.moveToPosition(0)

            val dateTime = ShortDateTime.ofEpochSecond(c.getLong(0))
            val temp = c.getFloat(1)
            val hum = c.getFloat(2)
            val press = c.getFloat(3)

            WeatherInfo(ValueUnitsPacked.CELSIUS_MM_OF_MERCURY, dateTime, temp, hum, press)
        }
    }

    private inline fun<T> query(sql: String, block: (c: Cursor) -> T): T {
        val cursor = db.rawQueryWithFactory(null, sql, null, null, null)

        return cursor.use(block)
    }

    private class OpenHelper(context: Context, name: String?) :
        SQLiteOpenHelper(context, name, null, 1) {
        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL("CREATE TABLE weather (datetime_epoch BIGINT,temperature FLOAT,humidity FLOAT,pressure FLOAT)")
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}
    }

    private class CursorWeatherPropertyIterable(private val cursor: Cursor) :
        WeatherPropertyIterable {
        override fun size(): Int = cursor.count

        override fun getUnits(): Int = ValueUnitsPacked.CELSIUS_MM_OF_MERCURY

        override fun getDateTime(): Long = ShortDateTime.ofEpochSecond(cursor.getLong(0))
        override fun getTemperature(): Float = cursor.getFloat(1)
        override fun getHumidity(): Float = cursor.getFloat(2)
        override fun getPressure(): Float = cursor.getFloat(3)

        override fun moveNext(): Boolean = cursor.moveToNext()
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
            val dayEpoch = ShortDate.toEpochDay(date).toLong()
            val startDateTime = dayEpoch * TimeConstants.SECONDS_IN_DAY
            val endDateTime = startDateTime + (TimeConstants.SECONDS_IN_DAY - 1)

            return dateRangeQuery(startDateTime, endDateTime)
        }

        private fun createDayRangeQuery(start: Int, end: Int): String {
            val startDateTime = ShortDateTime.startOfDayToEpochSecond(start)
            val endDateTime = ShortDateTime.endOfDayToEpochSecond(end)

            return dateRangeQuery(startDateTime, endDateTime)
        }

        private fun dateRangeQuery(startEpoch: Long, endEpoch: Long): String {
            return "SELECT datetime_epoch,temperature,humidity,pressure FROM weather WHERE datetime_epoch BETWEEN $startEpoch AND $endEpoch"
        }
    }
}