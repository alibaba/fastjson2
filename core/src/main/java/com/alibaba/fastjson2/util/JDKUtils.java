package com.alibaba.fastjson2.util;

import sun.misc.Unsafe;

import javax.sql.DataSource;
import javax.sql.RowSet;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.ByteOrder;

public class JDKUtils {
    public static final long FIELD_DECIMAL_INT_COMPACT_OFFSET;
    public static final boolean BIG_ENDIAN = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;
    public static final Unsafe UNSAFE;
    public static final long ARRAY_BYTE_BASE_OFFSET;
    public static final long ARRAY_CHAR_BASE_OFFSET;

    static {
        Unsafe unsafe = null;
        try {
            unsafe = Unsafe.getUnsafe();
        } catch (Throwable ignored) {
            // ignored
        }
        if (unsafe == null) {
            try {
                Field theUnsafeField = null;
                for (Field field : Unsafe.class.getDeclaredFields()) {
                    String fieldName = field.getName();
                    if (fieldName.equals("theUnsafe") || fieldName.equals("THE_ONE")) {
                        theUnsafeField = field;
                        break;
                    }
                }

                if (theUnsafeField != null) {
                    theUnsafeField.setAccessible(true);
                    unsafe = (Unsafe) theUnsafeField.get(null);
                }
            } catch (Throwable ignored) {
                // ignored
            }
        }
        UNSAFE = unsafe;

        int arrayByteBaseOffset = -1;
        int arrayCharBaseOffset = -1;
        if (unsafe != null) {
            arrayByteBaseOffset = unsafe.arrayBaseOffset(byte[].class);
            arrayCharBaseOffset = unsafe.arrayBaseOffset(char[].class);
        }
        ARRAY_BYTE_BASE_OFFSET = arrayByteBaseOffset;
        ARRAY_CHAR_BASE_OFFSET = arrayCharBaseOffset;

        long fieldOffset = -1;
        try {
            Field field = BigDecimal.class.getDeclaredField("intCompact");
            fieldOffset = UNSAFE.objectFieldOffset(field);
        } catch (Throwable ignored) {
            // ignored
        }
        FIELD_DECIMAL_INT_COMPACT_OFFSET = fieldOffset;
    }

    public static boolean isSQLDataSourceOrRowSet(Class<?> type) {
        return DataSource.class.isAssignableFrom(type)
                || RowSet.class.isAssignableFrom(type);
    }

    public static char[] getCharArray(String str) {
        return str.toCharArray();
    }
}
