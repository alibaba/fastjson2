package com.alibaba.fastjson2.util;

import java.lang.invoke.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.ByteOrder;
import java.util.function.*;

import static java.lang.invoke.MethodType.methodType;

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
    public static final boolean ANDROID;

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

    static final MethodHandles.Lookup IMPL_LOOKUP;
    static final boolean OPEN_J9;
    static volatile MethodHandle CONSTRUCTOR_LOOKUP;
    static volatile boolean CONSTRUCTOR_LOOKUP_ERROR;
    static volatile Throwable initErrorLast;

    static {
        int jvmVersion = -1;
        boolean openj9 = false, android = false;
        try {
            String jmvName = System.getProperty("java.vm.name");
            openj9 = jmvName.contains("OpenJ9");
            android = jmvName.equals("Dalvik");
            if (openj9 || android) {
                FIELD_STRING_ERROR = true;
            }

            String javaSpecVer = System.getProperty("java.specification.version");
            // android is 0.9
            if (javaSpecVer.startsWith("1.")) {
                javaSpecVer = javaSpecVer.substring(2);
            }
            if (javaSpecVer.indexOf('.') == -1) {
                jvmVersion = Integer.parseInt(javaSpecVer);
            }
        } catch (Throwable ignored) {
            initErrorLast = ignored;
        }

        OPEN_J9 = openj9;
        ANDROID = android;

        boolean hasJavaSql = true;
        Class dataSourceClass = null;
        Class rowSetClass = null;
        try {
            dataSourceClass = Class.forName("javax.sql.DataSource");
            rowSetClass = Class.forName("javax.sql.RowSet");
        } catch (Throwable ignored) {
            hasJavaSql = false;
        }
        CLASS_SQL_DATASOURCE = dataSourceClass;
        CLASS_SQL_ROW_SET = rowSetClass;
        HAS_SQL = hasJavaSql;

        Class transientClass = null;
        if (!android) {
            try {
                transientClass = Class.forName("java.beans.Transient");
            } catch (Throwable ignored) {
            }
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

        MethodHandles.Lookup trustedLookup = null;
        {
            try {
                Class lookupClass = MethodHandles.Lookup.class;
                Field implLookup = lookupClass.getDeclaredField("IMPL_LOOKUP");
                long fieldOffset = UnsafeUtils.UNSAFE.staticFieldOffset(implLookup);
                trustedLookup = (MethodHandles.Lookup) UnsafeUtils.UNSAFE.getObject(lookupClass, fieldOffset);
            } catch (Throwable ignored) {
                // ignored
            }
            if (trustedLookup == null) {
                trustedLookup = MethodHandles.lookup();
            }
            IMPL_LOOKUP = trustedLookup;
        }

        Boolean compact_strings = null;
        try {
            if (JVM_VERSION == 8 && trustedLookup != null) {
                MethodHandles.Lookup lookup = trustedLookup(String.class);

                MethodHandle handle = lookup.findConstructor(
                        String.class, methodType(void.class, char[].class, boolean.class)
                );

                CallSite callSite = LambdaMetafactory.metafactory(
                        lookup,
                        "apply",
                        methodType(BiFunction.class),
                        methodType(Object.class, Object.class, Object.class),
                        handle,
                        methodType(String.class, char[].class, boolean.class)
                );
                stringCreatorJDK8 = (BiFunction<char[], Boolean, String>) callSite.getTarget().invokeExact();
                stringCoder = (str) -> 1;
            }

            boolean lookupLambda = false;
            if (JVM_VERSION > 8 && trustedLookup != null && !android) {
                try {
                    Field compact_strings_field = String.class.getDeclaredField("COMPACT_STRINGS");
                    if (compact_strings_field != null) {
                        if (UNSAFE_SUPPORT) {
                            long fieldOffset = UnsafeUtils.UNSAFE.staticFieldOffset(compact_strings_field);
                            compact_strings = UnsafeUtils.UNSAFE.getBoolean(String.class, fieldOffset);
                        } else {
                            compact_strings_field.setAccessible(true);
                            compact_strings = (Boolean) compact_strings_field.get(null);
                        }
                    }
                } catch (Throwable ignored) {
                    initErrorLast = ignored;
                }
                lookupLambda = compact_strings != null && compact_strings.booleanValue();
            }

            if (lookupLambda) {
                MethodHandles.Lookup lookup = trustedLookup.in(String.class);
                MethodHandle handle = lookup.findConstructor(
                        String.class, methodType(void.class, byte[].class, byte.class)
                );
                CallSite callSite = LambdaMetafactory.metafactory(
                        lookup,
                        "apply",
                        methodType(BiFunction.class),
                        methodType(Object.class, Object.class, Object.class),
                        handle,
                        methodType(String.class, byte[].class, Byte.class)
                );
                stringCreatorJDK11 = (BiFunction<byte[], Byte, String>) callSite.getTarget().invokeExact();

                MethodHandle coder = lookup.findSpecial(
                        String.class,
                        "coder",
                        methodType(byte.class),
                        String.class
                );
                CallSite applyAsInt = LambdaMetafactory.metafactory(
                        lookup,
                        "applyAsInt",
                        methodType(ToIntFunction.class),
                        methodType(int.class, Object.class),
                        coder,
                        coder.type()
                );
                stringCoder = (ToIntFunction<String>) applyAsInt.getTarget().invokeExact();

                MethodHandle value = lookup.findSpecial(
                        String.class,
                        "value",
                        methodType(byte[].class),
                        String.class
                );
                CallSite apply = LambdaMetafactory.metafactory(
                        lookup,
                        "apply",
                        methodType(Function.class),
                        value.type().generic(),
                        value,
                        value.type()
                );
                stringValue = (Function<String, byte[]>) apply.getTarget().invokeExact();
            }
        } catch (Throwable ignored) {
            initErrorLast = ignored;
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

    public static MethodHandles.Lookup trustedLookup(Class objectClass) {
        if (!CONSTRUCTOR_LOOKUP_ERROR) {
            try {
                int TRUSTED = -1;

                MethodHandle constructor = CONSTRUCTOR_LOOKUP;
                if (JVM_VERSION < 15) {
                    if (constructor == null) {
                        constructor = IMPL_LOOKUP.findConstructor(
                                MethodHandles.Lookup.class,
                                methodType(void.class, Class.class, int.class)
                        );
                        CONSTRUCTOR_LOOKUP = constructor;
                    }
                    int allowedModes;
                    if (OPEN_J9) {
                        // int INTERNAL_PRIVILEGED = 0x80;
                        final int PACKAGE = 0x8;
                        final int MODULE = 0x10;
                        int FULL_ACCESS_MASK = Modifier.PUBLIC | Modifier.PRIVATE | Modifier.PROTECTED
                                | PACKAGE | MODULE;
                        allowedModes = FULL_ACCESS_MASK;
                    } else {
                        allowedModes = TRUSTED;
                    }
                    return (MethodHandles.Lookup) constructor.invoke(objectClass, allowedModes);
                } else {
                    if (constructor == null) {
                        constructor = IMPL_LOOKUP.findConstructor(
                                MethodHandles.Lookup.class,
                                methodType(void.class, Class.class, Class.class, int.class)
                        );
                        CONSTRUCTOR_LOOKUP = constructor;
                    }
                    return (MethodHandles.Lookup) constructor.invoke(objectClass, null, TRUSTED);
                }
            } catch (Throwable ignored) {
                CONSTRUCTOR_LOOKUP_ERROR = true;
            }
        }

        return IMPL_LOOKUP.in(objectClass);
    }
}
