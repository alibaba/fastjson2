package com.alibaba.fastjson2.util;

import java.lang.invoke.*;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.nio.ByteOrder;
import java.util.List;
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

    // Android not support
    public static final Class CLASS_TRANSIENT;
    public static final boolean BIG_ENDIAN;

    public static final boolean UNSAFE_SUPPORT;

    // GraalVM not support
    // Android not support
    public static final BiFunction<char[], Boolean, String> STRING_CREATOR_JDK8;
    public static final BiFunction<byte[], Byte, String> STRING_CREATOR_JDK11;
    public static final ToIntFunction<String> STRING_CODER;
    public static final Function<String, byte[]> STRING_VALUE;

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

        BiFunction<char[], Boolean, String> stringCreatorJDK8 = null;
        BiFunction<byte[], Byte, String> stringCreatorJDK11 = null;
        ToIntFunction<String> stringCoder = null;
        Function<String, byte[]> stringValue = null;

        Boolean compact_strings = null;
        try {
            if (JVM_VERSION == 8) {
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
                stringCreatorJDK8 = (BiFunction<char[], Boolean, String>) callSite.getTarget().invokeExact();
            }

            boolean lookupLambda;
            if (JVM_VERSION > 8 && JVM_VERSION < 16 && !openj9) {
                try {
                    Field compact_strings_field = String.class.getDeclaredField("COMPACT_STRINGS");
                    if (compact_strings_field != null) {
                        compact_strings_field.setAccessible(true);
                        compact_strings = (Boolean) compact_strings_field.get(null);
                    }
                } catch (Throwable ignored) {
                    // ignored
                }

                lookupLambda = compact_strings != null && compact_strings.booleanValue();
            } else {
                List<String> inputArguments = ManagementFactory
                        .getRuntimeMXBean()
                        .getInputArguments();
                lookupLambda = inputArguments.contains("--add-opens=java.base/java.lang.invoke=ALL-UNNAMED")
                        || inputArguments.contains("--add-opens=java.base/java.lang.invoke=com.alibaba.fastjson2");
                compact_strings = !inputArguments.contains("-XX:-CompactStrings");
            }

            if (lookupLambda) {
                MethodHandles.Lookup lookup = getLookup();
                MethodHandles.Lookup caller = lookup.in(String.class);
                MethodHandle handle = caller.findConstructor(
                        String.class, MethodType.methodType(void.class, byte[].class, byte.class)
                );
                CallSite callSite = LambdaMetafactory.metafactory(
                        caller,
                        "apply",
                        MethodType.methodType(BiFunction.class),
                        handle.type().generic(),
                        handle,
                        MethodType.methodType(String.class, byte[].class, Byte.class)
                );
                stringCreatorJDK11 = (BiFunction<byte[], Byte, String>) callSite.getTarget().invokeExact();

                MethodHandles.Lookup stringCaller = lookup.in(String.class);

                MethodHandle coder = stringCaller.findSpecial(
                        String.class,
                        "coder",
                        MethodType.methodType(byte.class),
                        String.class
                );
                CallSite applyAsInt = LambdaMetafactory.metafactory(
                        stringCaller,
                        "applyAsInt",
                        MethodType.methodType(ToIntFunction.class),
                        MethodType.methodType(int.class, Object.class),
                        coder,
                        coder.type()
                );
                stringCoder = (ToIntFunction<String>) applyAsInt.getTarget().invokeExact();

                MethodHandle value = stringCaller.findSpecial(
                        String.class,
                        "value",
                        MethodType.methodType(byte[].class),
                        String.class
                );
                CallSite apply = LambdaMetafactory.metafactory(
                        stringCaller,
                        "apply",
                        MethodType.methodType(Function.class),
                        value.type().generic(),
                        value,
                        value.type()
                );
                stringValue = (Function<String, byte[]>) apply.getTarget().invokeExact();
            }
        } catch (Throwable ignored) {
            // ignored
        }

        if (stringCreatorJDK11 == null
                && unsafeSupport
                && (compact_strings == null || compact_strings.booleanValue())
                && !openj9
        ) {
            stringCreatorJDK11 = ((Supplier<BiFunction<byte[], Byte, String>>) () -> {
                try {
                    return (BiFunction<byte[], Byte, String>) new UnsafeUtils.UnsafeStringCreator();
                } catch (Throwable e) {
                    return null;
                }
            }).get();
        }

        STRING_CREATOR_JDK8 = stringCreatorJDK8;
        STRING_CREATOR_JDK11 = stringCreatorJDK11;
        STRING_CODER = stringCoder;
        STRING_VALUE = stringValue;
    }

    public static boolean isSQLDataSourceOrRowSet(Class<?> type) {
        return (CLASS_SQL_DATASOURCE != null && CLASS_SQL_DATASOURCE.isAssignableFrom(type))
                || (CLASS_SQL_ROW_SET != null && CLASS_SQL_ROW_SET.isAssignableFrom(type));
    }

    public static char[] getCharArray(String str) {
        // GraalVM not support
        // Android not support
        if (!FIELD_STRING_ERROR) {
            try {
                return (char[]) UnsafeUtils.UNSAFE.getObject(str, FIELD_STRING_VALUE_OFFSET);
            } catch (Exception ignored) {
                FIELD_STRING_ERROR = true;
            }
        }

        return str.toCharArray();
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
