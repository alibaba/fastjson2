package com.alibaba.fastjson2.internal.memory;

import static com.alibaba.fastjson2.util.JDKUtils.ARRAY_BYTE_BASE_OFFSET;
import static com.alibaba.fastjson2.util.JDKUtils.ARRAY_CHAR_BASE_OFFSET;
import static com.alibaba.fastjson2.util.JDKUtils.BIG_ENDIAN;
import static com.alibaba.fastjson2.util.JDKUtils.UNSAFE;

public final class ByteArrayUnsafe extends ByteArray {
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
        return UNSAFE.getByte(buf, ARRAY_BYTE_BASE_OFFSET + pos);
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
        return UNSAFE.getChar(buf, ARRAY_CHAR_BASE_OFFSET + ((long) pos << 1));
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
        return UNSAFE.getChar(buf, ARRAY_BYTE_BASE_OFFSET + ((long) pos << 1));
    }

    public void putByte(byte[] buf, int pos, byte v) {
        UNSAFE.putByte(buf, ARRAY_BYTE_BASE_OFFSET + pos, v);
    }

    public void putChar(char[] buf, int pos, char v) {
        UNSAFE.putChar(buf, ARRAY_CHAR_BASE_OFFSET + ((long) pos << 1), v);
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
        UNSAFE.putShort(buf, ARRAY_BYTE_BASE_OFFSET + pos, convEndian(true, v));
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
        UNSAFE.putShort(buf, ARRAY_BYTE_BASE_OFFSET + pos, convEndian(false, v));
    }

    static short convEndian(boolean big, short n) {
        return big == BIG_ENDIAN ? n : Short.reverseBytes(n);
    }

    public short getShortUnaligned(byte[] buf, int offset) {
        return UNSAFE.getShort(buf, ARRAY_BYTE_BASE_OFFSET + offset);
    }

    public void putShortUnaligned(byte[] buf, int pos, short v) {
        UNSAFE.putShort(buf, ARRAY_BYTE_BASE_OFFSET + pos, v);
    }

    public short getShortBE(byte[] buf, int offset) {
        return convEndian(true,
                UNSAFE.getShort(buf, ARRAY_BYTE_BASE_OFFSET + offset));
    }

    public short getShortLE(byte[] buf, int offset) {
        return convEndian(false,
                UNSAFE.getShort(buf, ARRAY_BYTE_BASE_OFFSET + offset));
    }

    public int getIntUnaligned(byte[] buf, int offset) {
        return UNSAFE.getInt(buf, ARRAY_BYTE_BASE_OFFSET + offset);
    }

    static int convEndian(boolean big, int n) {
        return big == BIG_ENDIAN ? n : Integer.reverseBytes(n);
    }

    static long convEndian(boolean big, long n) {
        return big == BIG_ENDIAN ? n : Long.reverseBytes(n);
    }

    public int getIntBE(byte[] buf, int offset) {
        return convEndian(true, UNSAFE.getInt(buf, ARRAY_BYTE_BASE_OFFSET + offset));
    }

    public int getIntLE(byte[] buf, int offset) {
        return convEndian(false, UNSAFE.getInt(buf, ARRAY_BYTE_BASE_OFFSET + offset));
    }

    public void putIntUnaligned(byte[] buf, int pos, int v) {
        UNSAFE.putInt(buf, ARRAY_BYTE_BASE_OFFSET + pos, v);
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
        UNSAFE.putInt(buf, ARRAY_BYTE_BASE_OFFSET + pos, convEndian(true, v));
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
        UNSAFE.putInt(buf, ARRAY_BYTE_BASE_OFFSET + pos, convEndian(false, v));
    }

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

    public int getIntUnaligned(char[] buf, int offset) {
        return UNSAFE.getInt(buf, ARRAY_CHAR_BASE_OFFSET + ((long) offset << 1));
    }

    public int getIntBE(char[] buf, int offset) {
        return convEndian(true, UNSAFE.getInt(buf, ARRAY_CHAR_BASE_OFFSET + ((long) offset << 1)));
    }

    public int getIntLE(char[] buf, int offset) {
        return convEndian(false, UNSAFE.getInt(buf, ARRAY_CHAR_BASE_OFFSET + ((long) offset << 1)));
    }

    public void putIntUnaligned(char[] buf, int pos, int v) {
        UNSAFE.putInt(buf, ARRAY_CHAR_BASE_OFFSET + ((long) pos << 1), v);
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
        UNSAFE.putInt(buf, ARRAY_CHAR_BASE_OFFSET + ((long) pos << 1), convEndian(true, v));
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
        UNSAFE.putInt(buf, ARRAY_CHAR_BASE_OFFSET + ((long) pos << 1), convEndian(false, v));
    }

    public long getLongUnaligned(byte[] buf, int offset) {
        return UNSAFE.getLong(buf, ARRAY_BYTE_BASE_OFFSET + offset);
    }

    public long getLongBE(byte[] buf, int offset) {
        return convEndian(true, UNSAFE.getLong(buf, ARRAY_BYTE_BASE_OFFSET + offset));
    }

    public long getLongLE(byte[] buf, int offset) {
        return convEndian(false, UNSAFE.getLong(buf, ARRAY_BYTE_BASE_OFFSET + offset));
    }

    public void putLongUnaligned(byte[] buf, int pos, long v) {
        UNSAFE.putLong(buf, ARRAY_BYTE_BASE_OFFSET + pos, v);
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
        UNSAFE.putLong(buf, ARRAY_BYTE_BASE_OFFSET + pos, convEndian(true, v));
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
        UNSAFE.putLong(buf, ARRAY_BYTE_BASE_OFFSET + pos, convEndian(false, v));
    }

    public long getLongUnaligned(char[] buf, int offset) {
        return UNSAFE.getLong(buf, ARRAY_CHAR_BASE_OFFSET + ((long) offset << 1));
    }

    public long getLongBE(char[] buf, int offset) {
        return convEndian(true, UNSAFE.getLong(buf, ARRAY_CHAR_BASE_OFFSET + ((long) offset << 1)));
    }

    public long getLongLE(char[] buf, int offset) {
        return convEndian(false, UNSAFE.getLong(buf, ARRAY_CHAR_BASE_OFFSET + ((long) offset << 1)));
    }

    public void putLongUnaligned(char[] buf, int pos, long v) {
        UNSAFE.putLong(buf, ARRAY_CHAR_BASE_OFFSET + ((long) pos << 1), v);
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
        UNSAFE.putLong(buf, ARRAY_CHAR_BASE_OFFSET + ((long) pos << 1), convEndian(true, v));
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
        UNSAFE.putLong(buf, ARRAY_CHAR_BASE_OFFSET + ((long) pos << 1), convEndian(false, v));
    }
}
