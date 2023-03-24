package com.alibaba.fastjson2.support;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaders;
import com.alibaba.fastjson2.util.JDKUtils;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriters;

import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.function.*;

import static com.alibaba.fastjson2.util.TypeUtils.*;
import static com.alibaba.fastjson2.util.TypeUtils.METHOD_TYPE_OBJECT_LONG;

public class LambdaMiscCodec {
    static volatile boolean hppcError;

    public static ObjectWriter getObjectWriter(Type objectType, Class objectClass) {
        if (hppcError) {
            return null;
        }

        String className = objectClass.getName();

        switch (className) {
            case "gnu.trove.set.hash.TByteHashSet":
            case "gnu.trove.stack.array.TByteArrayStack":
            case "gnu.trove.list.array.TByteArrayList":
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
            case "gnu.trove.set.hash.TShortHashSet":
            case "gnu.trove.list.array.TShortArrayList":
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
            case "gnu.trove.list.array.TIntArrayList":
            case "gnu.trove.set.hash.TIntHashSet":
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
            case "gnu.trove.list.array.TLongArrayList":
            case "gnu.trove.set.hash.TLongHashSet":
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
            case "gnu.trove.list.array.TCharArrayList":
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
            case "gnu.trove.list.array.TFloatArrayList":
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
            case "gnu.trove.list.array.TDoubleArrayList":
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
            case "gnu.trove.set.hash.TByteHashSet":
            case "gnu.trove.stack.array.TByteArrayStack":
            case "gnu.trove.list.array.TByteArrayList": {
                MethodHandles.Lookup lookup = JDKUtils.trustedLookup(objectClass);
                try {
                    MethodHandle methodHandle = lookup.findConstructor(objectClass, MethodType.methodType(void.class, byte[].class));

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
            case "gnu.trove.list.array.TCharArrayList": {
                MethodHandles.Lookup lookup = JDKUtils.trustedLookup(objectClass);
                try {
                    MethodHandle methodHandle = lookup.findConstructor(objectClass, MethodType.methodType(void.class, char[].class));

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
            case "gnu.trove.set.hash.TShortHashSet":
            case "gnu.trove.list.array.TShortArrayList": {
                MethodHandles.Lookup lookup = JDKUtils.trustedLookup(objectClass);
                try {
                    MethodHandle methodHandle = lookup.findConstructor(objectClass, MethodType.methodType(void.class, short[].class));

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
            case "gnu.trove.set.hash.TIntHashSet":
            case "gnu.trove.list.array.TIntArrayList": {
                MethodHandles.Lookup lookup = JDKUtils.trustedLookup(objectClass);
                try {
                    MethodHandle methodHandle = lookup.findConstructor(objectClass, MethodType.methodType(void.class, int[].class));

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
            case "gnu.trove.set.hash.TLongHashSet":
            case "gnu.trove.list.array.TLongArrayList": {
                MethodHandles.Lookup lookup = JDKUtils.trustedLookup(objectClass);
                try {
                    MethodHandle methodHandle = lookup.findConstructor(objectClass, MethodType.methodType(void.class, long[].class));

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
            case "gnu.trove.list.array.TFloatArrayList": {
                MethodHandles.Lookup lookup = JDKUtils.trustedLookup(objectClass);
                try {
                    MethodHandle methodHandle = lookup.findConstructor(objectClass, MethodType.methodType(void.class, float[].class));

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
            case "gnu.trove.list.array.TDoubleArrayList": {
                MethodHandles.Lookup lookup = JDKUtils.trustedLookup(objectClass);
                try {
                    MethodHandle methodHandle = lookup.findConstructor(objectClass, MethodType.methodType(void.class, double[].class));

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

    public static LongFunction createLongFunction(Constructor constructor) {
        try {
            Class objectClass = constructor.getDeclaringClass();
            MethodHandles.Lookup lookup = JDKUtils.trustedLookup(objectClass);
            MethodHandle methodHandle
                    = lookup.findConstructor(
                    objectClass,
                    METHOD_TYPE_VOID_LONG
            );
            MethodType invokedType = MethodType.methodType(objectClass, long.class);
            CallSite callSite = LambdaMetafactory.metafactory(
                    lookup,
                    "apply",
                    METHOD_TYPE_LONG_FUNCTION,
                    METHOD_TYPE_OBJECT_LONG,
                    methodHandle,
                    invokedType
            );
            return (LongFunction) callSite.getTarget().invokeExact();
        } catch (Throwable ignored) {
            // ignored
        }
        return new ReflectLongFunction(constructor);
    }

    public static ToIntFunction createToIntFunction(Method method) {
        Class<?> objectClass = method.getDeclaringClass();
        try {
            MethodHandles.Lookup lookup = JDKUtils.trustedLookup(objectClass);
            MethodType methodType = MethodType.methodType(int.class);
            MethodHandle methodHandle
                    = lookup.findVirtual(
                    objectClass,
                    method.getName(),
                    methodType
            );
            CallSite callSite = LambdaMetafactory.metafactory(
                    lookup,
                    "applyAsInt",
                    METHOD_TYPE_TO_INT_FUNCTION,
                    METHOD_TYPE_INT_OBJECT,
                    methodHandle,
                    methodType
            );
            return (ToIntFunction) callSite.getTarget().invokeExact();
        } catch (Throwable ignored) {
            // ignored
        }
        return new ReflectToIntFunction(method);
    }

    public static Function createFunction(Method method) {
        Class<?> declaringClass = method.getDeclaringClass();
        int modifiers = method.getModifiers();
        Class<?>[] parameterTypes = method.getParameterTypes();

        boolean isStatic = Modifier.isStatic(modifiers);
        Class objectClass = method.getReturnType();
        Class paramClass;
        if (parameterTypes.length == 1 && isStatic) {
            paramClass = parameterTypes[0];
        } else if (parameterTypes.length == 0 && !isStatic) {
            paramClass = declaringClass;
        } else {
            throw new JSONException("not support parameters " + method);
        }

        try {
            MethodHandles.Lookup lookup = JDKUtils.trustedLookup(declaringClass);
            MethodHandle methodHandle;
            if (isStatic) {
                methodHandle = lookup.findStatic(
                        declaringClass,
                        method.getName(),
                        MethodType.methodType(objectClass, paramClass)
                );
            } else {
                methodHandle = lookup.findVirtual(
                        declaringClass,
                        method.getName(),
                        MethodType.methodType(objectClass)
                );
            }
            CallSite callSite = LambdaMetafactory.metafactory(
                    lookup,
                    "apply",
                    METHOD_TYPE_FUNCTION,
                    METHOD_TYPE_OBJECT_OBJECT,
                    methodHandle,
                    MethodType.methodType(objectClass, paramClass)
            );
            return (Function) callSite.getTarget().invokeExact();
        } catch (Throwable ignored) {
            // ignored
        }
        return new FactoryFunction(method);
    }

    public static ObjIntConsumer createObjIntConsumer(Method method) {
        Class<?> declaringClass = method.getDeclaringClass();
        try {
            MethodHandles.Lookup lookup = JDKUtils.trustedLookup(declaringClass);
            MethodType methodType = MethodType.methodType(int.class);
            MethodHandle methodHandle
                    = lookup.findVirtual(
                    declaringClass,
                    method.getName(),
                    methodType
            );
            CallSite callSite = LambdaMetafactory.metafactory(
                    lookup,
                    "accept",
                    METHOD_TYPE_OBJECT_INT_CONSUMER,
                    METHOD_TYPE_VOID_OBJECT_INT,
                    methodHandle,
                    methodType
            );
            return (ObjIntConsumer) callSite.getTarget().invokeExact();
        } catch (Throwable ignored) {
            // ignored
        }
        return new ReflectObjIntConsumer(method);
    }

    static final class ReflectObjIntConsumer
            implements ObjIntConsumer {
        final Method method;

        public ReflectObjIntConsumer(Method method) {
            this.method = method;
        }

        @Override
        public void accept(Object object, int value) {
            try {
                method.invoke(object, value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new JSONException("invoke error", e);
            }
        }
    }

    static final class FactoryFunction
            implements Function {
        final Method method;

        FactoryFunction(Method method) {
            this.method = method;
        }

        @Override
        public Object apply(Object arg) {
            try {
                return method.invoke(null, arg);
            } catch (Exception e) {
                throw new JSONException("createInstance error", e);
            }
        }
    }

    static final class GetterFunction
            implements Function {
        final Method method;

        GetterFunction(Method method) {
            this.method = method;
        }

        @Override
        public Object apply(Object object) {
            try {
                return method.invoke(object);
            } catch (Exception e) {
                throw new JSONException("createInstance error", e);
            }
        }
    }

    static final class ReflectLongFunction
            implements LongFunction {
        final Constructor constructor;

        public ReflectLongFunction(Constructor constructor) {
            this.constructor = constructor;
        }

        @Override
        public Object apply(long value) {
            try {
                return constructor.newInstance(value);
            } catch (Exception e) {
                throw new JSONException("createInstance error", e);
            }
        }
    }

    static final class ReflectToIntFunction
            implements ToIntFunction {
        final Method method;

        public ReflectToIntFunction(Method method) {
            this.method = method;
        }

        public int applyAsInt(Object object) {
            try {
                return (Integer) method.invoke(object);
            } catch (Exception e) {
                throw new JSONException("createInstance error", e);
            }
        }
    }
}
