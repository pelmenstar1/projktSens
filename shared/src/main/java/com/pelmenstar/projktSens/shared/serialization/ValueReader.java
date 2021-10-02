package com.pelmenstar.projktSens.shared.serialization;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

/**
 * Responsible for reading primitive values from byte buffer. Values in read in little-endian
 */
public abstract class ValueReader {
    @NotNull
    public static ValueReader ofByteArray(byte @NotNull [] buffer) {
        return ofByteArray(buffer, 0);
    }

    @NotNull
    public static ValueReader ofByteArray(byte @NotNull [] buffer, int position) {
        return new ByteArrayValueReader(buffer, position);
    }

    @NotNull
    public static ValueReader ofByteBuffer(@NotNull ByteBuffer buffer) {
        return new ByteBufferValueReader(buffer);
    }

    /**
     * Determines whether cursor is in end of the buffer
     */
    public final boolean inEnd() {
        return position() == size();
    }

    /**
     * Returns current position of cursor
     */
    public abstract int position();

    /**
     * Returns size of internal byte array
     */
    public abstract int size();

    /**
     * Reads byte from the buffer and moves cursor for 1 byte
     *
     * @throws IndexOutOfBoundsException if there is lack of data
     */
    public abstract byte readInt8();

    /**
     * Reads short from the internal buffer and moves cursor for 2 bytes
     *
     * @throws IndexOutOfBoundsException if there is lack of data
     */
    public abstract short readInt16();

    /**
     * Reads int from the buffer and moves cursor for 4 bytes
     *
     * @throws IndexOutOfBoundsException if there is lack of data
     */
    public abstract int readInt32();

    /**
     * Reads float from the buffer and moves cursor for 4 bytes
     *
     * @throws IndexOutOfBoundsException if there is lack of data
     */
    public final float readFloat() {
        return Float.intBitsToFloat(readInt32());
    }

    /**
     * Reads long from the buffer and moves cursor for 8 bytes
     *
     * @throws IndexOutOfBoundsException if there is lack of data
     */
    public abstract long readInt64();

    /**
     * Reads only 24 bits from the buffer.
     *
     * @throws IndexOutOfBoundsException if there is lack of data
     */
    public abstract int readInt24();

    /**
     * Reads only 40 bits from the buffer.
     *
     * @throws IndexOutOfBoundsException if there is lack of data
     */
    public abstract long readInt40();

    /**
     * Reads byte array with specified size from the internal buffer and moves cursor for size of byte array.
     *
     * @param size size of data to read
     * @throws IllegalArgumentException  if size less or equals to 0
     * @throws IndexOutOfBoundsException if there are lack of data
     */
    public abstract byte @NotNull [] readByteArray(int size);
}
