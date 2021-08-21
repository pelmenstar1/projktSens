package com.pelmenstar.projktSens.shared.serialization;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;

/**
 * Helper class that simplify work with classes that can be serialized
 */
public final class Serializable {
    private static final int REQUIRED_SERIALIZER_MODS = Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL;

    private static final HashMap<Class<?>, ObjectSerializer<?>> cachedSerializers = new HashMap<>();

    private Serializable() {
    }

    static {
        // common serializers
        cachedSerializers.put(Integer.class, new PrimitiveSerializers.Int32());
        cachedSerializers.put(Long.class, new PrimitiveSerializers.Int64());
    }

    /**
     * Gets a serializer for specified {@link Class}.
     * Requirements for class is described in {@link ObjectSerializer}
     *
     * @throws SerializerContractException if given class doesn't meet requirements described in {@link ObjectSerializer}
     * @throws NullPointerException        if specified class is null
     */
    @NotNull
    public static <T> ObjectSerializer<T> getSerializer(@NotNull Class<T> c) {
        //noinspection unchecked
        ObjectSerializer<T> s = (ObjectSerializer<T>) cachedSerializers.get(c);

        if (s == null) {
            s = getSerializerReflection(c);

            cachedSerializers.put(c, s);
        }

        return s;
    }

    /**
     * Pre-registers serializer with associated class. It's not important, but can affect performance.
     * When {@link Serializable#getSerializer(Class)} method called for the first time and there is no serializer in cache for given class,
     * it retrieves serializer using reflection from static SERIALIZER field. Basically this method only puts serializer in cache and
     * when it is accessed through {@link Serializable#getSerializer(Class)}, last one goes fast path and will just retrieve it from cache.
     * It's recommended to call it in static constructor of {@link T} class
     *
     * @throws NullPointerException if either given class or serializer is null
     */
    public static <T> void registerSerializer(@NotNull Class<T> c, @NotNull ObjectSerializer<T> serializer) {
        if (!cachedSerializers.containsKey(c)) {
            cachedSerializers.put(c, serializer);
        }
    }

    @NotNull
    private static <T> ObjectSerializer<T> getSerializerReflection(@NotNull Class<T> c) throws SerializerContractException {
        Field serializerField;

        try {
            serializerField = c.getField("SERIALIZER");
        } catch (Exception e) {
            throw SerializerContractException.noSerializerField(c);
        }

        serializerField.setAccessible(true);

        int mods = serializerField.getModifiers();
        if ((mods & REQUIRED_SERIALIZER_MODS) != REQUIRED_SERIALIZER_MODS) {
            throw SerializerContractException.illegalModifiers(mods);
        }

        Object serializerRawValue;

        try {
            serializerRawValue = serializerField.get(null);
        } catch (Exception e) {
            throw new RuntimeException("Static constructor of" + c.toString() + " throws an exception", e);
        }

        if (serializerRawValue == null) {
            throw SerializerContractException.serializerIsNull(c);
        }

        try {
            // if serializerRawValue is not instance of ObjectSerializer, ClassCastException will be caught
            //noinspection unchecked
            return (ObjectSerializer<T>) serializerRawValue;
        } catch (ClassCastException e) {
            throw SerializerContractException.serializerDoesntExtendObjectSerializer(c);
        }
    }


    /**
     * Deserializes raw data to object of type {@link <T>}
     *
     * @param data some valid raw data
     * @param c    {@linkplain Class<T>}
     */
    @NotNull
    public static <T> T ofByteArray(byte @NotNull [] data, @NotNull Class<T> c) {
        return ofByteArray(data, getSerializer(c));
    }

    /**
     * Deserializes raw data to object of type {@link <T>}
     *
     * @param data       some valid raw data
     * @param serializer serializer that will be used to parse data
     */
    @NotNull
    public static <T> T ofByteArray(byte @NotNull [] data, @NotNull ObjectSerializer<T> serializer) {
        ValueReader reader = new ValueReader(data);

        return serializer.readObject(reader);
    }

    /**
     * Converts specified object to raw byte data
     *
     * @param obj some object
     * @param c   class of specified object
     */
    public static <T> byte @NotNull [] toByteArray(@NotNull T obj, @NotNull Class<T> c) {
        return toByteArray(obj, getSerializer(c));
    }

    /**
     * Converts specified object to raw byte data
     *
     * @param obj        some object
     * @param serializer serializer that will be used to serialize given value
     */
    public static <T> byte @NotNull [] toByteArray(@NotNull T obj, @NotNull ObjectSerializer<T> serializer) {
        byte[] buffer = new byte[serializer.getSerializedObjectSize(obj)];
        ValueWriter writer = new ValueWriter(buffer);

        serializer.writeObject(obj, writer);

        return buffer;
    }
}
