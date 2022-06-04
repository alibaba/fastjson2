package com.alibaba.fastjson2.util;

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
}
