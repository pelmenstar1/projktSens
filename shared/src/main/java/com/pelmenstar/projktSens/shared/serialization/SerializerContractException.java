package com.pelmenstar.projktSens.shared.serialization;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Modifier;

/**
 * An exception that signals that some class, which can be serializer, does not meet requirements described in {@link ObjectSerializer}
 */
public final class SerializerContractException extends RuntimeException {
    public SerializerContractException() {
    }

    public SerializerContractException(@NotNull String msg) {
        super(msg);
    }

    public SerializerContractException(@NotNull String msg, @NotNull Exception innerException) {
        super(msg, innerException);
    }

    @NotNull
    public static SerializerContractException serializerDoesntExtendObjectSerializer(@NotNull Class<?> c) {
        return new SerializerContractException("Field 'SERIALIZER' of class " + c.toString() + "does not extend " + ObjectSerializer.class.toString());
    }

    @NotNull
    public static SerializerContractException noSerializerField(@NotNull Class<?> c) {
        return new SerializerContractException("Class " + c.toString() + " does not have SERIALIZER field");
    }

    @NotNull
    public static SerializerContractException illegalModifiers(int mods, @NotNull Class<?> c) {
        StringBuilder sb = new StringBuilder();
        sb.append("Field 'SERIALIZER' has not illegal modifiers. Current: '");
        appendModifiers(mods, sb);
        sb.append("', but contract requires: 'public static final'");

        return new SerializerContractException(sb.toString());
    }

    private static void appendModifiers(int mods, @NotNull StringBuilder sb) {
        checkAccessFlags:
        {
            if ((mods & Modifier.PUBLIC) != 0) {
                sb.append("public ");
                break checkAccessFlags;
            }

            if ((mods & Modifier.PRIVATE) != 0) {
                sb.append("private ");
                break checkAccessFlags;
            }

            if ((mods & Modifier.PROTECTED) != 0) {
                sb.append("protected ");
            }
        }

        if((mods & Modifier.STATIC) != 0) {
            sb.append("final ");
        }

        if((mods & Modifier.FINAL) != 0) {
            sb.append("final ");
        }

        if((mods & Modifier.TRANSIENT) != 0) {
            sb.append("transient ");
        }

        if((mods & Modifier.VOLATILE) != 0) {
            sb.append("volatile ");
        }

        sb.deleteCharAt(sb.length() - 1);
    }

    @NotNull
    public static SerializerContractException serializerIsNull(@NotNull Class<?> c) {
        return new SerializerContractException("Serializer of class" + c.toString() + " is null");
    }
}
