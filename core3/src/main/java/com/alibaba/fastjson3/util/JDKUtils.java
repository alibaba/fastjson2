package com.alibaba.fastjson3.util;

import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;

/**
 * JDK utility class providing low-level access for performance optimization.
 * Inspired by wast's UnsafeHelper / JSONEndianUnsafe.
 *
 * <p>Uses sun.misc.Unsafe when available (with fallback) for:
 * <ul>
 *   <li>Bulk array comparison (8 bytes at a time)</li>
 *   <li>Fast String creation without char[] copy (JDK 8-style)</li>
 *   <li>Direct field access bypassing getter overhead</li>
 * </ul>
 *
 * <p>Requires JDK 21+. Uses Record API directly (no reflection).</p>
 */
public final class JDKUtils {
    public static final boolean UNSAFE_AVAILABLE;
    public static final boolean NATIVE_IMAGE;
    public static final int JDK_VERSION;

    private static final sun.misc.Unsafe UNSAFE;
    private static final long BYTE_ARRAY_OFFSET;
    private static final long CHAR_ARRAY_OFFSET;

    // String internal field offset for zero-copy String creation
    private static final long STRING_VALUE_OFFSET;
    private static final long STRING_CODER_OFFSET;
    private static final boolean COMPACT_STRINGS; // JDK 9+ uses byte[] internally
    public static final boolean FAST_STRING_CREATION;

    // Vector API support (jdk.incubator.vector)
    public static final boolean VECTOR_SUPPORT;
    public static final int VECTOR_BYTE_SIZE;

    static {
        JDK_VERSION = Runtime.version().feature();

        // Detect GraalVM native-image runtime
        boolean nativeImage = false;
        try {
            nativeImage = "runtime".equals(System.getProperty("org.graalvm.nativeimage.imagecode"));
        } catch (Throwable ignored) {
        }
        NATIVE_IMAGE = nativeImage;

        sun.misc.Unsafe u = null;
        try {
            Field f = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            u = (sun.misc.Unsafe) f.get(null);
        } catch (Throwable ignored) {
            // Unsafe not available
        }
        UNSAFE = u;
        UNSAFE_AVAILABLE = UNSAFE != null;

        if (UNSAFE_AVAILABLE) {
            BYTE_ARRAY_OFFSET = UNSAFE.arrayBaseOffset(byte[].class);
            CHAR_ARRAY_OFFSET = UNSAFE.arrayBaseOffset(char[].class);

            long valOffset = -1;
            long coderOffset = -1;
            boolean compact = false;
            try {
                Field valueField = String.class.getDeclaredField("value");
                valOffset = UNSAFE.objectFieldOffset(valueField);
                // JDK 9+: String has a 'coder' field (compact strings)
                try {
                    Field coderField = String.class.getDeclaredField("coder");
                    coderOffset = UNSAFE.objectFieldOffset(coderField);
                    compact = true;
                } catch (NoSuchFieldException ignored) {
                    // JDK 8: no coder field
                }
            } catch (Throwable ignored) {
                // Can't access String internals
            }
            STRING_VALUE_OFFSET = valOffset;
            STRING_CODER_OFFSET = coderOffset;
            COMPACT_STRINGS = compact;
        } else {
            BYTE_ARRAY_OFFSET = -1;
            CHAR_ARRAY_OFFSET = -1;
            STRING_VALUE_OFFSET = -1;
            STRING_CODER_OFFSET = -1;
            COMPACT_STRINGS = false;
        }

        // Validate Unsafe String creation at startup (disabled in native-image:
        // String internal layout may differ from HotSpot)
        boolean fsc = false;
        if (!NATIVE_IMAGE && UNSAFE_AVAILABLE && COMPACT_STRINGS && STRING_VALUE_OFFSET >= 0 && STRING_CODER_OFFSET >= 0) {
            try {
                String test = (String) UNSAFE.allocateInstance(String.class);
                UNSAFE.putObject(test, STRING_VALUE_OFFSET, new byte[]{'t', 'e', 's', 't'});
                UNSAFE.putByte(test, STRING_CODER_OFFSET, (byte) 0);
                fsc = "test".equals(test);
            } catch (Throwable ignored) {
                // Fall through
            }
        }
        FAST_STRING_CREATION = fsc;

        // Detect Vector API: try to load VectorizedScanner which imports jdk.incubator.vector.
        // If the module is not available, the class won't load and we fall back to SWAR.
        boolean vs = false;
        int vbs = 0;
        try {
            // Force class loading to verify jdk.incubator.vector is accessible
            vbs = VectorizedScanner.VECTOR_SIZE;
            vs = vbs >= 16;
        } catch (Throwable ignored) {
            // NoClassDefFoundError / UnsupportedOperationException / etc.
        }
        VECTOR_SUPPORT = vs;
        VECTOR_BYTE_SIZE = vbs;
    }

