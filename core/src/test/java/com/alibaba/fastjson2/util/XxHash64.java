package com.alibaba.fastjson2.util;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

import static com.alibaba.fastjson2.util.JDKUtils.UNSAFE;
import static java.lang.Long.rotateLeft;
import static java.lang.Math.min;
import static sun.misc.Unsafe.ARRAY_BYTE_BASE_OFFSET;

public final class XxHash64 {
    private static final long PRIME64_1 = 0x9E3779B185EBCA87L;
    private static final long PRIME64_2 = 0xC2B2AE3D27D4EB4FL;
    private static final long PRIME64_3 = 0x165667B19E3779F9L;
    private static final long PRIME64_4 = 0x85EBCA77C2b2AE63L;
    private static final long PRIME64_5 = 0x27D4EB2F165667C5L;

    private static final long DEFAULT_SEED = 0;

    private final long seed;

    private static final long BUFFER_ADDRESS = ARRAY_BYTE_BASE_OFFSET;
    private final byte[] buffer = new byte[32];
    private int bufferSize;

    private long bodyLength;

    private long v1;
    private long v2;
    private long v3;
    private long v4;

    public XxHash64() {
        this(DEFAULT_SEED);
    }

    public XxHash64(long seed) {
        this.seed = seed;
        this.v1 = seed + PRIME64_1 + PRIME64_2;
        this.v2 = seed + PRIME64_2;
        this.v3 = seed;
        this.v4 = seed - PRIME64_1;
    }

    public XxHash64 update(byte[] data) {
        return update(data, 0, data.length);
    }

    public XxHash64 update(byte[] data, int offset, int length) {
        checkPositionIndexes(offset, offset + length, data.length);
        updateHash(data, ARRAY_BYTE_BASE_OFFSET + offset, length);
        return this;
    }

    public long hash() {
        long hash;
        if (bodyLength > 0) {
            hash = computeBody();
        } else {
            hash = seed + PRIME64_5;
        }

        hash += bodyLength + bufferSize;

        return updateTail(hash, buffer, BUFFER_ADDRESS, 0, bufferSize);
    }

    private long computeBody() {
        long hash = rotateLeft(v1, 1) + rotateLeft(v2, 7) + rotateLeft(v3, 12) + rotateLeft(v4, 18);

        hash = update(hash, v1);
        hash = update(hash, v2);
        hash = update(hash, v3);
        hash = update(hash, v4);

        return hash;
    }

    private void updateHash(Object base, long address, int length) {
        if (bufferSize > 0) {
            int available = min(32 - bufferSize, length);

            UNSAFE.copyMemory(base, address, buffer, BUFFER_ADDRESS + bufferSize, available);

            bufferSize += available;
            address += available;
            length -= available;

            if (bufferSize == 32) {
                updateBody(buffer, BUFFER_ADDRESS, bufferSize);
                bufferSize = 0;
            }
        }

        if (length >= 32) {
            int index = updateBody(base, address, length);
            address += index;
            length -= index;
        }

        if (length > 0) {
            UNSAFE.copyMemory(base, address, buffer, BUFFER_ADDRESS, length);
            bufferSize = length;
        }
    }

    private int updateBody(Object base, long address, int length) {
        int remaining = length;
        while (remaining >= 32) {
            v1 = mix(v1, UNSAFE.getLong(base, address));
            v2 = mix(v2, UNSAFE.getLong(base, address + 8));
            v3 = mix(v3, UNSAFE.getLong(base, address + 16));
            v4 = mix(v4, UNSAFE.getLong(base, address + 24));

            address += 32;
            remaining -= 32;
        }

        int index = length - remaining;
        bodyLength += index;
        return index;
    }

    public static long hash(long value) {
        long hash = DEFAULT_SEED + PRIME64_5 + SizeOf.SIZE_OF_LONG;
        hash = updateTail(hash, value);
        hash = finalShuffle(hash);

        return hash;
    }

    public static long hash(double value) {
        long longValue = (long) value;
        if (longValue != value) {
            longValue = Double.doubleToLongBits(value);
        }

        long hash = DEFAULT_SEED + PRIME64_5 + SizeOf.SIZE_OF_LONG;
        hash = updateTail(hash, longValue);
        hash = finalShuffle(hash);

        return hash;
    }

    public static long hash(Instant instant) {
        long seconds = instant.getEpochSecond();
        int nanos = instant.getNano();
        long millis;
        int nanos2;
        if (seconds < 0 && nanos > 0) {
            millis = IOUtils.multiplyExact(seconds + 1, 1000);
            long adjustment = nanos / 1000_000 - 1000;
            millis = IOUtils.addExact(millis, adjustment);
            nanos2 = nanos + (nanos / 1000) * 1000;
        } else {
            millis = IOUtils.multiplyExact(seconds, 1000);
            millis = IOUtils.addExact(millis, nanos / 1000_000);
            nanos2 = nanos - (nanos / 1000) * 1000;
        }
        return hash(millis, nanos2);
    }

