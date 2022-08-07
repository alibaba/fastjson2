package com.alibaba.fastjson2.util;

import java.lang.reflect.Field;
import java.nio.ByteOrder;
import java.util.function.*;

public class JDKUtils {
    public static final int JVM_VERSION;

    static final Field FIELD_STRING_VALUE;
    static final long FIELD_STRING_VALUE_OFFSET;
    static volatile boolean FIELD_STRING_ERROR;

    static final Class<?> CLASS_SQL_DATASOURCE;
    static final Class<?> CLASS_SQL_ROW_SET;
    public static final boolean HAS_SQL;

    public static final Class CLASS_TRANSIENT;
    public static final boolean BIG_ENDIAN;

    public static final boolean UNSAFE_SUPPORT;

    static {
        boolean openj9 = false;
        int jvmVersion = -1;
        try {
            String property = System.getProperty("java.specification.version");
            if (property.startsWith("1.")) {
                property = property.substring(2);
            }
            jvmVersion = Integer.parseInt(property);

            String jmvName = System.getProperty("java.vm.name");
            openj9 = jmvName.contains("OpenJ9");
            if (openj9) {
                FIELD_STRING_ERROR = true;
            }
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

        Class transientClass = null;
        try {
            transientClass = Class.forName("java.beans.Transient");
        } catch (Throwable ignored) {
        }
        CLASS_TRANSIENT = transientClass;

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

        BIG_ENDIAN = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;
    }

    public static boolean isSQLDataSourceOrRowSet(Class<?> type) {
        return (CLASS_SQL_DATASOURCE != null && CLASS_SQL_DATASOURCE.isAssignableFrom(type))
                || (CLASS_SQL_ROW_SET != null && CLASS_SQL_ROW_SET.isAssignableFrom(type));
    }
}
