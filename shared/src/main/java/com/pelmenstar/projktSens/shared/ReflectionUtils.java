package com.pelmenstar.projktSens.shared;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

/**
 * Contains useful method related to Java reflection
 */
public final class ReflectionUtils {
    private ReflectionUtils() {}

    /**
     * Creates instance of T invoking public constructor with no parameters
     * @param name name of class
     * @param <T> result type
     */
    @SuppressWarnings("unchecked")
    @NotNull
    public static<T> T createFromEmptyConstructor(@NotNull String name) throws ClassNotFoundException {
        Class<T> c = (Class<T>)Class.forName(name);
        return createFromEmptyConstructor(c);
    }

    /**
     * Creates instance of T invoking public constructor with no parameters
     * @param name name of class
     * @param initialize determines whether class should be initialized after lookup
     * @param classLoader {@link ClassLoader} to load class from
     * @param <T> result type
     */
    @SuppressWarnings("unchecked")
    @NotNull
    public static<T> T createFromEmptyConstructor(
            @NotNull String name,
            boolean initialize,
            @NotNull ClassLoader classLoader
    ) throws ClassNotFoundException {
        Class<T> c = (Class<T>)Class.forName(name, initialize, classLoader);
        return createFromEmptyConstructor(c);
    }

    /**
     * Creates instance of T invoking public constructor with no parameters
     * @param c class of instance which will be created
     * @param <T> result type
     */
    @SuppressWarnings("unchecked")
    @NotNull
    public static<T> T createFromEmptyConstructor(@NotNull Class<T> c) {
        Constructor<?>[] constructors = c.getConstructors();

        for(Constructor<?> constructor: constructors) {
            Class<?>[] params = constructor.getParameterTypes();
            if(params.length == 0) {
                try {
                    Object instance = constructor.newInstance(EmptyArray.OBJECT);
                    return (T)instance;
                } catch (InstantiationException e) {
                    throw new RuntimeException("Constructor threw exception", e);
                } catch (IllegalAccessException e) {
                    // will never happen. we checked length of parameters
                } catch (InvocationTargetException e) {
                    throw new RuntimeException("Class " + c.getName() + " is abstract");
                }
            }
        }

        throw new RuntimeException("Class has no public constructor with no parameters");
    }

    /**
     * Tries to create instance of class invoking public constructor with no parameters.
     * If there are no such constructors, tries to take instance of class from INSTANCE field.
     * Field INSTANCE is required to be the same type with top class (argument)
     * If any of choices succeed, it throws {@link RuntimeException}.
     *
     * @param name name of class
     * @param <T> result type
     */
    @SuppressWarnings("unchecked")
    @NotNull
    public static<T> T createFromEmptyConstructorOrInstance(@NotNull String name) throws ClassNotFoundException {
        Class<T> c = (Class<T>)Class.forName(name);
        return createFromEmptyConstructorOrInstance(c);
    }

    /**
     * Tries to create instance of class invoking public constructor with no parameters.
     * If there are no such constructors, tries to take instance of class from INSTANCE field.
     * Field INSTANCE is required to be the same type with top class (argument)
     * If any of choices succeed, it throws {@link RuntimeException}.
     *
     * @param name name of class
     * @param initialize determines whether class should be initialized after lookup
     * @param classLoader {@link ClassLoader} to load class from
     * @param <T> result type
     */
    @SuppressWarnings("unchecked")
    @NotNull
    public static<T> T createFromEmptyConstructorOrInstance(
            @NotNull String name,
            boolean initialize,
            @NotNull ClassLoader classLoader
    ) throws ClassNotFoundException  {
        Class<T> c = (Class<T>)Class.forName(name, initialize, classLoader);
        return createFromEmptyConstructorOrInstance(c);
    }

    /**
     * Tries to create instance of class invoking public constructor with no parameters.
     * If there are no such constructors, tries to take instance of class from INSTANCE field.
     * Field INSTANCE is required to be the same type with top class (argument).
     * If any of choices succeed, it throws {@link RuntimeException}.
     *
     * @param c class of instance which will be returned
     * @param <T> result type
     */
    @SuppressWarnings({"unchecked"})
    @NotNull
    public static<T> T createFromEmptyConstructorOrInstance(@NotNull Class<T> c) {
        int classMods = c.getModifiers();

        // No need to find constructor and lookup INSTANCE field if class is abstract.
        // Even when abstract class has INSTANCE field,
        // INSTANCE is required to be the same type with top class.
        // So it's impossible 'cause we can't instantiate abstract class
        if ((classMods & Modifier.ABSTRACT) != 0) {
            throw new RuntimeException("Class is abstract");
        }

        // searches for only public constructors
        Constructor<?>[] constructors = c.getConstructors();

        for (Constructor<?> constructor : constructors) {
            Class<?>[] params = constructor.getParameterTypes();
            if (params.length == 0) {
                try {
                    Object instance = constructor.newInstance(EmptyArray.OBJECT);
                    return (T) instance;
                } catch (InstantiationException e) {
                    throw new RuntimeException("Constructor threw exception", e);
                } catch (IllegalAccessException e) {
                    // will never happen. we checked length of parameters
                } catch (InvocationTargetException e) {
                    // will never happen. we checked whether class is abstract
                }
            }
        }

        Field instanceField;
        try {
            instanceField = c.getField("INSTANCE");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Class has no public constructors with no parameters and no public INSTANCE field");
        }
        int fieldMods = instanceField.getModifiers();

        if ((fieldMods & Modifier.STATIC) == 0) {
            throw new RuntimeException("Field INSTANCE should be static");
        }

        T instance;
        try {
            instance = (T) instanceField.get(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (instance == null) {
            throw new NullPointerException("Field INSTANCE is null");
        }

        if (instance.getClass() != c) {
            throw new NullPointerException("Base class and class of INSTANCE differs");
        }

        return instance;
    }
}
