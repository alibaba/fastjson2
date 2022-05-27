package com.alibaba.fastjson2.util;

import java.lang.invoke.*;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.List;
import java.util.function.*;

public class JDKUtils {
    public static final int JVM_VERSION;

    public static final boolean JAVAC_UNNAMED;
    public static final boolean ILLEGAL_ACCESS_PERMIT;
    public static final boolean LANG_UNNAMED;

    static final Field FIELD_STRING_VALUE;
    static final long FIELD_STRING_VALUE_OFFSET;
    static volatile boolean FIELD_STRING_ERROR;

    static final Class CLASS_SQL_DATASOURCE;
    static final Class CLASS_SQL_ROW_SET;
    public static final boolean HAS_SQL;

    public static final Class CLASS_TRANSIENT;
    public static final byte BIG_ENDIAN;

    public static final boolean UNSAFE_SUPPORT;
    public static final Function<byte[], String> UNSAFE_UTF16_CREATOR;
    public static final Function<byte[], String> UNSAFE_ASCII_CREATOR;

    static {
        String vmVendor = "", vmName = "";

        int jvmVersion = -1;
        try {
            String property = System.getProperty("java.specification.version");
            if (property.startsWith("1.")) {
                property = property.substring(2);
            }
            jvmVersion = Integer.parseInt(property);

            vmName = System.getProperty("java.vm.name");
            vmVendor = System.getProperty("java.vm.vendor");
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

        List<String> inputArguments = ManagementFactory.getRuntimeMXBean().getInputArguments();

        JAVAC_UNNAMED = inputArguments.contains("--add-opens=com.sun.tools.javac.processing=ALL-UNNAMED");
        ILLEGAL_ACCESS_PERMIT = inputArguments.contains("--illegal-access=permit");

        LANG_UNNAMED = !vmVendor.contains("GraalVM") && !vmName.contains("Substrate")
                && (inputArguments.contains("--add-opens=java.base/java.lang=ALL-UNNAMED") || JVM_VERSION <= 11);

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

        boolean unsafeSupport = false;
        unsafeSupport = ((Predicate) o -> {
            try {
                return UnsafeUtils.UNSAFE != null;
            } catch (Throwable ignored) {
                return false;
            }
        }).test(null);
        UNSAFE_SUPPORT = unsafeSupport;

        Boolean bigEndian = null;
        if (JDKUtils.JVM_VERSION > 8 && UNSAFE_SUPPORT && LANG_UNNAMED) {
            Class clazz;
            try {
                clazz = Class.forName("java.lang.StringUTF16");
                Field field = clazz.getDeclaredField("HI_BYTE_SHIFT");
                long fieldOffset = UnsafeUtils.UNSAFE.staticFieldOffset(field);
                int hiByteShift = UnsafeUtils.UNSAFE.getInt(clazz, fieldOffset);
                if (hiByteShift == 8) {
                    bigEndian = true;
                } else {
                    if (hiByteShift == 0) {
                        bigEndian = false;
                    }
                }
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
        }
        BIG_ENDIAN = bigEndian == null
                ? -1
                : bigEndian.booleanValue() ? (byte) 1 : (byte) 0;

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
        if (!FIELD_STRING_ERROR) {
            try {
                return (char[]) UnsafeUtils.UNSAFE.getObject(str, FIELD_STRING_VALUE_OFFSET);
            } catch (Exception ignored) {
                FIELD_STRING_ERROR = true;
            }
        }

        return str.toCharArray();
    }

    public static BiFunction<char[], Boolean, String> getStringCreatorJDK8() throws Throwable {
        MethodHandles.Lookup lookup = getLookup();

        MethodHandles.Lookup caller = lookup.in(String.class);

        MethodHandle handle = caller.findConstructor(
                String.class, MethodType.methodType(void.class, char[].class, boolean.class)
        );

        CallSite callSite = LambdaMetafactory.metafactory(
                caller,
                "apply",
                MethodType.methodType(BiFunction.class),
                handle.type().generic(),
                handle,
                handle.type()
        );
        return (BiFunction) callSite.getTarget().invokeExact();
    }

    public static Function<byte[], String> getStringCreatorJDK11() throws Throwable {
        MethodHandles.Lookup lookup = getLookup();

        Class clazz = Class.forName("java.lang.StringCoding");
        MethodHandles.Lookup caller = lookup.in(clazz);
        MethodHandle handle = caller.findStatic(
                clazz,
                "newStringLatin1",
                MethodType.methodType(String.class, byte[].class)
        );

        CallSite callSite = LambdaMetafactory.metafactory(
                caller,
                "apply",
                MethodType.methodType(Function.class),
                handle.type().generic(),
                handle,
                handle.type()
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
                caller,
                "apply",
                MethodType.methodType(BiFunction.class),
                handle.type().generic(),
                handle,
                handle.type()
        );
        return (BiFunction<byte[], Charset, String>) callSite.getTarget().invokeExact();
    }

    private static MethodHandles.Lookup getLookup() throws Exception {
        MethodHandles.Lookup lookup;
        if (JDKUtils.JVM_VERSION >= 17) {
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
