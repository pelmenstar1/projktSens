package com.pelmenstar.projktSens.shared.serialization;

import com.pelmenstar.projktSens.shared.Bytes;

import org.jetbrains.annotations.NotNull;

/**
 * Responsible for reading primitive value from byte buffer
 */
public final class ValueReader {
    @NotNull
    private final byte[] data;
    private int position;

    public ValueReader(@NotNull byte[] data) {
        this.data = data;
    }

    public ValueReader(@NotNull byte[] data, int offset) {
        if(offset < 0 || offset > data.length) {
            throw new IndexOutOfBoundsException("offset");
        }

        this.data = data;
        position = offset;
    }

    public boolean inEnd() {
        return position == data.length;
    }

    public int position() {
        return position;
    }

    public int size() {
        return data.length;
    }

    /**
     * Reads byte from the buffer and moves cursor for 1 byte
     *
     * @throws IndexOutOfBoundsException if there are lack of data
     */
    public byte readInt8() {
        return data[position++];
    }

    public short readInt16() {
        short s = Bytes.readShort(data, position);
        position += 2;

        return s;
    }

    /**
     * Reads int from the buffer and moves cursor for 4 bytes
     *
     * @throws IndexOutOfBoundsException if there are lack of data
     */
    public int readInt32() {
        int i = Bytes.readInt(data, position);
        position += 4;

        return i;
    }

    /**
     * Reads float from the buffer and moves cursor for 4 bytes
     *
     * @throws IndexOutOfBoundsException if there are lack of data
     */
    public float readFloat() {
        return Float.intBitsToFloat(readInt32());
    }

    /**
     * Reads long from the buffer and moves cursor for 8 bytes
     *
     * @throws IndexOutOfBoundsException if there are lack of data
     */
    public long readInt64() {
        long l = Bytes.readLong(data, position);
        position += 8;

        return l;
    }

    /**
     * Reads byte array with specified from the buffer and moves cursor for given size.
     *
     * @param size size of data to read
     * @throws IndexOutOfBoundsException if there are lack of data
     */
    @NotNull
    public byte[] readByteArray(int size) {
        if(size <= 0) {
            throw new IllegalArgumentException("size=" + size);
        }

        byte[] array = new byte[size];
        System.arraycopy(data, position, array, 0, size);
        position += size;

        return array;
    }
}
