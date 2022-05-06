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

    static final class UTF16StringCreator implements Function<byte[], String> {
        final long CODER_OFFSET;
        final long VALUE_OFFSET;

        public UTF16StringCreator() throws Exception {
            Field fieldCode = String.class.getDeclaredField("coder");
            fieldCode.setAccessible(true);
            Field fieldValue = String.class.getDeclaredField("value");
            fieldValue.setAccessible(true);

            CODER_OFFSET = UNSAFE.objectFieldOffset(fieldCode);
            VALUE_OFFSET = UNSAFE.objectFieldOffset(fieldValue);
        }

        @Override
        public String apply(byte[] bytes) {
            try {
                Object str = UNSAFE.allocateInstance(String.class);
                UNSAFE.putByte(str, CODER_OFFSET, (byte) 1);
                UNSAFE.putObject(str, VALUE_OFFSET, (byte[]) bytes);
                return (String) str;
            } catch (Throwable ex) {
                throw new JSONException("create string error");
            }
        }
    }

    static final class ASCIIStringCreator implements Function<byte[], String> {
        final long CODER_OFFSET;
        final long VALUE_OFFSET;

        public ASCIIStringCreator() throws Exception {
            Field fieldCode = String.class.getDeclaredField("coder");
            fieldCode.setAccessible(true);
            Field fieldValue = String.class.getDeclaredField("value");
            fieldValue.setAccessible(true);

            CODER_OFFSET = UNSAFE.objectFieldOffset(fieldCode);
            VALUE_OFFSET = UNSAFE.objectFieldOffset(fieldValue);
        }

        @Override
        public String apply(byte[] bytes) {
            try {
                Object str = UNSAFE.allocateInstance(String.class);
                UNSAFE.putByte(str, CODER_OFFSET, (byte) 0);
                UNSAFE.putObject(str, VALUE_OFFSET, bytes);
                return (String) str;
            } catch (Throwable ex) {
                throw new JSONException("create string error");
            }
        }
    }
}
