package com.alibaba.fastjson2.util;

import sun.misc.Unsafe;

import java.lang.invoke.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteOrder;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.*;

import static java.lang.invoke.MethodType.methodType;

public class JDKUtils {
    public static final Unsafe UNSAFE;
    public static final long ARRAY_BYTE_BASE_OFFSET;
    public static final long ARRAY_CHAR_BASE_OFFSET;

    public static final int JVM_VERSION;
    public static final Byte LATIN1 = 0;
    public static final Byte UTF16 = 1;

    public static final Field FIELD_STRING_VALUE;
    public static final long FIELD_STRING_VALUE_OFFSET;
    public static volatile boolean FIELD_STRING_VALUE_ERROR;

    public static final long FIELD_DECIMAL_INT_COMPACT_OFFSET;
    public static final long FIELD_BIGINTEGER_MAG_OFFSET;

    public static final Field FIELD_STRING_CODER;
    public static final long FIELD_STRING_CODER_OFFSET;
    public static volatile boolean FIELD_STRING_CODER_ERROR;

    static final Class<?> CLASS_SQL_DATASOURCE;
    static final Class<?> CLASS_SQL_ROW_SET;
    public static final boolean HAS_SQL;
    public static final boolean ANDROID;
    public static final boolean GRAAL;
    public static final boolean OPENJ9;

    // Android not support
    public static final Class CLASS_TRANSIENT;
    public static final boolean BIG_ENDIAN;

    public static final boolean UNSAFE_SUPPORT = true;
    public static final boolean VECTOR_SUPPORT;
    public static final int VECTOR_BIT_LENGTH;

    // GraalVM not support
    // Android not support
    public static final BiFunction<char[], Boolean, String> STRING_CREATOR_JDK8;
    public static final BiFunction<byte[], Byte, String> STRING_CREATOR_JDK11;
    public static final ToIntFunction<String> STRING_CODER;
    public static final Function<String, byte[]> STRING_VALUE;

    public static final MethodHandle METHOD_HANDLE_HAS_NEGATIVE;
    public static final Predicate<byte[]> PREDICATE_IS_ASCII;

    static final MethodHandles.Lookup IMPL_LOOKUP;
    static volatile MethodHandle CONSTRUCTOR_LOOKUP;
    static volatile boolean CONSTRUCTOR_LOOKUP_ERROR;
    static volatile Throwable initErrorLast;
    static volatile Throwable reflectErrorLast;
    static final AtomicInteger reflectErrorCount = new AtomicInteger();

    static {
        Unsafe unsafe = null;
        long offset = -1, charOffset = -1;
        try {
            Field theUnsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafeField.setAccessible(true);
            unsafe = (Unsafe) theUnsafeField.get(null);
            offset = Unsafe.ARRAY_BYTE_BASE_OFFSET;
            charOffset = Unsafe.ARRAY_CHAR_BASE_OFFSET;
        } catch (Throwable ignored) {
            // ignored
        }
        UNSAFE = unsafe;
        ARRAY_BYTE_BASE_OFFSET = offset;
        ARRAY_CHAR_BASE_OFFSET = charOffset;

        int jvmVersion = -1;
        boolean openj9 = false, android = false, graal = false;
        try {
            String jmvName = System.getProperty("java.vm.name");
            openj9 = jmvName.contains("OpenJ9");
            android = jmvName.equals("Dalvik");
            graal = System.getProperty("org.graalvm.nativeimage.imagecode") != null;
            if (openj9 || android || graal) {
                FIELD_STRING_VALUE_ERROR = true;
            }

            String javaSpecVer = System.getProperty("java.specification.version");
            // android is 0.9
            if (javaSpecVer.startsWith("1.")) {
                javaSpecVer = javaSpecVer.substring(2);
            }
            if (javaSpecVer.indexOf('.') == -1) {
                jvmVersion = Integer.parseInt(javaSpecVer);
            }
        } catch (Throwable e) {
            initErrorLast = e;
        }

        OPENJ9 = openj9;
        ANDROID = android;
        GRAAL = graal;

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
                fieldOffset = UNSAFE.objectFieldOffset(field);
            } catch (Exception ignored) {
                FIELD_STRING_VALUE_ERROR = true;
            }

