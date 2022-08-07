package com.alibaba.fastjson2.util;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class UnsafeUtils {
    public static final Unsafe UNSAFE;

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
