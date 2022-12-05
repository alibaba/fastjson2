package com.alibaba.fastjson2.util;

import com.alibaba.fastjson2.JSONException;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.function.BiFunction;

import static com.alibaba.fastjson2.util.JDKUtils.JVM_VERSION;

public class UnsafeUtils {
    public static final Unsafe UNSAFE;

    static long STRING_CODER_OFFSET;
    public static long STRING_VALUE_OFFSET;

    static {
        Unsafe unsafe = null;
        try {
            Field theUnsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafeField.setAccessible(true);
            unsafe = (Unsafe) theUnsafeField.get(null);
        } catch (Throwable ignored) {
            // ignored
        }
        UNSAFE = unsafe;
    }

    public static long objectFieldOffset(Field field) {
        return UNSAFE.objectFieldOffset(field);
    }

    public static byte getStringCoder(String str) {
        // GraalVM not support
        // Android not support
        if (str == null) {
            throw new NullPointerException();
        }

        if (JVM_VERSION == 8) {
            return 1;
        }

        if (STRING_CODER_OFFSET == 0) {
            try {
                Field fieldCode = String.class.getDeclaredField("coder");
                STRING_CODER_OFFSET = UNSAFE.objectFieldOffset(fieldCode);
            } catch (Exception e) {
                throw new JSONException("unsafe get String.coder error", e);
            }
        }

        return UNSAFE.getByte(str, STRING_CODER_OFFSET);
    }

    public static byte[] getStringValue(String str) {
        // GraalVM not support
        // Android not support
        if (str == null) {
            throw new NullPointerException();
        }

        if (STRING_VALUE_OFFSET == 0) {
            try {
                Field fieldCode = String.class.getDeclaredField("value");
                STRING_VALUE_OFFSET = UNSAFE.objectFieldOffset(fieldCode);
            } catch (Exception e) {
                throw new JSONException("unsafe get String.value error", e);
            }
        }

        return (byte[]) UNSAFE.getObject(str, STRING_VALUE_OFFSET);
    }

    public static final class UnsafeStringCreator
            implements BiFunction {
        final long coderOffset;
        final long valueOffset;
        static final Byte ZERO = (byte) 0;

        public UnsafeStringCreator() throws Exception {
            Field fieldCode = String.class.getDeclaredField("coder");
            Field fieldValue = String.class.getDeclaredField("value");

            coderOffset = UNSAFE.objectFieldOffset(fieldCode);
            valueOffset = UNSAFE.objectFieldOffset(fieldValue);
        }

        @Override
        public Object apply(Object value, Object coder) {
            try {
                Object str = UNSAFE.allocateInstance(String.class);
                if (coder != ZERO) {
                    UNSAFE.putByte(str, coderOffset, ((Byte) coder).byteValue());
                }

                UNSAFE.putObject(str, valueOffset, value);
                return str;
            } catch (Throwable ex) {
                throw new JSONException("create string error");
            }
        }
    }
}
