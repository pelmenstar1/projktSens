package com.pelmenstar.projktSens.weather.models;

import com.pelmenstar.projktSens.shared.AppendableToStringBuilder;
import com.pelmenstar.projktSens.shared.StringUtils;
import com.pelmenstar.projktSens.shared.serialization.ObjectSerializer;
import com.pelmenstar.projktSens.shared.serialization.Serializable;
import com.pelmenstar.projktSens.shared.serialization.ValidationException;
import com.pelmenstar.projktSens.shared.serialization.ValueReader;
import com.pelmenstar.projktSens.shared.serialization.ValueWriter;
import com.pelmenstar.projktSens.shared.time.ShortDate;
import com.pelmenstar.projktSens.shared.time.ShortDateInt;
import com.pelmenstar.projktSens.shared.time.ShortDateTime;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * A data class to save information about month statistics.
 * Instances of {@link DayRangeReport} can be serialized in format described below: <br/>
 * - ReportStats | {@link ReportStats#SERIALIZED_OBJECT_SIZE} <br/>
 * - entries.length | 2 bytes <br/>
 * [ <br/>
 * - date | 4 bytes <br/>
 * - minTemperature | 4 bytes <br/>
 * - maxTemperature | 4 bytes <br/>
 * - minHumidity | 4 byte <br/>
 * - maxHumidity | 4 byte <br/>
 * - minPressure | 4 bytes <br/>
 * - maxPressure | 4 bytes <br/>
 * ] <br/>
 */
public final class DayRangeReport extends AppendableToStringBuilder {
    public static final class Entry extends AppendableToStringBuilder {
        @ShortDateInt
        public final int date;

        public final float minTemperature;
        public final float maxTemperature;

        public final float minHumidity;
        public final float maxHumidity;

        public final float minPressure;
        public final float maxPressure;

        public Entry(@ShortDateInt int date,
                     float minTemperature, float maxTemperature,
                     float minHumidity, float maxHumidity,
                     float minPressure, float maxPressure) {
            this.date = date;
            this.minTemperature = minTemperature;
            this.maxTemperature = maxTemperature;

            this.minHumidity = minHumidity;
            this.maxHumidity = maxHumidity;

            this.minPressure = minPressure;
            this.maxPressure = maxPressure;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) return true;
            if (other == null || getClass() != other.getClass()) return false;

            Entry o = (Entry) other;

            return date == o.date &&
                    minTemperature == o.minTemperature && maxTemperature == o.maxTemperature &&
                    minHumidity == o.minHumidity && maxHumidity == o.maxHumidity &&
                    minPressure == o.minPressure && maxPressure == o.maxPressure;
        }

        @Override
        public int hashCode() {
            int result = date;
            result = 31 * result + Float.floatToIntBits(minTemperature);
            result = 31 * result + Float.floatToIntBits(maxTemperature);

            result = 31 * result + Float.floatToIntBits(minHumidity);
            result = 31 * result + Float.floatToIntBits(maxHumidity);

            result = 31 * result + Float.floatToIntBits(minPressure);
            result = 31 * result + Float.floatToIntBits(maxPressure);

            return result;
        }

        @Override
        public void append(@NotNull StringBuilder sb) {
            sb.append("{date=");
            ShortDate.append(date, sb);
            sb.append(", minTemperature=");
            sb.append(minTemperature);
            sb.append(", maxTemperature=");
            sb.append(maxTemperature);

            sb.append(", minHumidity=");
            sb.append(minHumidity);
            sb.append(", maxHumidity=");
            sb.append(maxHumidity);

            sb.append(", minPressure=");
            sb.append(minPressure);
            sb.append(", maxPressure=");
            sb.append(maxPressure);

            sb.append('}');
        }
    }

    @NotNull
    public static final ObjectSerializer<DayRangeReport> SERIALIZER;

    public final @NotNull Entry @NotNull [] entries;

    @NotNull
    public final ReportStats stats;

    static {
        SERIALIZER = new Serializer();

        // not necessary but can affect to performance
        Serializable.registerSerializer(DayRangeReport.class, SERIALIZER);
    }

    public DayRangeReport(@NotNull Entry @NotNull [] entries, @NotNull ReportStats stats) {
        int tempUnit = ValueUnitsPacked.getTemperatureUnit(stats.units);
        int pressUnit = ValueUnitsPacked.getPressureUnit(stats.units);

        for(Entry entry: entries) {
            if(!ShortDate.isValid(entry.date)) {
                throw ValidationException.invalidValue("entry date", entry.date);
            }

            UnitValue.ensureValid(entry.minTemperature, tempUnit, "entry min temperature");
            UnitValue.ensureValid(entry.maxTemperature, tempUnit, "entry max temperature");

            UnitValue.ensureValid(entry.minHumidity, ValueUnit.HUMIDITY, "entry min humidity");
            UnitValue.ensureValid(entry.maxHumidity, ValueUnit.HUMIDITY, "entry max humidity");

            UnitValue.ensureValid(entry.minPressure, pressUnit, "entry min pressure");
            UnitValue.ensureValid(entry.maxPressure, pressUnit, "entry max pressure");
        }

        this.entries = entries;
        this.stats = stats;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        DayRangeReport o = (DayRangeReport) other;

        return stats.equals(o.stats) && Arrays.equals(entries, o.entries);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(entries);
        result = 31 * result + stats.hashCode();
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
    public static DayRangeReport create(@NotNull WeatherInfo @NotNull [] values) {
        return create(new ArrayWeatherPropertyIterable(values));
    }

    @NotNull
    public static DayRangeReport create(@NotNull WeatherPropertyIterable data) {
        int length = data.size();
        if (length == 0) {
            throw new IllegalArgumentException("Data is empty");
        }

        Entry[] entries = new Entry[0];

        float tempSum = 0;
        float humSum = 0;
        float pressSum = 0;

        float dayMinTemp = Float.MAX_VALUE;
        float dayMaxTemp = Float.MIN_VALUE;

        float dayMinHum = Float.MAX_VALUE;
        float dayMaxHum = Float.MIN_VALUE;

        float dayMinPress = Float.MAX_VALUE;
        float dayMaxPress = Float.MIN_VALUE;

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

        boolean first = true;
        int currentDate = 0;
        int maxIdx = length - 1;

        float[] tempValues = new float[length];
        float[] humValues = new float[length];
        float[] pressValues = new float[length];

        int i = 0;
        while (data.moveNext()) {
            int units = data.getUnits();
            int tempUnit = ValueUnitsPacked.getTemperatureUnit(units);
            int pressUnit = ValueUnitsPacked.getPressureUnit(units);

            long dateTime = data.getDateTime();
            int date = ShortDateTime.getDate(dateTime);
            float temp = UnitValue.getValue(data.getTemperature(), tempUnit, ValueUnit.CELSIUS);
            float hum = data.getHumidity();
            float press = UnitValue.getValue(data.getPressure(), pressUnit, ValueUnit.MM_OF_MERCURY);

            tempValues[i] = temp;
            humValues[i] = hum;
            pressValues[i] = press;

            if (first) {
                first = false;
                currentDate = date;
            }

            if (currentDate != date || i == maxIdx) {
                Entry e = new Entry(
                        currentDate,
                        dayMinTemp, dayMaxTemp,
                        dayMinHum, dayMaxHum,
                        dayMinPress, dayMaxPress
                );

                Entry[] newEntries = new Entry[entries.length + 1];
                System.arraycopy(entries, 0, newEntries, 0, entries.length);
                newEntries[entries.length] = e;
                entries = newEntries;

                dayMinTemp = Float.MAX_VALUE;
                dayMaxTemp = Float.MIN_VALUE;

                dayMinHum = Float.MAX_VALUE;
                dayMaxHum = Float.MIN_VALUE;

                dayMinPress = Float.MAX_VALUE;
                dayMaxPress = Float.MIN_VALUE;

                currentDate = date;
            }

            // day min-max
            if (temp < dayMinTemp) {
                dayMinTemp = temp;
            }

            if (temp > dayMaxTemp) {
                dayMaxTemp = temp;
            }

            if (hum < dayMinHum) {
                dayMinHum = hum;
            }

            if (hum > dayMaxHum) {
                dayMaxHum = hum;
            }

            if (press < dayMinPress) {
                dayMinPress = press;
            }

            if (press > dayMaxPress) {
                dayMaxPress = press;
            }

            // general min-max
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

            tempSum += temp;
            humSum += hum;
            pressSum += press;

            i++;
        }

        float invLength = 1f / length;
        float avgTemp = tempSum * invLength;
        float avgHum = humSum * invLength;
        float avgPress = pressSum * invLength;

        Arrays.sort(tempValues);
        Arrays.sort(humValues);
        Arrays.sort(pressValues);

        float medianTemp;
        float medianHum;
        float medianPress;

        int mid = length / 2;
        if (mid * 2 == length) {
            medianTemp = (tempValues[mid] + tempValues[mid + 1]) * 0.5f;
            medianHum = (humValues[mid] + humValues[mid + 1]) * 0.5f;
            medianPress = (pressValues[mid] + pressValues[mid + 1]) * 0.5f;
        } else {
            medianTemp = tempValues[mid];
            medianHum = humValues[mid];
            medianPress = pressValues[mid];
        }

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

        return new DayRangeReport(entries, stats);
    }

    private static final class Serializer implements ObjectSerializer<DayRangeReport> {
        @Override
        public int getSerializedObjectSize(@NotNull DayRangeReport value) {
            return ReportStats.SERIALIZED_OBJECT_SIZE + 2 + (27 * value.entries.length);
        }

        @Override
        public void writeObject(@NotNull DayRangeReport value, @NotNull ValueWriter writer) {
            ReportStats.SERIALIZER.writeObject(value.stats, writer);
            writer.int16((short) value.entries.length);

            for (Entry e : value.entries) {
                writer.int24(e.date);

                writer.float32(e.minTemperature);
                writer.float32(e.maxTemperature);

                writer.float32(e.minHumidity);
                writer.float32(e.maxHumidity);

                writer.float32(e.minPressure);
                writer.float32(e.maxPressure);
            }
        }

        @Override
        @NotNull
        public DayRangeReport readObject(@NotNull ValueReader reader) throws ValidationException {
            ReportStats stats = ReportStats.SERIALIZER.readObject(reader);


            int entriesLength = reader.int16();
            Entry[] data = new Entry[entriesLength];

            for (int i = 0; i < entriesLength; i++) {
                int date = reader.int24();

                float minTemp = reader.float32();
                float maxTemp = reader.float32();

                float minHum = reader.float32();
                float maxHum = reader.float32();

                float minPress = reader.float32();
                float maxPress = reader.float32();

                data[i] = new Entry(
                        date,
                        minTemp, maxTemp,
                        minHum, maxHum,
                        minPress, maxPress
                );
            }

            return new DayRangeReport(data, stats);
        }
    }
}