    private JDKUtils() {
    }

    // ==================== Array bulk operations ====================

    /**
     * Read a long (8 bytes) from a byte array at the given offset.
     * Falls back to manual assembly if Unsafe is unavailable.
     */
    public static long getLong(byte[] buf, int offset) {
        if (UNSAFE_AVAILABLE) {
            return UNSAFE.getLong(buf, BYTE_ARRAY_OFFSET + offset);
        }
        // Manual fallback (big-endian)
        return ((long) buf[offset] << 56)
                | ((long) (buf[offset + 1] & 0xFF) << 48)
                | ((long) (buf[offset + 2] & 0xFF) << 40)
                | ((long) (buf[offset + 3] & 0xFF) << 32)
                | ((long) (buf[offset + 4] & 0xFF) << 24)
                | ((long) (buf[offset + 5] & 0xFF) << 16)
                | ((long) (buf[offset + 6] & 0xFF) << 8)
                | ((long) (buf[offset + 7] & 0xFF));
    }

    /**
     * Write a long (8 bytes) to a byte array at the given offset.
     */
    public static void putLong(byte[] buf, int offset, long value) {
        if (UNSAFE_AVAILABLE) {
            UNSAFE.putLong(buf, BYTE_ARRAY_OFFSET + offset, value);
            return;
        }
        buf[offset] = (byte) (value >> 56);
        buf[offset + 1] = (byte) (value >> 48);
        buf[offset + 2] = (byte) (value >> 40);
        buf[offset + 3] = (byte) (value >> 32);
        buf[offset + 4] = (byte) (value >> 24);
        buf[offset + 5] = (byte) (value >> 16);
        buf[offset + 6] = (byte) (value >> 8);
        buf[offset + 7] = (byte) value;
    }

    /**
     * Read a long (4 chars = 8 bytes) from a char array at the given char offset.
     */
    public static long getCharLong(char[] buf, int charOffset) {
        if (UNSAFE_AVAILABLE) {
            return UNSAFE.getLong(buf, CHAR_ARRAY_OFFSET + ((long) charOffset << 1));
        }
        return ((long) buf[charOffset] << 48)
                | ((long) buf[charOffset + 1] << 32)
                | ((long) buf[charOffset + 2] << 16)
                | ((long) buf[charOffset + 3]);
    }

    // ==================== Fast String creation ====================

    /**
     * Get the internal byte[] or char[] value of a String without copying.
     * Returns null if Unsafe is unavailable.
     */
    public static Object getStringValue(String s) {
        if (UNSAFE_AVAILABLE && STRING_VALUE_OFFSET >= 0) {
            return UNSAFE.getObject(s, STRING_VALUE_OFFSET);
        }
        return null;
    }

    /**
     * Get the coder of a String: 0 = LATIN1, 1 = UTF16.
     * Returns -1 if Unsafe is unavailable.
     */
    public static int getStringCoder(String s) {
        if (UNSAFE_AVAILABLE && COMPACT_STRINGS && STRING_CODER_OFFSET >= 0) {
            return UNSAFE.getByte(s, STRING_CODER_OFFSET);
        }
        return -1;
    }

