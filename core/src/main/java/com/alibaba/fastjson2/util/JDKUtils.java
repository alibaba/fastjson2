package com.alibaba.fastjson2.util;

import java.lang.invoke.*;
import java.lang.reflect.Constructor;
import java.nio.charset.Charset;
import java.util.function.*;

public class JDKUtils {
    public static final int JVM_VERSION;

    static volatile ToIntFunction<String> CODER_FUNCTION;
    static volatile Function<String, byte[]> VALUE_FUNCTION;

    static final Class CLASS_SQL_DATASOURCE;
    static final Class CLASS_SQL_ROW_SET;
    public static final boolean HAS_SQL;

    public static final Class CLASS_TRANSIENT;
    public final static byte BIG_ENDIAN;

    public final static boolean UNSAFE_SUPPORT;
    public final static Function<byte[], String> UNSAFE_UTF16_CREATOR;
    public final static Function<byte[], String> UNSAFE_ASCII_CREATOR;

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

        Function<byte[], String> utf16Creator = null, asciiCreator = null;
        if (unsafeSupport) {
            try {
                utf16Creator = ((Supplier<Function<byte[], String>>) () -> UnsafeUtils.getStringCreatorUTF16()).get();
                asciiCreator = ((Supplier<Function<byte[], String>>) () -> UnsafeUtils.getStringCreatorASCII()).get();
            } catch (Throwable ignored) {

            }
        }
        UNSAFE_UTF16_CREATOR = utf16Creator;
        UNSAFE_ASCII_CREATOR = asciiCreator;
    }

    public static boolean isSQLDataSourceOrRowSet(Class type) {
        return (CLASS_SQL_DATASOURCE != null && CLASS_SQL_ROW_SET.isAssignableFrom(type))
                || (CLASS_SQL_ROW_SET != null && CLASS_SQL_ROW_SET.isAssignableFrom(type));
    }

    public static char[] getCharArray(String str) {
        return str.toCharArray();
    }

    public static BiFunction<char[], Boolean, String> getStringCreatorJDK8() throws Throwable {
        MethodHandles.Lookup lookup = getLookup();

        MethodHandles.Lookup caller = lookup.in(String.class);

        MethodHandle handle = caller.findConstructor(
                String.class, MethodType.methodType(void.class, char[].class, boolean.class)
        );

        CallSite callSite = LambdaMetafactory.metafactory(
                caller
                , "apply"
                , MethodType.methodType(BiFunction.class)
                , handle.type().generic()
                , handle
                , handle.type()
        );
        return (BiFunction) callSite.getTarget().invokeExact();
    }

    public static Function<byte[], String> getStringCreatorJDK11() throws Throwable {
        MethodHandles.Lookup lookup = getLookup();

        Class clazz = Class.forName("java.lang.StringCoding");
        MethodHandles.Lookup caller = lookup.in(clazz);
        MethodHandle handle = caller.findStatic(
                clazz
                , "newStringLatin1"
                , MethodType.methodType(String.class, byte[].class)
        );

        CallSite callSite = LambdaMetafactory.metafactory(
                caller
                , "apply"
                , MethodType.methodType(Function.class)
                , handle.type().generic()
                , handle
                , handle.type()
        );
        return (Function<byte[], String>) callSite.getTarget().invokeExact();
    }

    public static BiFunction<byte[], Charset, String> getStringCreatorJDK17() throws Throwable {
        MethodHandles.Lookup lookup = getLookup();

        MethodHandles.Lookup caller = lookup.in(String.class);
        MethodHandle handle = caller.findStatic(
                String.class, "newStringNoRepl1", MethodType.methodType(String.class, byte[].class, Charset.class)
        );

        CallSite callSite = LambdaMetafactory.metafactory(
                caller
                , "apply"
                , MethodType.methodType(BiFunction.class)
                , handle.type().generic()
                , handle
                , handle.type()
        );
        return (BiFunction<byte[], Charset, String>) callSite.getTarget().invokeExact();
    }

    public static ToIntFunction<String> getStringCode11() throws Throwable {
        if (CODER_FUNCTION != null) {
            return CODER_FUNCTION;
        }

        MethodHandles.Lookup lookup = getLookup();

        MethodHandles.Lookup caller = lookup.in(String.class);
        MethodHandle handle = caller.findVirtual(
                String.class, "coder", MethodType.methodType(byte.class)
        );

        CallSite callSite = LambdaMetafactory.metafactory(
                caller
                , "applyAsInt"
                , MethodType.methodType(ToIntFunction.class)
                , MethodType.methodType(int.class, Object.class)
                , handle
                , handle.type()
        );
        return CODER_FUNCTION = (ToIntFunction<String>) callSite.getTarget().invokeExact();
    }

    public static Function<String, byte[]> getStringValue11() throws Throwable {
        if (VALUE_FUNCTION != null) {
            return VALUE_FUNCTION;
        }

        MethodHandles.Lookup lookup = getLookup();

        MethodHandles.Lookup caller = lookup.in(String.class);
        MethodHandle handle = caller.findVirtual(
                String.class, "value", MethodType.methodType(byte[].class)
        );

        CallSite callSite = LambdaMetafactory.metafactory(
                caller
                , "apply"
                , MethodType.methodType(Function.class)
                , handle.type().generic()
                , handle
                , handle.type()
        );
        return VALUE_FUNCTION = (Function<String, byte[]>) callSite.getTarget().invokeExact();
    }

    private static MethodHandles.Lookup getLookup() throws Exception {
        MethodHandles.Lookup lookup;
        if (JDKUtils.JVM_VERSION >= 17) {
            Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, Class.class, int.class);
            constructor.setAccessible(true);
            lookup = constructor.newInstance(
                    String.class
                    , null
                    , -1 // Lookup.TRUSTED
            );
        } else {
            Constructor<MethodHandles.Lookup> constructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);
            constructor.setAccessible(true);
            lookup = constructor.newInstance(
                    String.class
                    , -1 // Lookup.TRUSTED
            );
        }
        return lookup;
    }
}
