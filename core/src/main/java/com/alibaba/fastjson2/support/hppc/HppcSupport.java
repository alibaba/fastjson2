package com.alibaba.fastjson2.support.hppc;

import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaders;
import com.alibaba.fastjson2.util.JDKUtils;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriters;

import java.lang.invoke.*;
import java.lang.reflect.Type;
import java.util.function.*;

public class HppcSupport {
    static volatile boolean hppcError;
    static final MethodType METHOD_TYPE_FUNCTION = MethodType.methodType(Function.class);
    static final MethodType METHOD_TYPE_OBJECT_OBJECT = MethodType.methodType(Object.class, Object.class);

    public static ObjectWriter getObjectWriter(Type objectType, Class objectClass) {
        if (hppcError) {
            return null;
        }

        String className = objectClass.getName();

        switch (className) {
            case "com.carrotsearch.hppc.ByteArrayList": {
                MethodHandles.Lookup lookup = JDKUtils.trustedLookup(objectClass);
                try {
                    MethodHandle methodHandle = lookup.findVirtual(objectClass, "toArray", MethodType.methodType(byte[].class));

                    CallSite callSite = LambdaMetafactory.metafactory(
                            lookup,
                            "apply",
                            METHOD_TYPE_FUNCTION,
                            METHOD_TYPE_OBJECT_OBJECT,
                            methodHandle,
                            MethodType.methodType(byte[].class, objectClass)
                    );
                    MethodHandle target = callSite.getTarget();
                    Function<Object, byte[]> function = (Function<Object, byte[]>) target.invokeExact();
                    return ObjectWriters.ofToByteArray(function);
                } catch (Throwable ignored) {
                    hppcError = true;
                    // ignored
                }
                break;
            }
            case "com.carrotsearch.hppc.ShortArrayList": {
                MethodHandles.Lookup lookup = JDKUtils.trustedLookup(objectClass);
                try {
                    MethodHandle methodHandle = lookup.findVirtual(objectClass, "toArray", MethodType.methodType(short[].class));

                    CallSite callSite = LambdaMetafactory.metafactory(
                            lookup,
                            "apply",
                            METHOD_TYPE_FUNCTION,
                            METHOD_TYPE_OBJECT_OBJECT,
                            methodHandle,
                            MethodType.methodType(short[].class, objectClass)
                    );
                    MethodHandle target = callSite.getTarget();
                    Function<Object, short[]> function = (Function<Object, short[]>) target.invokeExact();
                    return ObjectWriters.ofToShortArray(function);
                } catch (Throwable ignored) {
                    hppcError = true;
                    // ignored
                }
                break;
            }
            case "com.carrotsearch.hppc.IntArrayList":
            case "com.carrotsearch.hppc.IntHashSet": {
                MethodHandles.Lookup lookup = JDKUtils.trustedLookup(objectClass);
                try {
                    MethodHandle methodHandle = lookup.findVirtual(objectClass, "toArray", MethodType.methodType(int[].class));

                    CallSite callSite = LambdaMetafactory.metafactory(
                            lookup,
                            "apply",
                            METHOD_TYPE_FUNCTION,
                            METHOD_TYPE_OBJECT_OBJECT,
                            methodHandle,
                            MethodType.methodType(int[].class, objectClass)
                    );
                    MethodHandle target = callSite.getTarget();
                    Function<Object, int[]> function = (Function<Object, int[]>) target.invokeExact();
                    return ObjectWriters.ofToIntArray(function);
                } catch (Throwable ignored) {
                    hppcError = true;
                    // ignored
                }
                break;
            }
            case "com.carrotsearch.hppc.LongArrayList":
            case "com.carrotsearch.hppc.LongHashSet": {
                MethodHandles.Lookup lookup = JDKUtils.trustedLookup(objectClass);
                try {
                    MethodHandle methodHandle = lookup.findVirtual(objectClass, "toArray", MethodType.methodType(long[].class));

                    CallSite callSite = LambdaMetafactory.metafactory(
                            lookup,
                            "apply",
                            METHOD_TYPE_FUNCTION,
                            METHOD_TYPE_OBJECT_OBJECT,
                            methodHandle,
                            MethodType.methodType(long[].class, objectClass)
                    );
                    MethodHandle target = callSite.getTarget();
                    Function<Object, long[]> function = (Function<Object, long[]>) target.invokeExact();
                    return ObjectWriters.ofToLongArray(function);
                } catch (Throwable ignored) {
                    hppcError = true;
                    // ignored
                }
                break;
            }
            case "com.carrotsearch.hppc.CharArrayList":
            case "com.carrotsearch.hppc.CharHashSet": {
                MethodHandles.Lookup lookup = JDKUtils.trustedLookup(objectClass);
                try {
                    MethodHandle methodHandle = lookup.findVirtual(objectClass, "toArray", MethodType.methodType(char[].class));

                    CallSite callSite = LambdaMetafactory.metafactory(
                            lookup,
                            "apply",
                            METHOD_TYPE_FUNCTION,
                            METHOD_TYPE_OBJECT_OBJECT,
                            methodHandle,
                            MethodType.methodType(char[].class, objectClass)
                    );
                    MethodHandle target = callSite.getTarget();
                    Function<Object, char[]> function = (Function<Object, char[]>) target.invokeExact();
                    return ObjectWriters.ofToCharArray(function);
                } catch (Throwable ignored) {
                    hppcError = true;
                    // ignored
                }
                break;
            }
            case "com.carrotsearch.hppc.FloatArrayList": {
                MethodHandles.Lookup lookup = JDKUtils.trustedLookup(objectClass);
                try {
                    MethodHandle methodHandle = lookup.findVirtual(objectClass, "toArray", MethodType.methodType(float[].class));

                    CallSite callSite = LambdaMetafactory.metafactory(
                            lookup,
                            "apply",
                            METHOD_TYPE_FUNCTION,
                            METHOD_TYPE_OBJECT_OBJECT,
                            methodHandle,
                            MethodType.methodType(float[].class, objectClass)
                    );
                    MethodHandle target = callSite.getTarget();
                    Function<Object, float[]> function = (Function<Object, float[]>) target.invokeExact();
                    return ObjectWriters.ofToFloatArray(function);
                } catch (Throwable ignored) {
                    hppcError = true;
                    // ignored
                }
                break;
            }
            case "com.carrotsearch.hppc.DoubleArrayList": {
                MethodHandles.Lookup lookup = JDKUtils.trustedLookup(objectClass);
                try {
                    MethodHandle methodHandle = lookup.findVirtual(objectClass, "toArray", MethodType.methodType(double[].class));

                    CallSite callSite = LambdaMetafactory.metafactory(
                            lookup,
                            "apply",
                            METHOD_TYPE_FUNCTION,
                            METHOD_TYPE_OBJECT_OBJECT,
                            methodHandle,
                            MethodType.methodType(double[].class, objectClass)
                    );
                    MethodHandle target = callSite.getTarget();
                    Function<Object, double[]> function = (Function<Object, double[]>) target.invokeExact();
                    return ObjectWriters.ofToDoubleArray(function);
                } catch (Throwable ignored) {
                    hppcError = true;
                    // ignored
                }
                break;
            }
            case "com.carrotsearch.hppc.BitSet": {
                MethodHandles.Lookup lookup = JDKUtils.trustedLookup(objectClass);
                try {
                    MethodHandle sizeHandler = lookup.findVirtual(objectClass, "size", MethodType.methodType(long.class));
                    CallSite sizeCallSite = LambdaMetafactory.metafactory(
                            lookup,
                            "applyAsLong",
                            MethodType.methodType(ToLongFunction.class),
                            MethodType.methodType(long.class, Object.class),
                            sizeHandler,
                            MethodType.methodType(long.class, objectClass)
                    );
                    ToLongFunction functionSize = (ToLongFunction) sizeCallSite.getTarget().invokeExact();

                    MethodHandle getHandler = lookup.findVirtual(objectClass, "get", MethodType.methodType(boolean.class, int.class));
                    CallSite getCallSite = LambdaMetafactory.metafactory(
                            lookup,
                            "apply",
                            MethodType.methodType(BiFunction.class),
                            MethodType.methodType(Object.class, Object.class, Object.class),
                            getHandler,
                            MethodType.methodType(Boolean.class, objectClass, Integer.class)
                    );
                    BiFunction<Object, Integer, Boolean> functionGet
                            = (BiFunction<Object, Integer, Boolean>) getCallSite.getTarget().invokeExact();

                    return ObjectWriters.ofToBooleanArray(functionSize, functionGet);
                } catch (Throwable ignored) {
                    hppcError = true;
                    // ignored
                }
                break;
            }
            default:
                break;
        }
        return null;
    }

