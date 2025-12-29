package com.alibaba.fastjson2.internal;

import static com.alibaba.fastjson2.util.JDKUtils.ARRAY_BYTE_BASE_OFFSET;
import static com.alibaba.fastjson2.util.JDKUtils.ARRAY_CHAR_BASE_OFFSET;
import static com.alibaba.fastjson2.util.JDKUtils.BIG_ENDIAN;
import static com.alibaba.fastjson2.util.JDKUtils.UNSAFE;

public final class ByteArrayUnsafe extends ByteArray {
    public int digit1(byte[] buf, int off) {
        int d = UNSAFE.getByte(buf, ARRAY_BYTE_BASE_OFFSET + off) - '0';
        return d >= 0 && d <= 9 ? d : -1;
    }

    public int digit1(char[] buf, int off) {
        int d = UNSAFE.getChar(buf, ARRAY_CHAR_BASE_OFFSET + ((long) off << 1)) - '0';
        return d >= 0 && d <= 9 ? d : -1;
    }

    public int digit2(byte[] buf, int off) {
        short x = UNSAFE.getShort(buf, ARRAY_BYTE_BASE_OFFSET + off);
        if (BIG_ENDIAN) {
            x = Short.reverseBytes(x);
        }
        int d;
        if ((((x & 0xF0F0) - 0x3030) | (((d = x & 0x0F0F) + 0x0606) & 0xF0F0)) != 0) {
            return -1;
        }
        return (d & 0xF) * 10 + (d >> 8);
    }

    public int digit2(char[] buf, int off) {
        int x = UNSAFE.getInt(buf, ARRAY_CHAR_BASE_OFFSET + ((long) off << 1));
        if (BIG_ENDIAN) {
            x = Integer.reverseBytes(x);
        }
        int d;
        if ((((x & 0xFFF0FFF0) - 0x300030) | (((d = x & 0x0F000F) + 0x060006) & 0xF000F0)) != 0) {
            return -1;
        }
        return (d & 0xF) * 10 + (d >> 16);
    }

    @Override
    public int digit3(byte[] buf, int off) {
        int x = UNSAFE.getShort(buf, ARRAY_BYTE_BASE_OFFSET + off) | (UNSAFE.getByte(buf, ARRAY_BYTE_BASE_OFFSET + off + 2) << 16);
        if (BIG_ENDIAN) {
            x = Integer.reverseBytes(x);
        }
        int d;
        if ((((x & 0xF0F0F0) - 0x303030) | (((d = x & 0x0F0F0F) + 0x060606) & 0xF0F0F0)) != 0) {
            return -1;
        }
        return ((d & 0xF) * 10 + ((d >> 8) & 0xF)) * 10 + (d >> 16);
    }

    @Override
    public int digit3(char[] buf, int off) {
        long x = UNSAFE.getInt(buf, ARRAY_CHAR_BASE_OFFSET + ((long) off << 1)) + (((long) UNSAFE.getChar(buf, ARRAY_CHAR_BASE_OFFSET + ((long) (off + 2) << 1))) << 32);
        if (BIG_ENDIAN) {
            x = Long.reverseBytes(x);
        }
        long d;
        if ((((x & 0xFFF0FFF0FFF0L) - 0x3000300030L) | (((d = x & 0x0F000F000FL) + 0x0600060006L) & 0xF000F000F0L)) != 0) {
            return -1;
        }
        return (int) (((d & 0xF) * 10 + ((d >> 16) & 0xF)) * 10 + (d >> 32));
    }

    @Override
    public int digit4(byte[] buf, int off) {
        int x = UNSAFE.getInt(buf, ARRAY_BYTE_BASE_OFFSET + off);
        if (BIG_ENDIAN) {
            x = Integer.reverseBytes(x);
        }
        int d;
        if ((((x & 0xF0F0F0F0) - 0x30303030) | (((d = x & 0x0F0F0F0F) + 0x06060606) & 0xF0F0F0F0)) != 0) {
            return -1;
        }
        return (((d & 0xF) * 10 +
                ((d >> 8) & 0xF)) * 10 +
                ((d >> 16) & 0xF)) * 10 +
                (d >> 24);
    }

    @Override
    public int digit4(char[] buf, int off) {
        long x = UNSAFE.getLong(buf, ARRAY_CHAR_BASE_OFFSET + ((long) off << 1));
        if (BIG_ENDIAN) {
            x = Long.reverseBytes(x);
        }
        long d;
        if ((((x & 0xFFF0FFF0FFF0FFF0L) - 0x30003000300030L) | (((d = x & 0x0F000F000F000FL) + 0x06000600060006L) & 0xF000F000F000F0L)) != 0) {
            return -1;
        }
        return (int) ((
                ((d & 0xF) * 10 +
                ((d >> 16) & 0xF)) * 10 +
                ((d >> 32) & 0xF)) * 10 +
                (d >> 48));
    }
}
