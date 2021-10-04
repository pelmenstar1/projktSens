package com.pelmenstar.projktSens.shared.serialization;

import com.pelmenstar.projktSens.shared.Bytes;

import org.jetbrains.annotations.NotNull;

/**
 * Responsible for reading primitive values from byte buffer. Values in read in little-endian
 */
public final class ValueReader {
    private final byte @NotNull [] data;
    private int position;

    public ValueReader(byte @NotNull [] data) {
        this.data = data;
    }

    public ValueReader(byte @NotNull [] data, int offset) {
        if (offset < 0 || offset > data.length) {
            throw new IndexOutOfBoundsException("offset");
        }

        this.data = data;
        position = offset;
    }

    public final boolean inEnd() {
        return position() == size();
    }

    public int position() {
        return position;
    }

    public int size() {
        return data.length;
    }

    public byte readInt8() {
        return data[position++];
    }

    public short readInt16() {
        short s = Bytes.readShort(data, position);
        position += 2;

        return s;
    }

    public int readInt32() {
        int i = Bytes.readInt(data, position);
        position += 4;

        return i;
    }

    public float readFloat() {
        return Float.intBitsToFloat(readInt32());
    }

    public long readInt64() {
        long l = Bytes.readLong(data, position);
        position += 8;

        return l;
    }

    public int readInt24() {
        int i = Bytes.readInt24(data, position);
        position += 3;


        return i;
    }

    public long readInt40() {
        long l = Bytes.readInt40(data, position);
        position += 5;

        return l;
    }

    public byte @NotNull [] readByteArray(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("size=" + size);
        }

        byte[] array = new byte[size];
        System.arraycopy(data, position, array, 0, size);
        position += size;

        return array;
    }
}
