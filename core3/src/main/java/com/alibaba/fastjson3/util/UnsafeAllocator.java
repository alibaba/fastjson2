package com.alibaba.fastjson3.util;

import java.lang.reflect.Field;

/**
 * Allocates object instances using sun.misc.Unsafe, bypassing the constructor.
 * This is significantly faster than Constructor.newInstance() for POJO deserialization.
 */
public final class UnsafeAllocator {
    private static final sun.misc.Unsafe UNSAFE;

    static {
        sun.misc.Unsafe u = null;
        try {
            Field f = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            u = (sun.misc.Unsafe) f.get(null);
        } catch (Throwable ignored) {
        }
        UNSAFE = u;
    }

    private UnsafeAllocator() {
    }

    @SuppressWarnings("unchecked")
    public static <T> T allocateInstance(Class<T> clazz) throws InstantiationException {
        return (T) UNSAFE.allocateInstance(clazz);
    }

    /**
     * Same as allocateInstance but wraps the checked exception.
     * Avoids try/catch in hot path callers which can inhibit JIT inlining.
     */
    @SuppressWarnings("unchecked")
    public static <T> T allocateInstanceUnchecked(Class<T> clazz) {
        try {
            return (T) UNSAFE.allocateInstance(clazz);
        } catch (InstantiationException e) {
            throw new RuntimeException("cannot allocate instance of " + clazz.getName(), e);
        }
    }
}
