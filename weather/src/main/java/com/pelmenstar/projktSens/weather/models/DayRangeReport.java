package com.pelmenstar.projktSens.weather.models;

import com.pelmenstar.projktSens.shared.AppendableToStringBuilder;
import com.pelmenstar.projktSens.shared.MyMath;
import com.pelmenstar.projktSens.shared.StringUtils;
import com.pelmenstar.projktSens.shared.serialization.ObjectSerializer;
import com.pelmenstar.projktSens.shared.serialization.Serializable;
import com.pelmenstar.projktSens.shared.serialization.ValidationException;
import com.pelmenstar.projktSens.shared.serialization.ValueReader;
import com.pelmenstar.projktSens.shared.serialization.ValueWriter;
import com.pelmenstar.projktSens.shared.time.ShortDate;
import com.pelmenstar.projktSens.shared.time.ShortDateInt;

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
                     float minHumidity,    float maxHumidity,
                     float minPressure,    float maxPressure) {
            this.date = date;
            this.minTemperature = MyMath.round(minTemperature);
            this.maxTemperature = MyMath.round(maxTemperature);

            this.minHumidity = MyMath.round(minHumidity);
            this.maxHumidity = MyMath.round(maxHumidity);

            this.minPressure = MyMath.round(minPressure);
            this.maxPressure = MyMath.round(maxPressure);
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
    public static DayRangeReport create(int units,
                                        int @NotNull [] dateValues,
                                        float @NotNull [] tempValues,
                                        float @NotNull [] humValues,
                                        float @NotNull [] pressValues) {
        int length = tempValues.length;
        if(length == 0) {
            throw new IllegalArgumentException("Data is empty");
        }

        Entry[] entries = new Entry[0];

        int tempUnit = ValueUnitsPacked.getTemperatureUnit(units);
        int pressUnit = ValueUnitsPacked.getPressureUnit(units);

        float tempSum = 0;
        float humSum = 0;
        float pressSum = 0;

        float dayMinTemp = Float.MAX_VALUE;
        float dayMaxTemp = Float.MIN_VALUE;

        float dayMinHum = Float.MAX_VALUE;
        float dayMaxHum = Float.MIN_VALUE;

        float dayMinPress = Float.MAX_VALUE;
        float dayMaxPress = Float.MIN_VALUE;

        boolean first = true;
        int currentDate = 0;
        int maxIdx = length - 1;

        for(int i = 0; i < length; i++) {
            int date = dateValues[i];
            float temp = UnitValue.getValue(tempValues[i], tempUnit, ValueUnit.CELSIUS);
            float hum = humValues[i];
            float press = UnitValue.getValue(pressValues[i], pressUnit, ValueUnit.MM_OF_MERCURY);

            if (first) {
                first = false;
                currentDate = date;
            }

            if (currentDate != date || i == maxIdx) {
                currentDate = date;

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
            }

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

            tempSum += temp;
            humSum += hum;
            pressSum += press;
        }

        float invLength = 1f / length;
        float avgTemp = tempSum * invLength;
        float avgHum = humSum * invLength;
        float avgPress = pressSum * invLength;

        Arrays.sort(tempValues);
        Arrays.sort(humValues);
        Arrays.sort(pressValues);

        float minTemp = tempValues[0];
        float maxTemp = tempValues[maxIdx];

        float minHum = humValues[0];
        float maxHum = humValues[maxIdx];

        float minPress = pressValues[0];
        float maxPress = pressValues[maxIdx];

        float medianTemp;
        float medianHum;
        float medianPress;

        int mid = length / 2;
        if(mid * 2 == length) {
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

        return new DayRangeReport(entries, stats);
    }

    private static final class Serializer implements ObjectSerializer<DayRangeReport> {
        @Override
        public int getSerializedObjectSize(@NotNull DayRangeReport value) {
            return ReportStats.SERIALIZED_OBJECT_SIZE + 2 + (28 * value.entries.length);
        }

        @Override
        public void writeObject(@NotNull DayRangeReport value, @NotNull ValueWriter writer) {
            ReportStats.SERIALIZER.writeObject(value.stats, writer);
            writer.emitInt16((short)value.entries.length);

            for(Entry e: value.entries) {
                writer.emitInt32(e.date);

                writer.emitFloat(e.minTemperature);
                writer.emitFloat(e.maxTemperature);

                writer.emitFloat(e.minHumidity);
                writer.emitFloat(e.maxHumidity);

                writer.emitFloat(e.minPressure);
                writer.emitFloat(e.maxPressure);
            }
        }

        @Override
        @NotNull
        public DayRangeReport readObject(@NotNull ValueReader reader) throws ValidationException {
            ReportStats stats = ReportStats.SERIALIZER.readObject(reader);
            int tempUnit = ValueUnitsPacked.getTemperatureUnit(stats.units);
            int pressUnit = ValueUnitsPacked.getPressureUnit(stats.units);

            int entriesLength = reader.readInt16();
            Entry[] data = new Entry[entriesLength];

            for(int i = 0; i < entriesLength; i++) {
                int date = reader.readInt32();

                float minTemp = reader.readFloat();
                float maxTemp = reader.readFloat();

                float minHum = reader.readFloat();
                float maxHum = reader.readFloat();

                float minPress = reader.readFloat();
                float maxPress = reader.readFloat();

                if(!ShortDate.isValid(date)) {
                    throw ValidationException.invalidValue("date", date);
                }

                if(!UnitValue.isValid(minTemp, tempUnit)) {
                    throw ValidationException.invalidValue("minTemp", minTemp);
                }

                if(!UnitValue.isValid(maxTemp, tempUnit)) {
                    throw ValidationException.invalidValue("maxTemp", maxTemp);
                }

                if(!UnitValue.isValid(minHum, ValueUnit.HUMIDITY)) {
                    throw ValidationException.invalidValue("minHum", minHum);
                }

                if(!UnitValue.isValid(maxHum, ValueUnit.HUMIDITY)) {
                    throw ValidationException.invalidValue("maxHum", maxHum);
                }

                if(!UnitValue.isValid(minPress, pressUnit)) {
                    throw ValidationException.invalidValue("minPress", minPress);
                }

                if(!UnitValue.isValid(maxPress, pressUnit)) {
                    throw ValidationException.invalidValue("maxPress", maxPress);
                }

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
