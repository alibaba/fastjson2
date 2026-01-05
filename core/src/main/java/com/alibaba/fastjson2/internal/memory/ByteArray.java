package com.alibaba.fastjson2.internal.memory;

import java.nio.ByteOrder;

public class ByteArray {
    private static final boolean BIG_ENDIAN = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;

    /**
     * Gets a byte value from a byte array at the specified position.
     * This method retrieves a byte value from the specified byte array at the given position
     * using unsafe memory operations for improved performance.
     *
     * @param buf the byte array buffer to read from
     * @param pos the position in the buffer where to read the byte value
     * @return the byte value at the specified position
     */
    public byte getByte(byte[] buf, int pos) {
        return buf[pos];
    }

    /**
     * Gets a character value from a character array at the specified position.
     * This method retrieves a character value from the specified character array at the given position
     * using unsafe memory operations for improved performance.
     *
     * @param buf the character array buffer to read from
     * @param pos the position in the buffer where to read the character value
     * @return the character value at the specified position
     */
    public char getChar(char[] buf, int pos) {
        return buf[pos];
    }

    /**
     * Gets a character value from a byte array at the specified position.
     * This method retrieves a character value from the specified byte array at the given position
     * using unsafe memory operations for improved performance.
     *
     * @param buf the byte array buffer to read from
     * @param pos the position in the buffer where to read the character value
     * @return the character value at the specified position
     */
    public char getChar(byte[] buf, int pos) {
        return (char) ((buf[pos << 1] & 0xFF) | (buf[(pos << 1) + 1] & 0xFF) << 8);
    }

    public void putByte(byte[] buf, int pos, byte v) {
        buf[pos] = v;
    }

    public void putChar(char[] buf, int pos, char v) {
        buf[pos] = v;
    }

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

    public int getIntUnaligned(byte[] buf, int offset) {
        return BIG_ENDIAN ? getIntBE(buf, offset) : getIntLE(buf, offset);
    }

