package com.pelmenstar.projktSens.weather.models;

import com.pelmenstar.projktSens.shared.AppendableToStringBuilder;
import com.pelmenstar.projktSens.shared.Median;
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
 */
public final class DayReport extends AppendableToStringBuilder {
    /**
     * Entry of {@link DayReport}
     */
    public static final class Entry extends AppendableToStringBuilder {
        private static final int FIELD_TEMPERATURE = 0;
        private static final int FIELD_HUMIDITY = 1;
        private static final int FIELD_PRESSURE = 2;

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

        private float getField(int type) {
            switch (type) {
                case FIELD_TEMPERATURE: return temperature;
                case FIELD_HUMIDITY: return humidity;
                case FIELD_PRESSURE: return pressure;
                default: throw new RuntimeException("Invalid field type");
            }
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
        int tempUnit = ValueUnitsPacked.getTemperatureUnit(stats.units);
        int pressUnit = ValueUnitsPacked.getPressureUnit(stats.units);

        for(Entry entry: entries) {
            if(!ShortTime.isValid(entry.time)) {
                throw ValidationException.invalidValue("entry time", entry.time);
            }

            UnitValue.ensureValid(entry.temperature, tempUnit, "entry temperature");
            UnitValue.ensureValid(entry.humidity, ValueUnit.HUMIDITY, "entry humidity");
            UnitValue.ensureValid(entry.pressure, pressUnit, "entry pressure");
        }

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
        if (size == 0) {
            throw new IllegalArgumentException("Data is empty");
        }

        Entry[] entries = new Entry[size];

        float tempSum = 0;
        float humSum = 0;
        float pressSum = 0;

        int i = 0;

        float minTempValue = Float.MAX_VALUE;
        long minTempDt = ShortDateTime.NONE;

        float maxTempValue = Float.MIN_VALUE;
        long maxTempDt = ShortDateTime.NONE;

        float minHumValue = Float.MAX_VALUE;
        long minHumDt = ShortDateTime.NONE;

        float maxHumValue = Float.MIN_VALUE;
        long maxHumDt = ShortDateTime.NONE;

        float minPressValue = Float.MAX_VALUE;
        long minPressDt = ShortDateTime.NONE;

        float maxPressValue = Float.MIN_VALUE;
        long maxPressDt = ShortDateTime.NONE;

        while (data.moveNext()) {
            int units = data.getUnits();
            int tempUnit = ValueUnitsPacked.getTemperatureUnit(units);
            int pressUnit = ValueUnitsPacked.getPressureUnit(units);

            long dateTime = data.getDateTime();
            float temp = UnitValue.getValue(data.getTemperature(), tempUnit, ValueUnit.CELSIUS);
            float hum = data.getHumidity();
            float press = UnitValue.getValue(data.getPressure(), pressUnit, ValueUnit.MM_OF_MERCURY);

            entries[i++] = new Entry(ShortDateTime.getTime(dateTime), temp, hum, press);

            tempSum += temp;
            humSum += hum;
            pressSum += press;

            if (temp < minTempValue) {
                minTempValue = temp;
                minTempDt = dateTime;
            }

            if (temp > maxTempValue) {
                maxTempValue = temp;
                maxTempDt = dateTime;
            }

            if (hum < minHumValue) {
                minHumValue = hum;
                minHumDt = dateTime;
            }

            if (hum > maxHumValue) {
                maxHumValue = hum;
                maxHumDt = dateTime;
            }

            if (press < minPressValue) {
                minPressValue = press;
                minPressDt = dateTime;
            }

            if (press > maxPressValue) {
                maxPressValue = press;
                maxPressDt = dateTime;
            }
        }

        float invSize = 1f / size;
        float avgTemp = tempSum * invSize;
        float avgHum = humSum * invSize;
        float avgPress = pressSum * invSize;

        float medianTemp;
        float medianHum;
        float medianPress;

        float[] values = new float[entries.length];

        medianTemp = computeMedian(values, entries, Entry.FIELD_TEMPERATURE);
        medianHum = computeMedian(values, entries, Entry.FIELD_HUMIDITY);
        medianPress = computeMedian(values, entries, Entry.FIELD_PRESSURE);

        ParameterStats tempStats = new ParameterStats(
                new ValueWithDate(minTempDt, minTempValue),
                new ValueWithDate(maxTempDt, maxTempValue),
                avgTemp,
                medianTemp
        );
        ParameterStats humStats = new ParameterStats(
                new ValueWithDate(minHumDt, minHumValue),
                new ValueWithDate(maxHumDt, maxHumValue),
                avgHum,
                medianHum
        );
        ParameterStats pressStats = new ParameterStats(
                new ValueWithDate(minPressDt, minPressValue),
                new ValueWithDate(maxPressDt, maxPressValue),
                avgPress,
                medianPress
        );

        ReportStats stats = new ReportStats(
                ValueUnitsPacked.CELSIUS_MM_OF_MERCURY,
                tempStats, humStats, pressStats
        );

        return new DayReport(entries, stats);
    }

    private static float computeMedian(
            float[] cachedValues,
            @NotNull Entry @NotNull [] entries,
            int field
    ) {
        for(int i = 0; i < entries.length; i++) {
            cachedValues[i] = entries[i].getField(field);
        }

        return Median.compute(cachedValues);
    }

    private static final class Serializer implements ObjectSerializer<DayReport> {
        @Override
        public int getSerializedObjectSize(@NotNull DayReport value) {
            return ReportStats.SERIALIZED_OBJECT_SIZE + 2 + (15 * value.entries.length);
        }

        @Override
        public void writeObject(@NotNull DayReport value, @NotNull ValueWriter writer) {
            ReportStats.SERIALIZER.writeObject(value.stats, writer);
            writer.int16((short) value.entries.length);

            for (Entry e : value.entries) {
                writer.int24(e.time);
                writer.float32(e.temperature);
                writer.float32(e.humidity);
                writer.float32(e.pressure);
            }
        }

        @Override
        @NotNull
        public DayReport readObject(@NotNull ValueReader reader) throws ValidationException {
            ReportStats stats = ReportStats.SERIALIZER.readObject(reader);

            int entriesLength = reader.int16();
            Entry[] entries = new Entry[entriesLength];

            for (int i = 0; i < entriesLength; i++) {
                int time = reader.int24();
                float temp = reader.float32();
                float hum = reader.float32();
                float press = reader.float32();

                entries[i] = new Entry(time, temp, hum, press);
            }

            return new DayReport(entries, stats);
        }
    }
}
