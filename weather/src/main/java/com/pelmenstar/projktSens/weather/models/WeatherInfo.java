package com.pelmenstar.projktSens.weather.models;

import com.pelmenstar.projktSens.shared.AppendableToStringBuilder;
import com.pelmenstar.projktSens.shared.RandomUtils;
import com.pelmenstar.projktSens.shared.serialization.ObjectSerializer;
import com.pelmenstar.projktSens.shared.serialization.Serializable;
import com.pelmenstar.projktSens.shared.serialization.ValidationException;
import com.pelmenstar.projktSens.shared.serialization.ValueReader;
import com.pelmenstar.projktSens.shared.serialization.ValueWriter;
import com.pelmenstar.projktSens.shared.time.ShortDateTime;
import com.pelmenstar.projktSens.shared.time.ShortDateTimeLong;

import org.jetbrains.annotations.NotNull;

import java.util.Random;

/**
 * A data class which contains information about weather (temperature, humidity, pressure) and timestamp of taking values.
 * Instances of this class can be serialized in format described below: <br/>
 * - units | 4 bytes <br/>
 * - dateTime | 5 bytes <br/>
 * - temperature | 4 bytes <br/>
 * - humidity | 4 bytes <br/>
 * - pressure | 4 bytes <br/>
 * <b>Total: 21 bytes</b>
 */
public final class WeatherInfo extends AppendableToStringBuilder {
    public static final int SERIALIZED_OBJECT_SIZE = 21;

    @NotNull
    public static final ObjectSerializer<WeatherInfo> SERIALIZER;

    public final int units;

    @ShortDateTimeLong
    public final long dateTime;

    public final float temperature;
    public final float humidity;
    public final float pressure;

    static {
        SERIALIZER = new Serializer();

        // not necessary but can affect to performance
        Serializable.registerSerializer(WeatherInfo.class, SERIALIZER);
    }

    public WeatherInfo(int units, @ShortDateTimeLong long dateTime, float temperature, float humidity, float pressure) {
        if (!ValueUnitsPacked.isValid(units)) {
            throw ValidationException.invalidValue("units", units);
        }

        if (!ShortDateTime.isValid(dateTime)) {
            throw ValidationException.invalidValue("dateTime", dateTime);
        }

        UnitValue.ensureValid(temperature, ValueUnitsPacked.getTemperatureUnit(units), "temperature");
        UnitValue.ensureValid(humidity, ValueUnit.HUMIDITY, "humidity");
        UnitValue.ensureValid(pressure, ValueUnitsPacked.getPressureUnit(units), "pressure");

        this.units = units;
        this.dateTime = dateTime;
        this.temperature = temperature;
        this.humidity = humidity;
        this.pressure = pressure;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WeatherInfo other = (WeatherInfo) o;

        return other.dateTime == dateTime &&
                other.temperature == temperature &&
                other.humidity == humidity &&
                other.pressure == pressure &&
                other.units == units;
    }

    @Override
    public int hashCode() {
        int result = Long.hashCode(dateTime);
        result = 31 * result + Float.floatToIntBits(temperature);
        result = 31 * result + Float.floatToIntBits(humidity);
        result = 31 * result + Float.floatToIntBits(pressure);
        result = 31 * result + units;

        return result;
    }

    @Override
    public void append(@NotNull StringBuilder sb) {
        sb.append("{dateTime=");
        ShortDateTime.append(dateTime, sb);
        sb.append(", temp=");
        sb.append(temperature);
        sb.append(", hum=");
        sb.append(humidity);
        sb.append(", press=");
        sb.append(pressure);
        sb.append(", units=");
        ValueUnitsPacked.append(units, sb);
        sb.append('}');
    }

    @NotNull
    public static WeatherInfo random(@NotNull Random random, @ShortDateTimeLong long dateTime) {
        return new WeatherInfo(
                ValueUnitsPacked.create(ValueUnit.CELSIUS, ValueUnit.MM_OF_MERCURY),
                dateTime,
                RandomUtils.nextFloat(random, -10f, 30f),
                RandomUtils.nextFloat(random, 50f, 90f),
                RandomUtils.nextFloat(random, 702f, 798f)
        );
    }

    private static final class Serializer implements ObjectSerializer<WeatherInfo> {
        @Override
        public int getSerializedObjectSize(@NotNull WeatherInfo value) {
            return SERIALIZED_OBJECT_SIZE;
        }

        @Override
        public void writeObject(@NotNull WeatherInfo value, @NotNull ValueWriter writer) {
            writer.emitInt32(value.units);
            writer.emitInt40(value.dateTime);
            writer.emitFloat(value.temperature);
            writer.emitFloat(value.humidity);
            writer.emitFloat(value.pressure);
        }

        @NotNull
        @Override
        public WeatherInfo readObject(@NotNull ValueReader reader) throws ValidationException {
            int units = reader.readInt32();
            long dateTime = reader.readInt40();
            float temperature = reader.readFloat();
            float humidity = reader.readFloat();
            float pressure = reader.readFloat();

            return new WeatherInfo(units, dateTime, temperature, humidity, pressure);
        }
    }
}
