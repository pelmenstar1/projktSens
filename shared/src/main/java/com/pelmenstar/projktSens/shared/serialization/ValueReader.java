package com.pelmenstar.projktSens.shared.serialization;

import com.pelmenstar.projktSens.shared.Bytes;

import org.jetbrains.annotations.NotNull;

/**
 * Responsible for reading primitive values from byte buffer. Values in read in little-endian
 */
public final class ValueReader {
    private final byte @NotNull [] data;
    private int position;

    /**
     * Initializes instance of {@link ValueReader} using byte array.
     * Position of cursor is set to 0
     *
     * @param data byte array from which values will be read
     */
    public ValueReader(byte @NotNull [] data) {
        this.data = data;
    }

    /**
     * Initializes instance of {@link ValueReader} using byte array and initial position of cursor
     * @param data byte array from which values will be read
     * @param offset initial position of cursor
     * @throws IndexOutOfBoundsException if offset is less than 0 or greater than length of byte array
     */
    public ValueReader(byte @NotNull [] data, int offset) {
        if(offset < 0 || offset > data.length) {
            throw new IndexOutOfBoundsException("offset");
        }

        this.data = data;
        position = offset;
    }

    /**
     * Determines whether cursor is in end of the buffer
     */
    public boolean inEnd() {
        return position == data.length;
    }

    /**
     * Returns current position of cursor
     */
    public int position() {
        return position;
    }

    /**
     * Returns size of internal byte array
     */
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

    /**
     * Reads short from the internal buffer and moves cursor for 2 bytes
     *
     * @throws IndexOutOfBoundsException if there are lack of data
     */
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
     * Reads byte array with specified size from the internal buffer and moves cursor for size of byte array.
     *
     * @param size size of data to read
     * @throws IllegalArgumentException if size less or equals to 0
     * @throws IndexOutOfBoundsException if there are lack of data
     */
    public byte @NotNull [] readByteArray(int size) {
        if(size <= 0) {
            throw new IllegalArgumentException("size=" + size);
        }

        byte[] array = new byte[size];
        System.arraycopy(data, position, array, 0, size);
        position += size;

        return array;
    }
}
