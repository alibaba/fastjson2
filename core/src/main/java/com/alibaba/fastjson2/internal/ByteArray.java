package com.alibaba.fastjson2.internal;

import java.nio.ByteOrder;

public class ByteArray {
    private static final boolean BIG_ENDIAN = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;

    public short getShortUnaligned(byte[] buf, int offset) {
        return BIG_ENDIAN ? getShortBE(buf, offset) : getShortLE(buf, offset);
    }

    public void putShortUnaligned(byte[] buf, int pos, short v) {
        if (BIG_ENDIAN) {
            putShortBE(buf, pos, v);
        } else {
            putShortLE(buf, pos, v);
        }
    }

    public short getShortBE(byte[] buf, int offset) {
        int b1 = (buf[offset] & 0xFF);
        int b2 = (buf[offset + 1] & 0xFF);
        return (short) ((b1 << 8) | b2);
    }

    public short getShortLE(byte[] buf, int offset) {
        int b1 = (buf[offset] & 0xFF);
        int b2 = (buf[offset + 1] & 0xFF);
        return (short) (b1 | (b2 << 8));
    }

    /**
     * Writes a short value to a byte array in big-endian byte order.
     * This method puts a short value into the specified byte array at the given position
     * using big-endian byte ordering (most significant byte first).
     *
     * @param buf the byte array buffer to write to
     * @param pos the position in the buffer where to write the short value
     * @param v the short value to write
     */
    public void putShortBE(byte[] buf, int pos, short v) {
        buf[pos] = (byte) ((v >>> 8) & 0xFF);
        buf[pos + 1] = (byte) (v & 0xFF);
    }

    /**
     * Writes a short value to a byte array in little-endian byte order.
     * This method puts a short value into the specified byte array at the given position
     * using little-endian byte ordering (least significant byte first).
     *
     * @param buf the byte array buffer to write to
     * @param pos the position in the buffer where to write the short value
     * @param v the short value to write
     */
    public void putShortLE(byte[] buf, int pos, short v) {
        buf[pos] = (byte) (v & 0xFF);
        buf[pos + 1] = (byte) ((v >>> 8) & 0xFF);
    }

    public int digit1(byte[] buf, int off) {
        int d = buf[off] - '0';
        return d >= 0 && d <= 9 ? d : -1;
    }

    public int digit1(char[] buf, int off) {
        int d = buf[off] - '0';
        return d >= 0 && d <= 9 ? d : -1;
    }

    public int digit2(byte[] str, int offset) {
        byte c0 = str[offset], c1 = str[offset + 1];
        if (c0 >= '0' && c0 <= '9' && c1 >= '0' && c1 <= '9') {
            return c0 * 10 + c1 - ('0' * 10 + '0');
        }
        return -1;
    }

    public int digit2(char[] str, int offset) {
        char c0 = str[offset], c1 = str[offset + 1];
        if (c0 >= '0' && c0 <= '9' && c1 >= '0' && c1 <= '9') {
            return c0 * 10 + c1 - ('0' * 10 + '0');
        }
        return -1;
    }

    public int digit3(byte[] str, int offset) {
        byte c0 = str[offset], c1 = str[offset + 1], c2 = str[offset + 2];
        if (c0 >= '0' && c0 <= '9' && c1 >= '0' && c1 <= '9' && c2 >= '0' && c2 <= '9') {
            return c0 * 100 + c1 * 10 + c2 - ('0' * 100 + '0' * 10 + '0');
        }
        return -1;
    }

    public int digit3(char[] str, int offset) {
        char c0 = str[offset], c1 = str[offset + 1], c2 = str[offset + 2];
        if (c0 >= '0' && c0 <= '9' && c1 >= '0' && c1 <= '9' && c2 >= '0' && c2 <= '9') {
            return c0 * 100 + c1 * 10 + c2 - ('0' * 100 + '0' * 10 + '0');
        }
        return -1;
    }

    public int digit4(byte[] str, int offset) {
        byte c0 = str[offset], c1 = str[offset + 1], c2 = str[offset + 2], c3 = str[offset + 3];
        if (c0 >= '0' && c0 <= '9' && c1 >= '0' && c1 <= '9' && c2 >= '0' && c2 <= '9' && c3 >= '0' && c3 <= '9') {
            return c0 * 1000 + c1 * 100 + c2 * 10 + c3 - ('0' * 1000 + '0' * 100 + '0' * 10 + '0');
        }
        return -1;
    }

    public int digit4(char[] str, int offset) {
        char c0 = str[offset], c1 = str[offset + 1], c2 = str[offset + 2], c3 = str[offset + 3];
        if (c0 >= '0' && c0 <= '9' && c1 >= '0' && c1 <= '9' && c2 >= '0' && c2 <= '9' && c3 >= '0' && c3 <= '9') {
            return c0 * 1000 + c1 * 100 + c2 * 10 + c3 - ('0' * 1000 + '0' * 100 + '0' * 10 + '0');
        }
        return -1;
    }
}