    /**
     * Create a String from a byte[] (Latin-1) without copying, if possible.
     * Falls back to normal constructor if Unsafe is unavailable.
     */
    public static String createAsciiString(byte[] bytes, int offset, int length) {
        if (!NATIVE_IMAGE && UNSAFE_AVAILABLE && COMPACT_STRINGS && STRING_VALUE_OFFSET >= 0) {
            try {
                byte[] value;
                if (offset == 0 && length == bytes.length) {
                    value = bytes;
                } else {
                    value = new byte[length];
                    System.arraycopy(bytes, offset, value, 0, length);
                }
                String s = new String();
                UNSAFE.putObject(s, STRING_VALUE_OFFSET, value);
                UNSAFE.putByte(s, STRING_CODER_OFFSET, (byte) 0); // LATIN1 = 0
                return s;
            } catch (Throwable ignored) {
                // Fall through
            }
        }
        return new String(bytes, offset, length, java.nio.charset.StandardCharsets.ISO_8859_1);
    }

    // ==================== Fast Latin1 String creation ====================

    /**
     * Create a Latin1 String directly using Unsafe, bypassing charset lookup and constructor overhead.
     * Caller must ensure FAST_STRING_CREATION is true before calling.
     * The src bytes must be ASCII/Latin1.
     */
    public static String createLatin1String(byte[] src, int off, int len) {
        // Arrays.copyOfRange is a JIT intrinsic — single allocation + bulk copy
        byte[] value = java.util.Arrays.copyOfRange(src, off, off + len);
        // new String() initializes coder=0 (LATIN1) — no need to set coder explicitly
        String s = new String();
        UNSAFE.putObject(s, STRING_VALUE_OFFSET, value);
        return s;
    }

    // ==================== Unsafe direct memory ops (hot path, no fallback) ====================

    /**
     * Write a long directly to a byte array. No fallback — caller must check UNSAFE_AVAILABLE.
     */
    public static void putLongDirect(byte[] buf, int offset, long value) {
        UNSAFE.putLong(buf, BYTE_ARRAY_OFFSET + offset, value);
    }

    /**
     * Read a long directly from a byte array. No fallback — caller must check UNSAFE_AVAILABLE.
     */
    public static long getLongDirect(byte[] buf, int offset) {
        return UNSAFE.getLong(buf, BYTE_ARRAY_OFFSET + offset);
    }

    /**
     * Read an int directly from a byte array.
     */
    public static int getIntDirect(byte[] buf, int offset) {
        return UNSAFE.getInt(buf, BYTE_ARRAY_OFFSET + offset);
    }

    /**
     * Write an int directly to a byte array.
     */
    public static void putIntDirect(byte[] buf, int offset, int value) {
        UNSAFE.putInt(buf, BYTE_ARRAY_OFFSET + offset, value);
    }

    /**
     * Write a short directly to a byte array.
     */
    public static void putShortDirect(byte[] buf, int offset, short value) {
        UNSAFE.putShort(buf, BYTE_ARRAY_OFFSET + offset, value);
    }

    // ==================== Direct field access (bypass reflection) ====================

    /**
     * Compute the Unsafe field offset for direct field access.
     * Returns -1 if Unsafe is unavailable.
     */
    public static long objectFieldOffset(Field field) {
        if (UNSAFE_AVAILABLE) {
            try {
                return UNSAFE.objectFieldOffset(field);
            } catch (UnsupportedOperationException e) {
                // JDK 16+ blocks Unsafe access to Record fields
                return -1;
            }
        }
        return -1;
    }

    /**
     * Read an object reference field directly via Unsafe.
     */
    public static Object getObject(Object obj, long offset) {
        return UNSAFE.getObject(obj, offset);
    }

    /**
     * Read an int field directly via Unsafe.
     */
    public static int getInt(Object obj, long offset) {
        return UNSAFE.getInt(obj, offset);
    }

    /**
     * Read a long field directly via Unsafe.
     */
    public static long getLongField(Object obj, long offset) {
        return UNSAFE.getLong(obj, offset);
    }

    /**
     * Read a double field directly via Unsafe.
     */
    public static double getDouble(Object obj, long offset) {
        return UNSAFE.getDouble(obj, offset);
    }

