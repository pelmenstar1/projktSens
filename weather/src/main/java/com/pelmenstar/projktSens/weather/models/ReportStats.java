package com.pelmenstar.projktSens.weather.models;

import com.pelmenstar.projktSens.shared.AppendableToStringBuilder;
import com.pelmenstar.projktSens.shared.serialization.ValueReader;
import com.pelmenstar.projktSens.shared.serialization.ValueWriter;
import com.pelmenstar.projktSens.shared.serialization.ObjectSerializer;
import com.pelmenstar.projktSens.shared.serialization.Serializable;
import com.pelmenstar.projktSens.shared.serialization.ValidationException;

import org.jetbrains.annotations.NotNull;

/**
 * A data class which contains basic statistics information.
 */
public final class ReportStats extends AppendableToStringBuilder {
    public static final int SERIALIZED_OBJECT_SIZE = 4 + 3 * ParameterStats.SERIALIZED_OBJECT_SIZE;

    @NotNull
    public static final ObjectSerializer<ReportStats> SERIALIZER;

    public final int units;

    @NotNull
    public final ParameterStats temperature;

    @NotNull
    public final ParameterStats humidity;

    @NotNull
    public final ParameterStats pressure;

    static {
        SERIALIZER = new Serializer();

        // not necessary but can affect to performance
        Serializable.registerSerializer(ReportStats.class, SERIALIZER);
    }

    public ReportStats(int units,
                       @NotNull ParameterStats tempStats,
                       @NotNull ParameterStats humStats,
                       @NotNull ParameterStats pressStats) {
        this.units = units;
        this.temperature = tempStats;
        this.humidity = humStats;
        this.pressure = pressStats;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        ReportStats o = (ReportStats) other;

        return units == o.units &&
                temperature.equals(o.temperature) &&
                humidity.equals(o.humidity) &&
                pressure.equals(o.pressure);
    }

    @Override
    public int hashCode() {
        int result = units;
        result = 31 * result + temperature.hashCode();
        result = 31 * result + humidity.hashCode();
        result = 31 * result + pressure.hashCode();

        return result;
    }

    @Override
    public void append(@NotNull StringBuilder sb) {
        sb.append("{units=");
        ValueUnitsPacked.append(units, sb);
        sb.append(", temperature=");
        temperature.append(sb);
        sb.append(", humidity=");
        humidity.append(sb);
        sb.append(", pressure=");
        pressure.append(sb);
        sb.append('}');
    }

    private static final class Serializer implements ObjectSerializer<ReportStats> {
        @Override
        public int getSerializedObjectSize(@NotNull ReportStats value) {
            return SERIALIZED_OBJECT_SIZE;
        }

        @Override
        public void writeObject(@NotNull ReportStats value, @NotNull ValueWriter writer) {
            writer.emitInt32(value.units);
            ParameterStats.SERIALIZER.writeObject(value.temperature, writer);
            ParameterStats.SERIALIZER.writeObject(value.humidity, writer);
            ParameterStats.SERIALIZER.writeObject(value.pressure, writer);
        }

        @NotNull
        @Override
        public ReportStats readObject(@NotNull ValueReader reader) throws ValidationException {
            int units = reader.readInt32();
            if(!ValueUnitsPacked.isValid(units)) {
                throw ValidationException.invalidValue("units", units);
            }

            int tempUnit = ValueUnitsPacked.getTemperatureUnit(units);
            int pressUnit = ValueUnitsPacked.getPressureUnit(units);

            ParameterStats temp = ParameterStats.SERIALIZER.readObject(reader);
            ParameterStats hum = ParameterStats.SERIALIZER.readObject(reader);
            ParameterStats press = ParameterStats.SERIALIZER.readObject(reader);

            // validate temperature
            if(!UnitValue.isValid(temp.min.value, tempUnit)) {
                throw ValidationException.invalidValue("min temperature", temp.min.value);
            }

            if(!UnitValue.isValid(temp.max.value, tempUnit)) {
                throw ValidationException.invalidValue("max temperature", temp.max.value);
            }

            if(!UnitValue.isValid(temp.avg, tempUnit)) {
                throw ValidationException.invalidValue("avg temperature", temp.avg);
            }

            // validate humidity
            if(!UnitValue.isValid(hum.min.value, ValueUnit.HUMIDITY)) {
                throw ValidationException.invalidValue("min humidity", hum.min.value);
            }

            if(!UnitValue.isValid(hum.max.value, ValueUnit.HUMIDITY)) {
                throw ValidationException.invalidValue("max humidity", hum.max.value);
            }

            if(!UnitValue.isValid(hum.avg, ValueUnit.HUMIDITY)) {
                throw ValidationException.invalidValue("avg humidity", hum.avg);
            }

            // validate pressure
            if(!UnitValue.isValid(press.min.value, pressUnit)) {
                throw ValidationException.invalidValue("min pressure", press.min.value);
            }

            if(!UnitValue.isValid(press.max.value, pressUnit)) {
                throw ValidationException.invalidValue("max pressure", press.max.value);
            }

            if(!UnitValue.isValid(press.avg, pressUnit)) {
                throw ValidationException.invalidValue("avg pressure", press.avg);
            }

            return new ReportStats(units, temp, hum, press);
        }
    }
}
