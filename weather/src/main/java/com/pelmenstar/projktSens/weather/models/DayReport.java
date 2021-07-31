package com.pelmenstar.projktSens.weather.models;

import com.pelmenstar.projktSens.shared.AppendableToStringBuilder;
import com.pelmenstar.projktSens.shared.StringUtils;
import com.pelmenstar.projktSens.shared.serialization.ObjectSerializer;
import com.pelmenstar.projktSens.shared.serialization.Serializable;
import com.pelmenstar.projktSens.shared.serialization.ValidationException;
import com.pelmenstar.projktSens.shared.serialization.ValueReader;
import com.pelmenstar.projktSens.shared.serialization.ValueWriter;
import com.pelmenstar.projktSens.shared.time.ShortDateTime;
import com.pelmenstar.projktSens.shared.time.ShortTime;
import com.pelmenstar.projktSens.shared.time.TimeInt;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * A data class to save information about day statistics.
 * Instances of {@link DayReport} can be serialized in format described below: <br/>
 * - ReportStats | {@link ReportStats#SERIALIZED_OBJECT_SIZE} <br/>
 * - entries.length | 2 bytes <br/>
 * [ <br/>
 * - time | 4 bytes <br/>
 * - temperature | 4 bytes <br/>
 * - humidity | 4 byte <br/>
 * - pressure | 4 bytes <br/>
 * ] <br/>
 */
public final class DayReport extends AppendableToStringBuilder {
    /**
     * Entry of {@link DayReport}
     */
    public static final class Entry extends AppendableToStringBuilder {
        @TimeInt
        public final int time;

        public final float temperature;
        public final float humidity;
        public final float pressure;

        public Entry(int time, float temp, float hum, float press) {
            this.time = time;
            this.temperature = temp;
            this.humidity = hum;
            this.pressure = press;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (other == null || getClass() != other.getClass()) return false;

            Entry o = (Entry) other;

            return time == o.time &&
                    temperature == o.temperature &&
                    humidity == o.humidity &&
                    pressure == o.pressure;
        }

        @Override
        public int hashCode() {
            int result = time;
            result = 31 * result + Float.floatToIntBits(temperature);
            result = 31 * result + Float.floatToIntBits(humidity);
            result = 31 * result + Float.floatToIntBits(pressure);

            return result;
        }

        @Override
        public void append(@NotNull StringBuilder builder) {
            builder.append("{time=");
            ShortTime.append(time, builder);
            builder.append(", temperature=");
            builder.append(temperature);
            builder.append(", humidity=");
            builder.append(humidity);
            builder.append(", pressure=");
            builder.append(pressure);
            builder.append('}');
        }
    }

    @NotNull
    public static final ObjectSerializer<DayReport> SERIALIZER;

    public final @NotNull Entry @NotNull [] entries;

    @NotNull
    public final ReportStats stats;

    static {
        SERIALIZER = new Serializer();

        // not necessary but can affect performance
        Serializable.registerSerializer(DayReport.class, SERIALIZER);
    }

    public DayReport(@NotNull Entry @NotNull [] entries, @NotNull ReportStats stats) {
        this.entries = entries;
        this.stats = stats;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        DayReport o = (DayReport) other;

        return Arrays.equals(entries, o.entries) && stats.equals(o.stats);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(entries);
        result = result * 31 + stats.hashCode();

        return result;
    }

    @Override
    public void append(@NotNull StringBuilder sb) {
        sb.append("{data=");
        StringUtils.appendArray(entries, sb);
        sb.append(", header=");
        stats.append(sb);
        sb.append('}');
    }

    @NotNull
    public static DayReport create(@NotNull WeatherInfo @NotNull [] data) {
        return create(new ArrayWeatherPropertyIterable(data));
    }

    @NotNull
    public static DayReport create(@NotNull WeatherPropertyIterable data) {
        int size = data.size();
        if(size == 0) {
            throw new IllegalArgumentException("Data is empty");
        }

        Entry[] entries = new Entry[size];

        float tempSum = 0;
        float humSum = 0;
        float pressSum = 0;

        float[] tempValues = new float[size];
        float[] humValues = new float[size];
        float[] pressValues = new float[size];
        int i = 0;

        while(data.moveNext()) {
            int units = data.getUnits();
            int tempUnit = ValueUnitsPacked.getTemperatureUnit(units);
            int pressUnit = ValueUnitsPacked.getPressureUnit(units);

            float temp = UnitValue.getValue(data.getTemperature(), tempUnit, ValueUnit.CELSIUS);
            float hum = data.getHumidity();
            float press = UnitValue.getValue(data.getPressure(), pressUnit, ValueUnit.MM_OF_MERCURY);

            tempValues[i] = temp;
            humValues[i] = hum;
            pressValues[i] = press;

            entries[i] = new Entry(ShortDateTime.getTime(data.getDateTime()), temp, hum, press);

            tempSum += temp;
            humSum += hum;
            pressSum += press;

            i++;
        }

        float invSize = 1f / size;
        float avgTemp = tempSum * invSize;
        float avgHum = humSum * invSize;
        float avgPress = pressSum * invSize;

        Arrays.sort(tempValues);
        Arrays.sort(humValues);
        Arrays.sort(pressValues);

        float minTemp = tempValues[0];
        float maxTemp = tempValues[size - 1];

        float minHum = humValues[0];
        float maxHum = humValues[size - 1];

        float minPress = pressValues[0];
        float maxPress = pressValues[size - 1];

        float medianTemp;
        float medianHum;
        float medianPress;

        int mid = size / 2;
        if(mid * 2 == size) {
            medianTemp = (tempValues[mid] + tempValues[mid + 1]) * 0.5f;
            medianHum = (humValues[mid] + humValues[mid + 1]) * 0.5f;
            medianPress = (pressValues[mid] + pressValues[mid + 1]) * 0.5f;
        } else {
            medianTemp = tempValues[mid];
            medianHum = humValues[mid];
            medianPress = pressValues[mid];
        }

        ParameterStats tempStats = new ParameterStats(minTemp, maxTemp, avgTemp, medianTemp);
        ParameterStats humStats = new ParameterStats(minHum, maxHum, avgHum, medianHum);
        ParameterStats pressStats = new ParameterStats(minPress, maxPress, avgPress, medianPress);

        ReportStats stats = new ReportStats(
                ValueUnitsPacked.CELSIUS_MM_OF_MERCURY,
                tempStats, humStats, pressStats
        );

        return new DayReport(entries, stats);
    }

    private static final class Serializer implements ObjectSerializer<DayReport> {
        @Override
        public int getSerializedObjectSize(@NotNull DayReport value) {
            return ReportStats.SERIALIZED_OBJECT_SIZE + 2 + (15 * value.entries.length);
        }

        @Override
        public void writeObject(@NotNull DayReport value, @NotNull ValueWriter writer) {
            ReportStats.SERIALIZER.writeObject(value.stats, writer);
            writer.emitInt16((short)value.entries.length);

            for(Entry e: value.entries) {
                writer.emitInt24(e.time);
                writer.emitFloat(e.temperature);
                writer.emitFloat(e.humidity);
                writer.emitFloat(e.pressure);
            }
        }

        @Override
        @NotNull
        public DayReport readObject(@NotNull ValueReader reader) throws ValidationException {
            ReportStats header = ReportStats.SERIALIZER.readObject(reader);

            int tempUnit = ValueUnitsPacked.getTemperatureUnit(header.units);
            int pressUnit = ValueUnitsPacked.getPressureUnit(header.units);

            int entriesLength = reader.readInt16();
            Entry[] entries = new Entry[entriesLength];

            for(int i = 0; i < entriesLength; i++) {
                int time = reader.readInt24();
                float temp = reader.readFloat();
                float hum = reader.readFloat();
                float press = reader.readFloat();

                if(!ShortTime.isValid(time)) {
                    throw ValidationException.invalidValue("time", time);
                }

                if(!UnitValue.isValid(temp, tempUnit)) {
                    throw ValidationException.invalidValue("temperature", temp);
                }

                if(!UnitValue.isValid(hum, ValueUnit.HUMIDITY)) {
                    throw ValidationException.invalidValue("humidity", hum);
                }

                if(!UnitValue.isValid(press, pressUnit)) {
                    throw ValidationException.invalidValue("pressure", press);
                }

                entries[i] = new Entry(time, temp, hum, press);
            }

            return new DayReport(entries, header);
        }
    }
}
