package com.alibaba.fastjson2.util;

import java.lang.invoke.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.function.*;

public class JDKUtils {
    public static final int JVM_VERSION;
    public static final Byte LATIN1 = 0;
    public static final Byte UTF16 = 1;

    static final Field FIELD_STRING_VALUE;
    static final long FIELD_STRING_VALUE_OFFSET;
    static volatile boolean FIELD_STRING_ERROR;

    static final Class<?> CLASS_SQL_DATASOURCE;
    static final Class<?> CLASS_SQL_ROW_SET;
    public static final boolean HAS_SQL;

    public static final boolean UNSAFE_SUPPORT;

    static {
        int jvmVersion = -1;
        try {
            String property = System.getProperty("java.specification.version");
            if (property.startsWith("1.")) {
                property = property.substring(2);
            }
            jvmVersion = Integer.parseInt(property);
        } catch (Throwable ignored) {
        }

        boolean hasJavaSql = true;
        Class dataSourceClass = null;
        Class rowSetClass = null;
        try {
            dataSourceClass = Class.forName("javax.sql.DataSource");
            rowSetClass = Class.forName("javax.sql.RowSet");
        } catch (Throwable e) {
            hasJavaSql = false;
        }
        CLASS_SQL_DATASOURCE = dataSourceClass;
        CLASS_SQL_ROW_SET = rowSetClass;
        HAS_SQL = hasJavaSql;

        JVM_VERSION = jvmVersion;

        if (JVM_VERSION == 8) {
            Field field = null;
            long fieldOffset = -1;
            try {
                field = String.class.getDeclaredField("value");
                field.setAccessible(true);
                fieldOffset = UnsafeUtils.objectFieldOffset(field);
            } catch (Exception ignored) {
                FIELD_STRING_ERROR = true;
            }

            FIELD_STRING_VALUE = field;
            FIELD_STRING_VALUE_OFFSET = fieldOffset;
        } else {
            FIELD_STRING_ERROR = true;
            FIELD_STRING_VALUE = null;
            FIELD_STRING_VALUE_OFFSET = -1;
        }

        boolean unsafeSupport;
        unsafeSupport = ((Predicate) o -> {
            try {
                return UnsafeUtils.UNSAFE != null;
            } catch (Throwable ignored) {
                return false;
            }
        }).test(null);
        UNSAFE_SUPPORT = unsafeSupport;
    }

    public static boolean isSQLDataSourceOrRowSet(Class<?> type) {
        return (CLASS_SQL_DATASOURCE != null && CLASS_SQL_DATASOURCE.isAssignableFrom(type))
                || (CLASS_SQL_ROW_SET != null && CLASS_SQL_ROW_SET.isAssignableFrom(type));
    }

//
//    public static BiFunction<byte[], java.nio.charset.Charset, String> getStringCreatorJDK17() throws Throwable {
//        MethodHandles.Lookup lookup = getLookup();
//
//        MethodHandles.Lookup caller = lookup.in(String.class);
//        MethodHandle handle = caller.findStatic(
//                String.class, "newStringNoRepl1", MethodType.methodType(String.class, byte[].class, Charset.class)
//        );
//
//        CallSite callSite = LambdaMetafactory.metafactory(
//                caller,
//                "apply",
//                MethodType.methodType(BiFunction.class),
//                handle.type().generic(),
//                handle,
//                handle.type()
//        );
//        return (BiFunction<byte[], java.nio.charset.Charset, String>) callSite.getTarget().invokeExact();
//    }

    static MethodHandles.Lookup getLookup() throws Exception {
        // GraalVM not support
        // Android not support
        MethodHandles.Lookup lookup;
        if (JVM_VERSION >= 15) {
            Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, Class.class, int.class);
            constructor.setAccessible(true);
            lookup = constructor.newInstance(
                    String.class,
                    null,
                    -1 // Lookup.TRUSTED
            );
        } else {
            Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);
            constructor.setAccessible(true);
            lookup = constructor.newInstance(
                    String.class,
                    -1 // Lookup.TRUSTED
            );
        }
        return lookup;
    }
}
