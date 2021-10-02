package com.pelmenstar.projktSens.shared.serialization;

import com.pelmenstar.projktSens.shared.Bytes;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

final class ByteBufferValueReader extends ValueReader {
    @NotNull
    private final ByteBuffer buffer;

    private static final byte[] int40Buffer = new byte[5];

    public ByteBufferValueReader(@NotNull ByteBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public int position() {
        return buffer.position();
    }

    @Override
    public int size() {
        return buffer.capacity();
    }

    @Override
    public byte readInt8() {
        return buffer.get();
    }

    @Override
    public short readInt16() {
        return buffer.getShort();
    }

    @Override
    public int readInt32() {
        return buffer.getInt();
    }

    @Override
    public long readInt64() {
        return buffer.getLong();
    }

    @Override
    public int readInt24() {
        buffer.get(int40Buffer, 0, 3);

        return Bytes.readInt24(int40Buffer, 0);
    }

    @Override
    public long readInt40() {
        buffer.get(int40Buffer);

        return Bytes.readInt40(int40Buffer, 0);
    }

    @Override
    public byte @NotNull [] readByteArray(int size) {
        byte[] array = new byte[size];
        buffer.get(array);

        return array;
    }
}
