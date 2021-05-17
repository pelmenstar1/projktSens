package com.pelmenstar.projktSens.weather.models;

import com.pelmenstar.projktSens.shared.AppendableToStringBuilder;
import com.pelmenstar.projktSens.shared.MyMath;
import com.pelmenstar.projktSens.shared.RandomUtils;
import com.pelmenstar.projktSens.shared.serialization.ValueReader;
import com.pelmenstar.projktSens.shared.serialization.ValueWriter;
import com.pelmenstar.projktSens.shared.serialization.ObjectSerializer;
import com.pelmenstar.projktSens.shared.serialization.Serializable;
import com.pelmenstar.projktSens.shared.serialization.ValidationException;
import com.pelmenstar.projktSens.shared.time.ShortDateTime;
import com.pelmenstar.projktSens.shared.time.ShortDateTimeLong;

import org.jetbrains.annotations.NotNull;

import java.util.Random;

/**
 * A data class which contains information about weather (temperature, humidity, pressure) and timestamp of taking values.
 * Instances of this class can be serialized in format described below: <br/>
 * - units | 4 bytes <br/>
 * - dateTime | 8 bytes <br/>
 * - temperature | 4 bytes <br/>
 * - humidity | 4 bytes <br/>
 * - pressure | 4 bytes <br/>
 * <b>Total: 24 bytes</b>
 */
public final class WeatherInfo extends AppendableToStringBuilder {
    public static final int SERIALIZED_OBJECT_SIZE = 24;

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
        if(!ValueUnitsPacked.isValid(units)) {
            throw ValidationException.invalidValue("units", units);
        }

        if(!ShortDateTime.isValid(dateTime)) {
            throw ValidationException.invalidValue("dateTime", dateTime);
        }

        if(!UnitValue.isValid(temperature, ValueUnitsPacked.getTemperatureUnit(units))) {
            throw ValidationException.invalidValue("temperature", temperature);
        }

        if(!UnitValue.isValid(humidity, ValueUnit.HUMIDITY)) {
            throw ValidationException.invalidValue("humidity", humidity);
        }

        if(!UnitValue.isValid(pressure, ValueUnitsPacked.getPressureUnit(units))) {
            throw ValidationException.invalidValue("pressure", pressure);
        }

        this.units = units;
        this.dateTime = dateTime;
        this.temperature = MyMath.round(temperature);
        this.humidity = MyMath.round(humidity);
        this.pressure = MyMath.round(pressure);
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
        int result = (int)(dateTime ^ (dateTime >>> 32));
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
                RandomUtils.nextInt(random, -10, 30),
                RandomUtils.nextInt(random, 50, 90),
                RandomUtils.nextInt(random, 702, 798)
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
            writer.emitInt64(value.dateTime);
            writer.emitFloat(value.temperature);
            writer.emitFloat(value.humidity);
            writer.emitFloat(value.pressure);
        }

        @NotNull
        @Override
        public WeatherInfo readObject(@NotNull ValueReader reader) throws ValidationException {
            int units = reader.readInt32();
            long dateTime = reader.readInt64();
            float temperature = reader.readFloat();
            float humidity = reader.readFloat();
            float pressure = reader.readFloat();

            return new WeatherInfo(units, dateTime, temperature, humidity, pressure);
        }
    }
}
