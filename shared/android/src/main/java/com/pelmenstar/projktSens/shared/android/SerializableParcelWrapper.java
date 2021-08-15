package com.pelmenstar.projktSens.shared.android;

import android.os.Parcel;
import android.os.Parcelable;

import com.pelmenstar.projktSens.shared.serialization.ObjectSerializer;
import com.pelmenstar.projktSens.shared.serialization.Serializable;

import org.jetbrains.annotations.NotNull;

public final class SerializableParcelWrapper<T> implements Parcelable {
    @NotNull
    public final T value;
    private final ObjectSerializer<T> serializer;

    public SerializableParcelWrapper(
            @NotNull T value,
            @NotNull ObjectSerializer<T> serializer
    ) {
        this.value = value;
        this.serializer = serializer;
    }

    private SerializableParcelWrapper(@NotNull Parcel in) {
        String className = in.readString();
        Class<?> c;
        try {
            c = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        int bufferSize = in.readInt();
        byte[] buffer = new byte[bufferSize];
        in.readByteArray(buffer);

        //noinspection unchecked
        serializer = (ObjectSerializer<T>) Serializable.getSerializer(c);
        value = Serializable.ofByteArray(buffer, serializer);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NotNull Parcel dest, int flags) {
        byte[] buffer = Serializable.toByteArray(value, serializer);

        dest.writeString(value.getClass().getName());
        dest.writeInt(buffer.length);
        dest.writeByteArray(buffer);
    }

    @NotNull
    public static final Creator<SerializableParcelWrapper<?>> CREATOR = new Creator<SerializableParcelWrapper<?>>() {
        @Override
        @NotNull
        public SerializableParcelWrapper<?> createFromParcel(@NotNull Parcel in) {
            return new SerializableParcelWrapper<>(in);
        }

        @Override
        public SerializableParcelWrapper<?> @NotNull [] newArray(int size) {
            return new SerializableParcelWrapper<?>[size];
        }
    };
}