            FIELD_STRING_VALUE = field;
            FIELD_STRING_VALUE_OFFSET = fieldOffset;

            FIELD_STRING_CODER = null;
            FIELD_STRING_CODER_OFFSET = -1;
            FIELD_STRING_CODER_ERROR = true;
        } else {
            Field fieldValue = null;
            long fieldValueOffset = -1;
            try {
                fieldValue = String.class.getDeclaredField("value");
                fieldValueOffset = UNSAFE.objectFieldOffset(fieldValue);
            } catch (Exception ignored) {
                FIELD_STRING_VALUE_ERROR = true;
            }
            FIELD_STRING_VALUE_OFFSET = fieldValueOffset;
            FIELD_STRING_VALUE = fieldValue;

            Field fieldCode = null;
            long fieldCodeOffset = -1;
            try {
                fieldCode = String.class.getDeclaredField("coder");
                fieldCodeOffset = UNSAFE.objectFieldOffset(fieldCode);
            } catch (Exception ignored) {
                FIELD_STRING_CODER_ERROR = true;
            }
            FIELD_STRING_CODER_OFFSET = fieldCodeOffset;
            FIELD_STRING_CODER = fieldCode;
        }

        {
            long fieldOffset = -1;
            try {
                Field field = BigDecimal.class.getDeclaredField("intCompact");
                fieldOffset = UNSAFE.objectFieldOffset(field);
            } catch (Throwable ignored) {
                // ignored
            }
            FIELD_DECIMAL_INT_COMPACT_OFFSET = fieldOffset;
        }

        {
            long fieldOffset = -1;
            try {
                Field field = BigInteger.class.getDeclaredField("mag");
                fieldOffset = UNSAFE.objectFieldOffset(field);
            } catch (Throwable ignored) {
                // ignored
            }
            FIELD_BIGINTEGER_MAG_OFFSET = fieldOffset;
        }

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
                long fieldOffset = UNSAFE.staticFieldOffset(implLookup);
                trustedLookup = (MethodHandles.Lookup) UNSAFE.getObject(lookupClass, fieldOffset);
            } catch (Throwable ignored) {
                // ignored
            }
            if (trustedLookup == null) {
                trustedLookup = MethodHandles.lookup();
            }
            IMPL_LOOKUP = trustedLookup;
        }

        int vector_bit_length = -1;
        boolean vector_support = false;
        try {
            if (JVM_VERSION >= 11) {
                Class<?> factorClass = Class.forName("java.lang.management.ManagementFactory");
                Class<?> runtimeMXBeanClass = Class.forName("java.lang.management.RuntimeMXBean");
                Method getRuntimeMXBean = factorClass.getMethod("getRuntimeMXBean");
                Object runtimeMXBean = getRuntimeMXBean.invoke(null);
                Method getInputArguments = runtimeMXBeanClass.getMethod("getInputArguments");
                List<String> inputArguments = (List<String>) getInputArguments.invoke(runtimeMXBean);
                vector_support = inputArguments.contains("--add-modules=jdk.incubator.vector");

                if (vector_support) {
                    Class<?> byteVectorClass = Class.forName("jdk.incubator.vector.ByteVector");
                    Class<?> vectorSpeciesClass = Class.forName("jdk.incubator.vector.VectorSpecies");
                    Field speciesMax = byteVectorClass.getField("SPECIES_MAX");
                    Object species = speciesMax.get(null);
                    Method lengthMethod = vectorSpeciesClass.getMethod("length");
                    int length = (Integer) lengthMethod.invoke(species);
                    vector_bit_length = length * 8;
                }
            }
        } catch (Throwable e) {
            initErrorLast = e;
        }
        VECTOR_SUPPORT = vector_support;
        VECTOR_BIT_LENGTH = vector_bit_length;

        {
            Predicate<byte[]> isAscii = null;
            // isASCII
            MethodHandle handle = null;
            Class<?> classStringCoding = null;
            if (JVM_VERSION >= 17) {
                try {
                    handle = trustedLookup.findStatic(
                            classStringCoding = String.class,
                            "isASCII",
                            MethodType.methodType(boolean.class, byte[].class)
                    );
                } catch (Throwable e) {
                    initErrorLast = e;
                }
            }

            if (handle == null && JVM_VERSION >= 11) {
                try {
                    classStringCoding = Class.forName("java.lang.StringCoding");
                    handle = trustedLookup.findStatic(
                            classStringCoding,
                            "isASCII",
                            MethodType.methodType(boolean.class, byte[].class)
                    );
                } catch (Throwable e) {
                    initErrorLast = e;
                }
            }

            if (handle != null) {
                try {
                    MethodHandles.Lookup lookup = trustedLookup(classStringCoding);
                    CallSite callSite = LambdaMetafactory.metafactory(
                            lookup,
                            "test",
                            methodType(Predicate.class),
                            methodType(boolean.class, Object.class),
                            handle,
                            methodType(boolean.class, byte[].class)
                    );
                    isAscii = (Predicate<byte[]>) callSite.getTarget().invokeExact();
                } catch (Throwable e) {
                    initErrorLast = e;
                }
            }

            PREDICATE_IS_ASCII = isAscii;
        }

        {
            MethodHandle handle = null;
            if (JVM_VERSION >= 11) {
                try {
                    Class<?> classStringCoding = Class.forName("java.lang.StringCoding");
                    handle = trustedLookup.findStatic(
                            classStringCoding,
                            "hasNegatives",
                            MethodType.methodType(boolean.class, byte[].class, int.class, int.class)
                    );
                } catch (Throwable e) {
                    initErrorLast = e;
                }
            }
            METHOD_HANDLE_HAS_NEGATIVE = handle;
        }

        Boolean compact_strings = null;
        try {
            if (JVM_VERSION == 8) {
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
            }

            boolean lookupLambda = false;
            if (JVM_VERSION > 8 && !android) {
                try {
                    Field compact_strings_field = String.class.getDeclaredField("COMPACT_STRINGS");
                    long fieldOffset = UNSAFE.staticFieldOffset(compact_strings_field);
                    compact_strings = UNSAFE.getBoolean(String.class, fieldOffset);
                } catch (Throwable e) {
                    initErrorLast = e;
                }
                lookupLambda = compact_strings != null && compact_strings;
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
                        methodType(byte.class, String.class)
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
                        methodType(Object.class, Object.class),
                        value,
                        methodType(byte[].class, String.class)
                );
                stringValue = (Function<String, byte[]>) apply.getTarget().invokeExact();
            }
        } catch (Throwable e) {
            initErrorLast = e;
        }

        if (stringCoder == null) {
            stringCoder = (str) -> 1;
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

    public static void setReflectErrorLast(Throwable error) {
        reflectErrorCount.incrementAndGet();
        reflectErrorLast = error;
    }

    public static char[] getCharArray(String str) {
        // GraalVM not support
        // Android not support
        if (!FIELD_STRING_VALUE_ERROR) {
            try {
                return (char[]) UNSAFE.getObject(str, FIELD_STRING_VALUE_OFFSET);
            } catch (Exception ignored) {
                FIELD_STRING_VALUE_ERROR = true;
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
                    int FULL_ACCESS_MASK = 31; // for IBM Open J9 JDK
                    return (MethodHandles.Lookup) constructor.invoke(
                            objectClass,
                            OPENJ9 ? FULL_ACCESS_MASK : TRUSTED
                    );
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