    public int getIntBE(byte[] buf, int offset) {
        int b1 = (buf[offset] & 0xFF);
        int b2 = (buf[offset + 1] & 0xFF);
        int b3 = (buf[offset + 2] & 0xFF);
        int b4 = (buf[offset + 3] & 0xFF);
        return (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;
    }

    public int getIntLE(byte[] buf, int offset) {
        int b1 = (buf[offset] & 0xFF);
        int b2 = (buf[offset + 1] & 0xFF);
        int b3 = (buf[offset + 2] & 0xFF);
        int b4 = (buf[offset + 3] & 0xFF);
        return b1 | (b2 << 8) | (b3 << 16) | (b4 << 24);
    }

    public void putIntUnaligned(byte[] buf, int pos, int v) {
        if (BIG_ENDIAN) {
            putIntBE(buf, pos, v);
        } else {
            putIntLE(buf, pos, v);
        }
    }

    /**
     * Writes an int value to a byte array in big-endian byte order.
     * This method puts an int value into the specified byte array at the given position
     * using big-endian byte ordering (most significant byte first).
     *
     * @param buf the byte array buffer to write to
     * @param pos the position in the buffer where to write the int value
     * @param v the int value to write
     */
    public void putIntBE(byte[] buf, int pos, int v) {
        buf[pos] = (byte) ((v >>> 24) & 0xFF);
        buf[pos + 1] = (byte) ((v >>> 16) & 0xFF);
        buf[pos + 2] = (byte) ((v >>> 8) & 0xFF);
        buf[pos + 3] = (byte) (v & 0xFF);
    }

    /**
     * Writes an int value to a byte array in little-endian byte order.
     * This method puts an int value into the specified byte array at the given position
     * using little-endian byte ordering (least significant byte first).
     *
     * @param buf the byte array buffer to write to
     * @param pos the position in the buffer where to write the int value
     * @param v the int value to write
     */
    public void putIntLE(byte[] buf, int pos, int v) {
        buf[pos] = (byte) (v & 0xFF);
        buf[pos + 1] = (byte) ((v >>> 8) & 0xFF);
        buf[pos + 2] = (byte) ((v >>> 16) & 0xFF);
        buf[pos + 3] = (byte) ((v >>> 24) & 0xFF);
    }

    public int getIntUnaligned(char[] buf, int offset) {
        return BIG_ENDIAN ? getIntBE(buf, offset) : getIntLE(buf, offset);
    }

    public int getIntBE(char[] buf, int offset) {
        return (((buf[offset] & 0xFFFF)) << 16) | (buf[offset + 1] & 0xFFFF);
    }

    /**
     * Gets an int value from a character array at the specified offset in little-endian byte order.
     * This method retrieves an int value from the specified character array at the given offset
     * using little-endian byte ordering (least significant byte first).
     *
     * @param buf the character array to read from
     * @param offset the offset in the array where to read the int value
     * @return the int value at the specified offset in little-endian order
     */
    public int getIntLE(char[] buf, int offset) {
        return ((buf[offset + 1] & 0xFFFF) << 16) | (buf[offset] & 0xFFFF);
    }

    public void putIntUnaligned(char[] buf, int pos, int v) {
        if (BIG_ENDIAN) {
            putIntBE(buf, pos, v);
        } else {
            putIntLE(buf, pos, v);
        }
    }

    /**
     * Writes an int value to a character array in big-endian byte order.
     * This method puts an int value into the specified character array at the given position
     * using big-endian byte ordering (most significant byte first).
     *
     * @param buf the character array buffer to write to
     * @param pos the position in the buffer where to write the int value
     * @param v the int value to write
     */
    public void putIntBE(char[] buf, int pos, int v) {
        buf[pos] = (char) ((v >>> 16) & 0xFFFF);
        buf[pos + 1] = (char) (v & 0xFFFF);
    }

    /**
     * Writes an int value to a character array in little-endian byte order.
     * This method puts an int value into the specified character array at the given position
     * using little-endian byte ordering (least significant byte first).
     *
     * @param buf the character array buffer to write to
     * @param pos the position in the buffer where to write the int value
     * @param v the int value to write
     */
    public void putIntLE(char[] buf, int pos, int v) {
        buf[pos] = (char) (v & 0xFFFF);
        buf[pos + 1] = (char) ((v >>> 16) & 0xFFFF);
    }

    public long getLongUnaligned(byte[] buf, int offset) {
        return BIG_ENDIAN ? getLongBE(buf, offset) : getLongLE(buf, offset);
    }

    public long getLongBE(byte[] buf, int offset) {
        long b1 = (buf[offset] & 0xFFL);
        long b2 = (buf[offset + 1] & 0xFFL);
        long b3 = (buf[offset + 2] & 0xFFL);
        long b4 = (buf[offset + 3] & 0xFFL);
        long b5 = (buf[offset + 4] & 0xFFL);
        long b6 = (buf[offset + 5] & 0xFFL);
        long b7 = (buf[offset + 6] & 0xFFL);
        long b8 = (buf[offset + 7] & 0xFFL);
        return (b1 << 56) | (b2 << 48) | (b3 << 40) | (b4 << 32) | (b5 << 24) | (b6 << 16) | (b7 << 8) | b8;
    }

    public long getLongLE(byte[] buf, int offset) {
        long b1 = (buf[offset] & 0xFFL);
        long b2 = (buf[offset + 1] & 0xFFL);
        long b3 = (buf[offset + 2] & 0xFFL);
        long b4 = (buf[offset + 3] & 0xFFL);
        long b5 = (buf[offset + 4] & 0xFFL);
        long b6 = (buf[offset + 5] & 0xFFL);
        long b7 = (buf[offset + 6] & 0xFFL);
        long b8 = (buf[offset + 7] & 0xFFL);
        return b1 | (b2 << 8) | (b3 << 16) | (b4 << 24) | (b5 << 32) | (b6 << 40) | (b7 << 48) | (b8 << 56);
    }

    public void putLongUnaligned(byte[] buf, int pos, long v) {
        if (BIG_ENDIAN) {
            putLongBE(buf, pos, v);
        } else {
            putLongLE(buf, pos, v);
        }
    }

    /**
     * Writes a long value to a byte array in big-endian byte order.
     * This method puts a long value into the specified byte array at the given position
     * using big-endian byte ordering (most significant byte first).
     *
     * @param buf the byte array buffer to write to
     * @param pos the position in the buffer where to write the long value
     * @param v the long value to write
     */
    public void putLongBE(byte[] buf, int pos, long v) {
        buf[pos] = (byte) ((v >>> 56) & 0xFF);
        buf[pos + 1] = (byte) ((v >>> 48) & 0xFF);
        buf[pos + 2] = (byte) ((v >>> 40) & 0xFF);
        buf[pos + 3] = (byte) ((v >>> 32) & 0xFF);
        buf[pos + 4] = (byte) ((v >>> 24) & 0xFF);
        buf[pos + 5] = (byte) ((v >>> 16) & 0xFF);
        buf[pos + 6] = (byte) ((v >>> 8) & 0xFF);
        buf[pos + 7] = (byte) (v & 0xFF);
    }

    /**
     * Writes a long value to a byte array in little-endian byte order.
     * This method puts a long value into the specified byte array at the given position
     * using little-endian byte ordering (least significant byte first).
     *
     * @param buf the byte array buffer to write to
     * @param pos the position in the buffer where to write the long value
     * @param v the long value to write
     */
    public void putLongLE(byte[] buf, int pos, long v) {
        buf[pos] = (byte) (v & 0xFF);
        buf[pos + 1] = (byte) ((v >>> 8) & 0xFF);
        buf[pos + 2] = (byte) ((v >>> 16) & 0xFF);
        buf[pos + 3] = (byte) ((v >>> 24) & 0xFF);
        buf[pos + 4] = (byte) ((v >>> 32) & 0xFF);
        buf[pos + 5] = (byte) ((v >>> 40) & 0xFF);
        buf[pos + 6] = (byte) ((v >>> 48) & 0xFF);
        buf[pos + 7] = (byte) ((v >>> 56) & 0xFF);
    }

    public long getLongUnaligned(char[] buf, int offset) {
        return BIG_ENDIAN ? getLongBE(buf, offset) : getLongLE(buf, offset);
    }

    public long getLongBE(char[] buf, int offset) {
        return (((long) (buf[offset] & 0xFFFF)) << 48) | (((long) (buf[offset + 1] & 0xFFFF)) << 32) |
                (((long) (buf[offset + 2] & 0xFFFF)) << 16) | (buf[offset + 3] & 0xFFFF);
    }

    /**
     * Gets a long value from a character array at the specified offset in little-endian byte order.
     * This method retrieves a long value from the specified character array at the given offset
     * using little-endian byte ordering (least significant byte first).
     *
     * @param buf the character array to read from
     * @param offset the offset in the array where to read the long value
     * @return the long value at the specified offset in little-endian order
     */
    public long getLongLE(char[] buf, int offset) {
        return (((long) (buf[offset + 3] & 0xFFFF)) << 48) | (((long) (buf[offset + 2] & 0xFFFF)) << 32) |
                (((long) (buf[offset + 1] & 0xFFFF)) << 16) | (buf[offset] & 0xFFFF);
    }

    public void putLongUnaligned(char[] buf, int pos, long v) {
        if (BIG_ENDIAN) {
            putLongBE(buf, pos, v);
        } else {
            putLongLE(buf, pos, v);
        }
    }

    /**
     * Writes a long value to a character array in big-endian byte order.
     * This method puts a long value into the specified character array at the given position
     * using big-endian byte ordering (most significant byte first).
     *
     * @param buf the character array buffer to write to
     * @param pos the position in the buffer where to write the long value
     * @param v the long value to write
     */
    public void putLongBE(char[] buf, int pos, long v) {
        buf[pos] = (char) ((v >>> 48) & 0xFFFF);
        buf[pos + 1] = (char) ((v >>> 32) & 0xFFFF);
        buf[pos + 2] = (char) ((v >>> 16) & 0xFFFF);
        buf[pos + 3] = (char) (v & 0xFFFF);
    }

    /**
     * Writes a long value to a character array in little-endian byte order.
     * This method puts a long value into the specified character array at the given position
     * using little-endian byte ordering (least significant byte first).
     *
     * @param buf the character array buffer to write to
     * @param pos the position in the buffer where to write the long value
     * @param v the long value to write
     */
    public void putLongLE(char[] buf, int pos, long v) {
        buf[pos] = (char) (v & 0xFFFF);
        buf[pos + 1] = (char) ((v >>> 16) & 0xFFFF);
        buf[pos + 2] = (char) ((v >>> 32) & 0xFFFF);
        buf[pos + 3] = (char) ((v >>> 48) & 0xFFFF);
    }

    static long convEndian(boolean big, long n) {
        return big == BIG_ENDIAN ? n : Long.reverseBytes(n);
    }
}
