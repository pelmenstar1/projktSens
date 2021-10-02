package com.pelmenstar.projktSens.shared.serialization;

import com.pelmenstar.projktSens.shared.Bytes;

import org.jetbrains.annotations.NotNull;

final class ByteArrayValueReader extends ValueReader {
    private final byte @NotNull [] data;
    private int position;

    public ByteArrayValueReader(byte @NotNull [] data) {
        this.data = data;
    }

    public ByteArrayValueReader(byte @NotNull [] data, int offset) {
        if (offset < 0 || offset > data.length) {
            throw new IndexOutOfBoundsException("offset");
        }

        this.data = data;
        position = offset;
    }

    @Override
    public int position() {
        return position;
    }

    @Override
    public int size() {
        return data.length;
    }

    @Override
    public byte readInt8() {
        return data[position++];
    }

    @Override
    public short readInt16() {
        short s = Bytes.readShort(data, position);
        position += 2;

        return s;
    }

    @Override
    public int readInt32() {
        int i = Bytes.readInt(data, position);
        position += 4;

        return i;
    }

    @Override
    public long readInt64() {
        long l = Bytes.readLong(data, position);
        position += 8;

        return l;
    }

    @Override
    public int readInt24() {
        int i = Bytes.readInt24(data, position);
        position += 3;


        return i;
    }

    @Override
    public long readInt40() {
        long l = Bytes.readInt40(data, position);
        position += 5;

        return l;
    }

    @Override
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