    public static long hash(long v1, long v2) {
        long hash = DEFAULT_SEED + PRIME64_5 + SizeOf.SIZE_OF_LONG;
        hash = updateTail(hash, v1);
        hash = updateTail(hash, v2);
        hash = finalShuffle(hash);
        return hash;
    }

    static final BigInteger INT64_MIN = BigInteger.valueOf(Long.MIN_VALUE);
    static final BigInteger INT64_MAX = BigInteger.valueOf(Long.MAX_VALUE);
    public static long hash(BigInteger value) {
        if (INT64_MIN.compareTo(value) <= 0 && INT64_MAX.compareTo(value) >= 0) {
            long longValue = value.longValue();
            long hash = DEFAULT_SEED + PRIME64_5 + SizeOf.SIZE_OF_LONG;
            hash = updateTail(hash, longValue);
            hash = finalShuffle(hash);

            return hash;
        }

        String str = value.toString();
        byte[] bytes = str.getBytes();
        return hash(0, bytes, 0, bytes.length);
    }

    public static long hash(BigDecimal value) {
        value = value.stripTrailingZeros();
        if (value.scale() == 0) {
            return hash(value.unscaledValue());
        }

        byte[] bytes = value.toPlainString().getBytes();
        return hash(0, bytes, 0, bytes.length);
    }

    public static long hash(String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        return hash(0, bytes, 0, bytes.length);
    }

    public static long hash(long seed, byte[] bytes, int off, int len) {
        XxHash64 hash = new XxHash64(seed);
        hash.update(bytes, 0, len);
        return hash.hash();
    }

    public static long hash(InputStream in)
            throws IOException {
        return hash(DEFAULT_SEED, in);
    }

    public static long hash(long seed, InputStream in)
            throws IOException {
        XxHash64 hash = new XxHash64(seed);
        byte[] buffer = new byte[8192];
        while (true) {
            int length = in.read(buffer);
            if (length == -1) {
                break;
            }
            hash.update(buffer, 0, length);
        }
        return hash.hash();
    }

    private static long updateTail(long hash, Object base, long address, int index, int length) {
        while (index <= length - 8) {
            hash = updateTail(hash, UNSAFE.getLong(base, address + index));
            index += 8;
        }

        if (index <= length - 4) {
            hash = updateTail(hash, UNSAFE.getInt(base, address + index));
            index += 4;
        }

        while (index < length) {
            hash = updateTail(hash, UNSAFE.getByte(base, address + index));
            index++;
        }

        hash = finalShuffle(hash);

        return hash;
    }

    private static long mix(long current, long value) {
        return rotateLeft(current + value * PRIME64_2, 31) * PRIME64_1;
    }

    private static long update(long hash, long value) {
        long temp = hash ^ mix(0, value);
        return temp * PRIME64_1 + PRIME64_4;
    }

    private static long updateTail(long hash, long value) {
        long temp = hash ^ mix(0, value);
        return rotateLeft(temp, 27) * PRIME64_1 + PRIME64_4;
    }

    private static long updateTail(long hash, int value) {
        long unsigned = value & 0xFFFF_FFFFL;
        long temp = hash ^ (unsigned * PRIME64_1);
        return rotateLeft(temp, 23) * PRIME64_2 + PRIME64_3;
    }

    private static long updateTail(long hash, byte value) {
        int unsigned = value & 0xFF;
        long temp = hash ^ (unsigned * PRIME64_5);
        return rotateLeft(temp, 11) * PRIME64_1;
    }

    private static long finalShuffle(long hash) {
        hash ^= hash >>> 33;
        hash *= PRIME64_2;
        hash ^= hash >>> 29;
        hash *= PRIME64_3;
        hash ^= hash >>> 32;
        return hash;
    }

    public static void checkPositionIndexes(int start, int end, int size) {
        // Carefully optimized for execution by hotspot (explanatory comment above)
        if (start < 0 || end < start || end > size) {
            throw new IndexOutOfBoundsException();
        }
    }

    interface SizeOf {
        byte SIZE_OF_BYTE = 1;
        byte SIZE_OF_SHORT = 2;
        byte SIZE_OF_INT = 4;
        byte SIZE_OF_LONG = 8;
        byte SIZE_OF_FLOAT = 4;
        byte SIZE_OF_DOUBLE = 8;
    }
}
