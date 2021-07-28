package com.pelmenstar.projktSens.shared;

import org.jetbrains.annotations.NotNull;

/**
 * A helper to class to convert primitive types to bytes. Byte order: Little Endian
 */
public final class Bytes {
    private Bytes() {}

    /**
     * Writes int64 to buffer at specified offset
     */
    public static void writeLong(long value, byte @NotNull [] buffer, int offset) {
        buffer[offset] = (byte)value;
        buffer[offset + 1] = (byte)(value >> 8);
        buffer[offset + 2] = (byte)(value >> 16);
        buffer[offset + 3] = (byte)(value >> 24);
        buffer[offset + 4] = (byte)(value >> 32);
        buffer[offset + 5] = (byte)(value >> 40);
        buffer[offset + 6] = (byte)(value >> 48);
        buffer[offset + 7] = (byte)(value >> 56);
    }

    /**
     * Writes int32 to buffer at specified offset
     */
    public static void writeInt(int value, byte @NotNull [] buffer, int offset) {
        buffer[offset] = (byte)value;
        buffer[offset + 1] = (byte)(value >> 8);
        buffer[offset + 2] = (byte)(value >> 16);
        buffer[offset + 3] = (byte)(value >> 24);
    }

    public static void writeInt24(int value, byte @NotNull [] buffer, int offset) {
        buffer[offset] = (byte)value;
        buffer[offset + 1] = (byte)(value >> 8);
        buffer[offset + 2] = (byte)(value >> 16);
    }

    public static void writeInt40(long value, byte @NotNull [] buffer, int offset) {
        buffer[offset] = (byte)value;
        buffer[offset + 1] = (byte)(value >> 8);
        buffer[offset + 2] = (byte)(value >> 16);
        buffer[offset + 3] = (byte)(value >> 24);
        buffer[offset + 4] = (byte)(value >> 32);
    }

    /**
     * Writes float32 to buffer at specified offset
     */
    public static void writeFloat(float value, byte  @NotNull [] buffer, int offset) {
        writeInt(Float.floatToIntBits(value), buffer, offset);
    }

    /**
     * Writes int16 to buffer at specified offset
     */
    public static void writeShort(short value, byte @NotNull [] buffer, int offset) {
        buffer[offset] = (byte)value;
        buffer[offset + 1] = (byte)(value >> 8);
    }

    /**
     * Returns int64 value from buffer at specified offset
     */
    public static long readLong(byte @NotNull [] buffer, int offset) {
        return ((buffer[offset] & 0xffL)) |
                ((buffer[offset + 1] & 0xffL) << 8 ) |
                ((buffer[offset + 2] & 0xffL) << 16) |
                ((buffer[offset + 3] & 0xffL) << 24) |
                ((buffer[offset + 4] & 0xffL) << 32) |
                ((buffer[offset + 5] & 0xffL) << 40) |
                ((buffer[offset + 6] & 0xffL) << 48) |
                ((buffer[offset + 7] & 0xffL) << 56);
    }

    public static long readInt40(byte @NotNull [] buffer, int offset) {
        return ((buffer[offset] & 0xffL)) |
                ((buffer[offset + 1] & 0xffL) << 8 ) |
                ((buffer[offset + 2] & 0xffL) << 16) |
                ((buffer[offset + 3] & 0xffL) << 24) |
                ((buffer[offset + 4] & 0xffL) << 32);
    }

    /**
     * Returns int32 value from buffer at specified offset
     */
    public static int readInt(byte @NotNull [] buffer, int offset) {
        return (buffer[offset] & 0xFF) |
                ((buffer[offset + 1] & 0xFF) << 8 ) |
                ((buffer[offset + 2] & 0xFF) << 16) |
                ((buffer[offset + 3] & 0xFF) << 24);
    }

    public static int readInt24(byte @NotNull [] buffer, int offset) {
        return (buffer[offset] & 0xFF) |
                ((buffer[offset + 1] & 0xFF) << 8 ) |
                ((buffer[offset + 2] & 0xFF) << 16);
    }

    /**
     * Returns float32 value from buffer at specified offset
     */
    public static float readFloat(byte @NotNull [] buffer, int offset) {
        return Float.intBitsToFloat(readInt(buffer, offset));
    }

    /**
     * Returns int16 value from buffer at specified offset
     */
    public static short readShort(byte @NotNull [] buffer, int offset) {
        return (short)((buffer[offset] & 0xFF) | ((buffer[offset + 1] & 0xFF) << 8));
    }
}
