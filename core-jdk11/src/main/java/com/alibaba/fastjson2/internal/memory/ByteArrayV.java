package com.alibaba.fastjson2.internal.memory;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;

public final class ByteArrayV extends ByteArray {
    private static final boolean BIG_ENDIAN = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;
    private static final VarHandle
            CHAR_BE = create(char[].class, true),
            CHAR_LE = create(char[].class, false),
            CHAR = BIG_ENDIAN ? CHAR_BE : CHAR_LE,
            SHORT_BE = create(short[].class, true),
            SHORT_LE = create(short[].class, false),
            SHORT = BIG_ENDIAN ? SHORT_BE : SHORT_LE,
            INT_BE = create(int[].class, true),
            INT_LE = create(int[].class, false),
            INT = BIG_ENDIAN ? INT_BE : INT_LE,
            LONG_BE = create(long[].class, true),
            LONG_LE = create(long[].class, false),
            LONG = BIG_ENDIAN ? LONG_BE : LONG_LE;

    @Override
    public void putShortBE(byte[] buf, int pos, short v) {
        SHORT_BE.set(buf, pos, v);
    }

    @Override
    public void putShortLE(byte[] buf, int pos, short v) {
        SHORT_LE.set(buf, pos, v);
    }

    @Override
    public char getChar(byte[] buf, int pos) {
        return (char) CHAR.get(buf, pos << 1);
    }

    @Override
    public void putShortUnaligned(byte[] buf, int pos, short v) {
        SHORT.set(buf, pos, v);
    }

    @Override
    public void putIntBE(byte[] buf, int pos, int v) {
        INT_BE.set(buf, pos, v);
    }

    @Override
    public void putIntLE(byte[] buf, int pos, int v) {
        INT_LE.set(buf, pos, v);
    }

    @Override
    public void putIntUnaligned(byte[] buf, int pos, int v) {
        INT.set(buf, pos, v);
    }

    @Override
    public void putLongBE(byte[] buf, int pos, long v) {
        LONG_BE.set(buf, pos, v);
    }

    @Override
    public void putLongLE(byte[] buf, int pos, long v) {
        LONG_LE.set(buf, pos, v);
    }

    @Override
    public void putLongUnaligned(byte[] buf, int pos, long v) {
        LONG.set(buf, pos, v);
    }

    @Override
    public long getLongBE(byte[] buf, int offset) {
        return (long) LONG_BE.get(buf, offset);
    }

    @Override
    public long getLongLE(byte[] buf, int offset) {
        return (long) LONG_LE.get(buf, offset);
    }

    @Override
    public long getLongUnaligned(byte[] buf, int offset) {
        return (long) LONG.get(buf, offset);
    }

    private static VarHandle create(Class<?> viewArrayClass, boolean bigEndian) {
        return MethodHandles.byteArrayViewVarHandle(viewArrayClass, bigEndian ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
    }
}