    public static ObjectReader getObjectReader(Class objectClass) {
        if (hppcError) {
            return null;
        }

        String className = objectClass.getName();

        switch (className) {
            case "com.carrotsearch.hppc.ByteArrayList": {
                MethodHandles.Lookup lookup = JDKUtils.trustedLookup(objectClass);
                try {
                    MethodHandle methodHandle = lookup.findStatic(objectClass, "from", MethodType.methodType(objectClass, byte[].class));

                    CallSite callSite = LambdaMetafactory.metafactory(
                            lookup,
                            "apply",
                            METHOD_TYPE_FUNCTION,
                            METHOD_TYPE_OBJECT_OBJECT,
                            methodHandle,
                            MethodType.methodType(objectClass, byte[].class)
                    );
                    MethodHandle target = callSite.getTarget();
                    Function<byte[], Object> function = (Function<byte[], Object>) target.invokeExact();
                    return ObjectReaders.fromByteArray(function);
                } catch (Throwable ignored) {
                    hppcError = true;
                    // ignored
                }
                break;
            }
            case "com.carrotsearch.hppc.ShortArrayList": {
                MethodHandles.Lookup lookup = JDKUtils.trustedLookup(objectClass);
                try {
                    MethodHandle methodHandle = lookup.findStatic(objectClass, "from", MethodType.methodType(objectClass, short[].class));

                    CallSite callSite = LambdaMetafactory.metafactory(
                            lookup,
                            "apply",
                            METHOD_TYPE_FUNCTION,
                            METHOD_TYPE_OBJECT_OBJECT,
                            methodHandle,
                            MethodType.methodType(objectClass, short[].class)
                    );
                    MethodHandle target = callSite.getTarget();
                    Function<short[], Object> function = (Function<short[], Object>) target.invokeExact();
                    return ObjectReaders.fromShortArray(function);
                } catch (Throwable ignored) {
                    hppcError = true;
                    // ignored
                }
                break;
            }
            case "com.carrotsearch.hppc.IntArrayList":
            case "com.carrotsearch.hppc.IntHashSet": {
                MethodHandles.Lookup lookup = JDKUtils.trustedLookup(objectClass);
                try {
                    MethodHandle methodHandle = lookup.findStatic(objectClass, "from", MethodType.methodType(objectClass, int[].class));

                    CallSite callSite = LambdaMetafactory.metafactory(
                            lookup,
                            "apply",
                            METHOD_TYPE_FUNCTION,
                            METHOD_TYPE_OBJECT_OBJECT,
                            methodHandle,
                            MethodType.methodType(objectClass, int[].class)
                    );
                    MethodHandle target = callSite.getTarget();
                    Function<int[], Object> function = (Function<int[], Object>) target.invokeExact();
                    return ObjectReaders.fromIntArray(function);
                } catch (Throwable ignored) {
                    hppcError = true;
                    // ignored
                }
                break;
            }
            case "com.carrotsearch.hppc.LongArrayList":
            case "com.carrotsearch.hppc.LongHashSet": {
                MethodHandles.Lookup lookup = JDKUtils.trustedLookup(objectClass);
                try {
                    MethodHandle methodHandle = lookup.findStatic(objectClass, "from", MethodType.methodType(objectClass, long[].class));

                    CallSite callSite = LambdaMetafactory.metafactory(
                            lookup,
                            "apply",
                            METHOD_TYPE_FUNCTION,
                            METHOD_TYPE_OBJECT_OBJECT,
                            methodHandle,
                            MethodType.methodType(objectClass, long[].class)
                    );
                    MethodHandle target = callSite.getTarget();
                    Function<long[], Object> function = (Function<long[], Object>) target.invokeExact();
                    return ObjectReaders.fromLongArray(function);
                } catch (Throwable ignored) {
                    hppcError = true;
                    // ignored
                }
                break;
            }
            case "com.carrotsearch.hppc.CharArrayList":
            case "com.carrotsearch.hppc.CharHashSet": {
                MethodHandles.Lookup lookup = JDKUtils.trustedLookup(objectClass);
                try {
                    MethodHandle methodHandle = lookup.findStatic(objectClass, "from", MethodType.methodType(objectClass, char[].class));

                    CallSite callSite = LambdaMetafactory.metafactory(
                            lookup,
                            "apply",
                            METHOD_TYPE_FUNCTION,
                            METHOD_TYPE_OBJECT_OBJECT,
                            methodHandle,
                            MethodType.methodType(objectClass, char[].class)
                    );
                    MethodHandle target = callSite.getTarget();
                    Function<char[], Object> function = (Function<char[], Object>) target.invokeExact();
                    return ObjectReaders.fromCharArray(function);
                } catch (Throwable ignored) {
                    hppcError = true;
                    // ignored
                }
                break;
            }
            case "com.carrotsearch.hppc.FloatArrayList": {
                MethodHandles.Lookup lookup = JDKUtils.trustedLookup(objectClass);
                try {
                    MethodHandle methodHandle = lookup.findStatic(objectClass, "from", MethodType.methodType(objectClass, float[].class));

                    CallSite callSite = LambdaMetafactory.metafactory(
                            lookup,
                            "apply",
                            METHOD_TYPE_FUNCTION,
                            METHOD_TYPE_OBJECT_OBJECT,
                            methodHandle,
                            MethodType.methodType(objectClass, float[].class)
                    );
                    MethodHandle target = callSite.getTarget();
                    Function<float[], Object> function = (Function<float[], Object>) target.invokeExact();
                    return ObjectReaders.fromFloatArray(function);
                } catch (Throwable ignored) {
                    hppcError = true;
                    // ignored
                }
                break;
            }
            case "com.carrotsearch.hppc.DoubleArrayList": {
                MethodHandles.Lookup lookup = JDKUtils.trustedLookup(objectClass);
                try {
                    MethodHandle methodHandle = lookup.findStatic(objectClass, "from", MethodType.methodType(objectClass, double[].class));

                    CallSite callSite = LambdaMetafactory.metafactory(
                            lookup,
                            "apply",
                            METHOD_TYPE_FUNCTION,
                            METHOD_TYPE_OBJECT_OBJECT,
                            methodHandle,
                            MethodType.methodType(objectClass, double[].class)
                    );
                    MethodHandle target = callSite.getTarget();
                    Function<double[], Object> function = (Function<double[], Object>) target.invokeExact();
                    return ObjectReaders.fromDoubleArray(function);
                } catch (Throwable ignored) {
                    hppcError = true;
                    // ignored
                }
                break;
            }
            default:
                break;
        }

        return null;
    }
}
