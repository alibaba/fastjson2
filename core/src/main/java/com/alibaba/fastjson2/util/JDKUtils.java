package com.alibaba.fastjson2.util;

import java.util.function.*;

public class JDKUtils {
    public static final int JVM_VERSION;

    static final Class CLASS_SQL_DATASOURCE;
    static final Class CLASS_SQL_ROW_SET;
    public static final boolean HAS_SQL;

    public static final Class CLASS_TRANSIENT;
    public final static byte BIG_ENDIAN;

    public final static boolean UNSAFE_SUPPORT;

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

        Class transientClass = null;
        try {
            transientClass = Class.forName("java.beans.Transient");
        } catch (Throwable ignored) {
        }
        CLASS_TRANSIENT = transientClass;

        JVM_VERSION = jvmVersion;


        Boolean bigEndian = null;
        BIG_ENDIAN = bigEndian == null
                ? -1
                : bigEndian.booleanValue() ? (byte) 1 : (byte) 0;

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

    public static boolean isSQLDataSourceOrRowSet(Class type) {
        return (CLASS_SQL_DATASOURCE != null && CLASS_SQL_ROW_SET.isAssignableFrom(type))
                || (CLASS_SQL_ROW_SET != null && CLASS_SQL_ROW_SET.isAssignableFrom(type));
    }

    public static char[] getCharArray(String str) {
        return str.toCharArray();
    }
}
