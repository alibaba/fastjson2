package com.alibaba.fastjson2.util;

import com.alibaba.fastjson2.JSONException;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.function.Function;

public class UnsafeUtils {
    public static final Unsafe UNSAFE;

    static volatile Function<byte[], String> STRING_CREATOR_UTF16;
    static volatile boolean STRING_CREATOR_UTF16_ERROR;

    static volatile Function<byte[], String> STRING_CREATOR_ASCII;
    static volatile boolean STRING_CREATOR_ASCII_ERROR;

    static long STRING_CODER_OFFSET;
    static long STRING_VALUE_OFFSET;

    static {
        Unsafe unsafe = null;
        try {
            Field theUnsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafeField.setAccessible(true);
            unsafe = (Unsafe) theUnsafeField.get(null);
        } catch (Throwable ignored) {
            ignored.printStackTrace();
        }
        UNSAFE = unsafe;
    }

    public static long objectFieldOffset(Field field) {
        return UNSAFE.objectFieldOffset(field);
    }

    public static Function<byte[], String> getStringCreatorUTF16() {
        if (STRING_CREATOR_UTF16 == null) {
            if (!STRING_CREATOR_UTF16_ERROR) {
                try {
                    STRING_CREATOR_UTF16 = new UTF16StringCreator();
                } catch (Throwable ignored) {
                    STRING_CREATOR_UTF16_ERROR = true;
                }
            }
        }
        return STRING_CREATOR_UTF16;
    }

    public static Function<byte[], String> getStringCreatorASCII() {
        if (STRING_CREATOR_ASCII == null) {
            if (!STRING_CREATOR_ASCII_ERROR) {
                try {
                    STRING_CREATOR_ASCII = new ASCIIStringCreator();
                } catch (Throwable ignored) {
                    STRING_CREATOR_ASCII_ERROR = true;
                }
            }
        }
        return STRING_CREATOR_ASCII;
    }

    public static byte getStringCoder(String str) {
        if (str == null) {
            throw new NullPointerException();
        }

        if (JDKUtils.JVM_VERSION == 8) {
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

    static final class UTF16StringCreator
            implements Function<byte[], String> {
        final long coderOffset;
        final long valueOffset;

        public UTF16StringCreator() throws Exception {
            Field fieldCode = String.class.getDeclaredField("coder");
            Field fieldValue = String.class.getDeclaredField("value");

            coderOffset = UNSAFE.objectFieldOffset(fieldCode);
            valueOffset = UNSAFE.objectFieldOffset(fieldValue);
        }

        @Override
        public String apply(byte[] bytes) {
            try {
                Object str = UNSAFE.allocateInstance(String.class);
                UNSAFE.putByte(str, coderOffset, (byte) 1);
                UNSAFE.putObject(str, valueOffset, (byte[]) bytes);
                return (String) str;
            } catch (Throwable ex) {
                throw new JSONException("create string error");
            }
        }
    }

    static final class ASCIIStringCreator
            implements Function<byte[], String> {
        final long coderOffset;
        final long valueOffset;

        public ASCIIStringCreator() throws Exception {
            Field fieldCode = String.class.getDeclaredField("coder");
            Field fieldValue = String.class.getDeclaredField("value");

            coderOffset = UNSAFE.objectFieldOffset(fieldCode);
            valueOffset = UNSAFE.objectFieldOffset(fieldValue);
        }

        @Override
        public String apply(byte[] bytes) {
            try {
                Object str = UNSAFE.allocateInstance(String.class);
                UNSAFE.putByte(str, coderOffset, (byte) 0);
                UNSAFE.putObject(str, valueOffset, bytes);
                return (String) str;
            } catch (Throwable ex) {
                throw new JSONException("create string error");
            }
        }
    }
}
