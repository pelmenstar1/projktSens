package com.pelmenstar.projktSens.shared.serialization;

import com.pelmenstar.projktSens.shared.Bytes;

import org.jetbrains.annotations.NotNull;

/**
 * Responsible for writing primitive value to byte buffer
 */
public final class ValueWriter {
    @NotNull
    private final byte[] data;
    private int position;

    public ValueWriter(@NotNull byte[] data) {
        this.data = data;
    }

    public ValueWriter(@NotNull byte[] data, int offset) {
        if(offset < 0 || offset > data.length) {
            throw new IndexOutOfBoundsException("offset");
        }

        this.data = data;
        this.position = offset;
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

    /**
     * Writes specified array and moves cursor for given array valuesLength
     *
     *  @throws IndexOutOfBoundsException if cursor is in {@code size() - array.valuesLength - 1} position or further
     */
    public void emitByteArray(byte[] array) {
        System.arraycopy(array, 0, data, position, array.length);
        position += array.length;
    }
}