    /**
     * Read a float field directly via Unsafe.
     */
    public static float getFloat(Object obj, long offset) {
        return UNSAFE.getFloat(obj, offset);
    }

    /**
     * Read a boolean field directly via Unsafe.
     */
    public static boolean getBoolean(Object obj, long offset) {
        return UNSAFE.getBoolean(obj, offset);
    }

    // ==================== Direct field write (bypass reflection) ====================

    public static void putObject(Object obj, long offset, Object value) {
        UNSAFE.putObject(obj, offset, value);
    }

    /**
     * Set the internal value byte[] of a String using Unsafe.
     * Used to create a String that shares an existing byte[] without copying.
     */
    public static void putStringValue(String s, byte[] value) {
        if (STRING_VALUE_OFFSET >= 0) {
            UNSAFE.putObject(s, STRING_VALUE_OFFSET, value);
        }
    }

    public static void putInt(Object obj, long offset, int value) {
        UNSAFE.putInt(obj, offset, value);
    }

    public static void putLongField(Object obj, long offset, long value) {
        UNSAFE.putLong(obj, offset, value);
    }

    public static void putDouble(Object obj, long offset, double value) {
        UNSAFE.putDouble(obj, offset, value);
    }

    public static void putFloat(Object obj, long offset, float value) {
        UNSAFE.putFloat(obj, offset, value);
    }

    public static void putBoolean(Object obj, long offset, boolean value) {
        UNSAFE.putBoolean(obj, offset, value);
    }

    // ==================== Bulk byte array comparison ====================

    /**
     * Compare two byte arrays using Unsafe long reads for 8x throughput.
     */
    public static boolean arrayEquals(byte[] a, int aOff, byte[] b, int bOff, int len) {
        if (UNSAFE_AVAILABLE) {
            while (len >= 8) {
                if (UNSAFE.getLong(a, BYTE_ARRAY_OFFSET + aOff)
                        != UNSAFE.getLong(b, BYTE_ARRAY_OFFSET + bOff)) {
                    return false;
                }
                aOff += 8;
                bOff += 8;
                len -= 8;
            }
            while (len > 0) {
                if (a[aOff++] != b[bOff++]) {
                    return false;
                }
                len--;
            }
            return true;
        }
        // Fallback
        for (int i = 0; i < len; i++) {
            if (a[aOff + i] != b[bOff + i]) {
                return false;
            }
        }
        return true;
    }

    // ==================== Record support (JDK 21+, direct API) ====================

    /**
     * Check if a class is a Java Record.
     */
    public static boolean isRecord(Class<?> type) {
        return type.isRecord();
    }

    /**
     * Get record component names in declaration order.
     */
    public static String[] getRecordComponentNames(Class<?> recordType) {
        RecordComponent[] components = recordType.getRecordComponents();
        String[] names = new String[components.length];
        for (int i = 0; i < components.length; i++) {
            names[i] = components[i].getName();
        }
        return names;
    }

    /**
     * Get record component types in declaration order.
     */
    public static Class<?>[] getRecordComponentTypes(Class<?> recordType) {
        RecordComponent[] components = recordType.getRecordComponents();
        Class<?>[] types = new Class[components.length];
        for (int i = 0; i < components.length; i++) {
            types[i] = components[i].getType();
        }
        return types;
    }

    /**
     * Get record component generic types in declaration order.
     */
    public static java.lang.reflect.Type[] getRecordComponentGenericTypes(Class<?> recordType) {
        RecordComponent[] components = recordType.getRecordComponents();
        java.lang.reflect.Type[] types = new java.lang.reflect.Type[components.length];
        for (int i = 0; i < components.length; i++) {
            types[i] = components[i].getGenericType();
        }
        return types;
    }

    /**
     * Get accessor methods for each record component in declaration order.
     */
    public static java.lang.reflect.Method[] getRecordComponentAccessors(Class<?> recordType) {
        RecordComponent[] components = recordType.getRecordComponents();
        java.lang.reflect.Method[] accessors = new java.lang.reflect.Method[components.length];
        for (int i = 0; i < components.length; i++) {
            accessors[i] = components[i].getAccessor();
        }
        return accessors;
    }
}
