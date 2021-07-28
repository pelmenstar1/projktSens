package com.pelmenstar.projktSens.shared.serialization;

import com.pelmenstar.projktSens.shared.Bytes;

import org.jetbrains.annotations.NotNull;

/**
 * Responsible for writing primitive value to byte buffer
 */
public final class ValueWriter {
    private final byte @NotNull [] data;
    private int position;

    /**
     * Initializes instance of {@link ValueWriter} using specified byte array
     *
     * @param data byte array for reading values
     */
    public ValueWriter(byte @NotNull [] data) {
        this.data = data;
    }

    /**
     * Initializes instance of {@link ValueWriter} using byte array and offset
     *
     * @param data byte array for reading values
     * @param offset primary position of cursor
     *
     * @throws IndexOutOfBoundsException if offset is less than 0 or greater than size of data
     */
    public ValueWriter(byte @NotNull [] data, int offset) {
        if(offset < 0 || offset > data.length) {
            throw new IndexOutOfBoundsException("offset");
        }

        this.data = data;
        this.position = offset;
    }

    /**
     * Determines whether position of cursor is in end of the buffer
     */
    public boolean inEnd() {
        return position == data.length;
    }

    /**
     * Gets position of cursor
     */
    public int position() {
        return position;
    }

    /**
     * Returns size of the buffer
     */
    public int size() {
        return data.length;
    }

    /**
     * Writes specified value and moves cursor for 1 byte
     *
     * @throws IndexOutOfBoundsException if cursor is in the end of buffer
     */
    public void emitInt8(byte value) {
        data[position++] = value;
    }

    /**
     * Writes specified value and moves cursor for 2 byte
     *
     * @throws IndexOutOfBoundsException if cursor is in {@code size() - 1} position or further
     */
    public void emitInt16(short value) {
        Bytes.writeShort(value, data, position);
        position += 2;
    }

    /**
     * Writes specified value and moves cursor for 4 byte
     *
     * @throws IndexOutOfBoundsException if cursor is in {@code size() - 3} position or further
     */
    public void emitInt32(int value) {
        Bytes.writeInt(value, data, position);
        position += 4;
    }

    /**
     * Writes specified value and moves cursor for 4 byte
     *
     * @throws IndexOutOfBoundsException if cursor is in {@code size() - 3} position or further
     */
    public void emitFloat(float value) {
        emitInt32(Float.floatToIntBits(value));
    }

    /**
     * Writes specified value and moves cursor for 8 byte
     *
     * @throws IndexOutOfBoundsException if cursor is in {@code size() - 7} position or further
     */
    public void emitInt64(long value) {
        Bytes.writeLong(value, data, position);
        position += 8;
    }

    public void emitInt24(int value) {
        Bytes.writeInt24(value, data, position);
        position += 3;
    }

    public void emitInt40(long value) {
        Bytes.writeInt40(value, data, position);
        position += 5;
    }

    /**
     * Writes specified array and moves cursor for given array valuesLength
     *
     *  @throws IndexOutOfBoundsException if cursor is in {@code size() - array.valuesLength - 1} position or further
     */
    public void emitByteArray(byte @NotNull [] array) {
        System.arraycopy(array, 0, data, position, array.length);
        position += array.length;
    }
}
