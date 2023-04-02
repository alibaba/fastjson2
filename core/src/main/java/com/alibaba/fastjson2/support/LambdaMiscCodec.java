package com.alibaba.fastjson2.support;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaders;
import com.alibaba.fastjson2.util.JDKUtils;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriters;

import java.lang.invoke.*;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.util.function.*;

import static com.alibaba.fastjson2.util.TypeUtils.*;
import static com.alibaba.fastjson2.util.TypeUtils.METHOD_TYPE_OBJECT_LONG;

public class LambdaMiscCodec {
    static volatile boolean hppcError;
    static volatile Throwable errorLast;

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
                try {
                    return ObjectWriters.ofToByteArray(
                            createFunction(
                                    objectClass.getMethod("toArray")
                            )
                    );
                } catch (NoSuchMethodException | SecurityException e) {
                    throw new JSONException("illegal state", e);
                }
            }
            case "gnu.trove.set.hash.TShortHashSet":
            case "gnu.trove.list.array.TShortArrayList":
            case "com.carrotsearch.hppc.ShortArrayList": {
                try {
                    return ObjectWriters.ofToShortArray(
                            createFunction(
                                    objectClass.getMethod("toArray")
                            )
                    );
                } catch (NoSuchMethodException | SecurityException e) {
                    throw new JSONException("illegal state", e);
                }
            }
            case "gnu.trove.list.array.TIntArrayList":
            case "gnu.trove.set.hash.TIntHashSet":
            case "com.carrotsearch.hppc.IntArrayList":
            case "com.carrotsearch.hppc.IntHashSet": {
                try {
                    return ObjectWriters.ofToIntArray(
                            createFunction(
                                    objectClass.getMethod("toArray")
                            )
                    );
                } catch (NoSuchMethodException | SecurityException e) {
                    throw new JSONException("illegal state", e);
                }
            }
            case "gnu.trove.list.array.TLongArrayList":
            case "gnu.trove.set.hash.TLongHashSet":
            case "com.carrotsearch.hppc.LongArrayList":
            case "com.carrotsearch.hppc.LongHashSet": {
                try {
                    return ObjectWriters.ofToLongArray(
                            createFunction(
                                    objectClass.getMethod("toArray")
                            )
                    );
                } catch (NoSuchMethodException | SecurityException e) {
                    throw new JSONException("illegal state", e);
                }
            }
            case "gnu.trove.list.array.TCharArrayList":
            case "com.carrotsearch.hppc.CharArrayList":
            case "com.carrotsearch.hppc.CharHashSet": {
                try {
                    return ObjectWriters.ofToCharArray(
                            createFunction(
                                    objectClass.getMethod("toArray")
                            )
                    );
                } catch (NoSuchMethodException | SecurityException e) {
                    throw new JSONException("illegal state", e);
                }
            }
            case "gnu.trove.list.array.TFloatArrayList":
            case "com.carrotsearch.hppc.FloatArrayList": {
                try {
                    return ObjectWriters.ofToFloatArray(
                            createFunction(
                                    objectClass.getMethod("toArray")
                            )
                    );
                } catch (NoSuchMethodException | SecurityException e) {
                    throw new JSONException("illegal state", e);
                }
            }
            case "gnu.trove.list.array.TDoubleArrayList":
            case "com.carrotsearch.hppc.DoubleArrayList": {
                try {
                    return ObjectWriters.ofToDoubleArray(
                            createFunction(
                                    objectClass.getMethod("toArray")
                            )
                    );
                } catch (NoSuchMethodException | SecurityException e) {
                    throw new JSONException("illegal state", e);
                }
            }
            case "com.carrotsearch.hppc.BitSet": {
                MethodHandles.Lookup lookup = JDKUtils.trustedLookup(objectClass);
                try {
                    ToLongFunction functionSize = createToLongFunction(
                            objectClass.getMethod("size")
                    );

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
            case "org.bson.types.Decimal128":
                try {
                    return ObjectWriters.ofToBigDecimal(
                            createFunction(
                                    objectClass.getMethod("bigDecimalValue")
                            )
                    );
                } catch (NoSuchMethodException | SecurityException e) {
                    throw new JSONException("illegal state", e);
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
                try {
                    return ObjectReaders.fromByteArray(
                            createFunction(
                                    objectClass.getMethod("from", byte[].class)
                            )
                    );
                } catch (NoSuchMethodException | SecurityException e) {
                    throw new JSONException("illegal state", e);
                }
            }
            case "com.carrotsearch.hppc.ShortArrayList": {
                try {
                    return ObjectReaders.fromShortArray(
                            createFunction(
                                    objectClass.getMethod("from", short[].class)
                            )
                    );
                } catch (NoSuchMethodException | SecurityException e) {
                    throw new JSONException("illegal state", e);
                }
            }
            case "com.carrotsearch.hppc.IntArrayList":
            case "com.carrotsearch.hppc.IntHashSet": {
                try {
                    return ObjectReaders.fromIntArray(
                            createFunction(
                                    objectClass.getMethod("from", int[].class)
                            )
                    );
                } catch (NoSuchMethodException | SecurityException e) {
                    throw new JSONException("illegal state", e);
                }
            }
            case "com.carrotsearch.hppc.LongArrayList":
            case "com.carrotsearch.hppc.LongHashSet": {
                try {
                    return ObjectReaders.fromLongArray(
                            createFunction(
                                    objectClass.getMethod("from", long[].class)
                            )
                    );
                } catch (NoSuchMethodException | SecurityException e) {
                    throw new JSONException("illegal state", e);
                }
            }
            case "com.carrotsearch.hppc.CharArrayList":
            case "com.carrotsearch.hppc.CharHashSet": {
                try {
                    return ObjectReaders.fromCharArray(
                            createFunction(
                                    objectClass.getMethod("from", char[].class)
                            )
                    );
                } catch (NoSuchMethodException | SecurityException e) {
                    throw new JSONException("illegal state", e);
                }
            }
            case "com.carrotsearch.hppc.FloatArrayList": {
                try {
                    return ObjectReaders.fromFloatArray(
                            createFunction(
                                    objectClass.getMethod("from", float[].class)
                            )
                    );
                } catch (NoSuchMethodException | SecurityException e) {
                    throw new JSONException("illegal state", e);
                }
            }
            case "com.carrotsearch.hppc.DoubleArrayList": {
                try {
                    return ObjectReaders.fromDoubleArray(
                            createFunction(
                                    objectClass.getMethod("from", double[].class)
                            )
                    );
                } catch (NoSuchMethodException | SecurityException e) {
                    throw new JSONException("illegal state", e);
                }
            }
            case "gnu.trove.set.hash.TByteHashSet":
            case "gnu.trove.stack.array.TByteArrayStack":
            case "gnu.trove.list.array.TByteArrayList": {
                try {
                    return ObjectReaders.fromByteArray(
                            createFunction(
                                    objectClass.getConstructor(byte[].class)
                            )
                    );
                } catch (NoSuchMethodException | SecurityException e) {
                    throw new JSONException("illegal state", e);
                }
            }
            case "gnu.trove.list.array.TCharArrayList": {
                try {
                    return ObjectReaders.fromCharArray(
                            createFunction(
                                    objectClass.getConstructor(char[].class)
                            )
                    );
                } catch (NoSuchMethodException | SecurityException e) {
                    throw new JSONException("illegal state", e);
                }
            }
            case "gnu.trove.set.hash.TShortHashSet":
            case "gnu.trove.list.array.TShortArrayList": {
                try {
                    return ObjectReaders.fromShortArray(
                            createFunction(
                                    objectClass.getConstructor(short[].class)
                            )
                    );
                } catch (NoSuchMethodException | SecurityException e) {
                    throw new JSONException("illegal state", e);
                }
            }
            case "gnu.trove.set.hash.TIntHashSet":
            case "gnu.trove.list.array.TIntArrayList": {
                try {
                    return ObjectReaders.fromIntArray(
                            createFunction(
                                    objectClass.getConstructor(int[].class)
                            )
                    );
                } catch (NoSuchMethodException | SecurityException e) {
                    throw new JSONException("illegal state", e);
                }
            }
            case "gnu.trove.set.hash.TLongHashSet":
            case "gnu.trove.list.array.TLongArrayList": {
                try {
                    return ObjectReaders.fromLongArray(
                            createFunction(
                                    objectClass.getConstructor(long[].class)
                            )
                    );
                } catch (NoSuchMethodException | SecurityException e) {
                    throw new JSONException("illegal state", e);
                }
            }
            case "gnu.trove.list.array.TFloatArrayList": {
                try {
                    return ObjectReaders.fromFloatArray(
                            createFunction(
                                    objectClass.getConstructor(float[].class)
                            )
                    );
                } catch (NoSuchMethodException | SecurityException e) {
                    throw new JSONException("illegal state", e);
                }
            }
            case "gnu.trove.list.array.TDoubleArrayList": {
                try {
                    return ObjectReaders.fromDoubleArray(
                            createFunction(
                                    objectClass.getConstructor(double[].class)
                            )
                    );
                } catch (NoSuchMethodException | SecurityException e) {
                    throw new JSONException("illegal state", e);
                }
            }
            case "org.bson.types.Decimal128":
                try {
                    return ObjectReaders.fromBigDecimal(
                            createFunction(
                                    objectClass.getConstructor(BigDecimal.class)
                            )
                    );
                } catch (NoSuchMethodException | SecurityException e) {
                    throw new JSONException("illegal state", e);
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
            errorLast = ignored;
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
                    MethodType.methodType(int.class, objectClass)
            );
            return (ToIntFunction) callSite.getTarget().invokeExact();
        } catch (Throwable ignored) {
            errorLast = ignored;
        }
        return new ReflectToIntFunction(method);
    }

    public static ToLongFunction createToLongFunction(Method method) {
        Class<?> objectClass = method.getDeclaringClass();
        try {
            MethodHandles.Lookup lookup = JDKUtils.trustedLookup(objectClass);
            MethodType methodType = MethodType.methodType(long.class);
            MethodHandle methodHandle
                    = lookup.findVirtual(
                    objectClass,
                    method.getName(),
                    methodType
            );
            CallSite callSite = LambdaMetafactory.metafactory(
                    lookup,
                    "applyAsLong",
                    METHOD_TYPE_TO_LONG_FUNCTION,
                    METHOD_TYPE_LONG_OBJECT,
                    methodHandle,
                    MethodType.methodType(long.class, objectClass)
            );
            return (ToLongFunction) callSite.getTarget().invokeExact();
        } catch (Throwable ignored) {
            errorLast = ignored;
        }
        return new ReflectToLongFunction(method);
    }

    public static Function createFunction(Constructor constructor) {
        try {
            Class<?> declaringClass = constructor.getDeclaringClass();
            MethodHandles.Lookup lookup = JDKUtils.trustedLookup(declaringClass);
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            Class<?> param0 = parameterTypes[0];

            MethodHandle methodHandle = lookup.findConstructor(
                    declaringClass,
                    MethodType.methodType(void.class, param0)
            );

            CallSite callSite = LambdaMetafactory.metafactory(
                    lookup,
                    "apply",
                    METHOD_TYPE_FUNCTION,
                    METHOD_TYPE_OBJECT_OBJECT,
                    methodHandle,
                    MethodType.methodType(declaringClass, param0)
            );
            return (Function) callSite.getTarget().invokeExact();
        } catch (Throwable ignored) {
            errorLast = ignored;
        }

        return new ConstructorFunction(constructor);
    }

    public static Supplier createSupplier(Method method) {
        try {
            Class<?> declaringClass = method.getDeclaringClass();
            Class objectClass = method.getReturnType();
            MethodHandles.Lookup lookup = JDKUtils.trustedLookup(declaringClass);
            MethodHandle methodHandle = lookup.findStatic(
                    declaringClass,
                    method.getName(),
                    MethodType.methodType(objectClass)
            );

            CallSite callSite = LambdaMetafactory.metafactory(
                    lookup,
                    "get",
                    METHOD_TYPE_SUPPLIER,
                    METHOD_TYPE_OBJECT,
                    methodHandle,
                    MethodType.methodType(objectClass)
            );
            return (Supplier) callSite.getTarget().invokeExact();
        } catch (Throwable ignored) {
            errorLast = ignored;
        }

        return new ReflectSupplier(method);
    }

    public static BiFunction createBiFunction(Method method) {
        try {
            Class<?> declaringClass = method.getDeclaringClass();
            Class objectClass = method.getReturnType();
            MethodHandles.Lookup lookup = JDKUtils.trustedLookup(declaringClass);
            Class<?>[] parameterTypes = method.getParameterTypes();
            Class<?> param0 = parameterTypes[0];

            MethodType methodType;
            MethodHandle methodHandle;
            if (Modifier.isStatic(method.getModifiers())) {
                Class<?> param1 = parameterTypes[1];

                methodHandle = lookup.findStatic(
                        declaringClass,
                        method.getName(),
                        MethodType.methodType(objectClass, param0, param1)
                );
                methodType = MethodType.methodType(objectClass, param0, param1);
            } else {
                methodHandle = lookup.findVirtual(
                        declaringClass,
                        method.getName(),
                        MethodType.methodType(objectClass, param0)
                );
                methodType = MethodType.methodType(objectClass, declaringClass, param0);
            }

            CallSite callSite = LambdaMetafactory.metafactory(
                    lookup,
                    "apply",
                    METHOD_TYPE_BI_FUNCTION,
                    METHOD_TYPE_OBJECT_OBJECT_OBJECT,
                    methodHandle,
                    methodType
            );
            return (BiFunction) callSite.getTarget().invokeExact();
        } catch (Throwable ignored) {
            errorLast = ignored;
        }

        return new ReflectBiFunction(method);
    }

    public static BiFunction createBiFunction(Constructor constructor) {
        try {
            Class<?> declaringClass = constructor.getDeclaringClass();
            MethodHandles.Lookup lookup = JDKUtils.trustedLookup(declaringClass);
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            Class<?> param0 = parameterTypes[0];
            Class<?> param1 = parameterTypes[1];

            MethodHandle methodHandle = lookup.findConstructor(
                    declaringClass,
                    MethodType.methodType(void.class, param0, param1)
            );

            CallSite callSite = LambdaMetafactory.metafactory(
                    lookup,
                    "apply",
                    METHOD_TYPE_BI_FUNCTION,
                    METHOD_TYPE_OBJECT_OBJECT_OBJECT,
                    methodHandle,
                    MethodType.methodType(declaringClass, param0, param1)
            );
            return (BiFunction) callSite.getTarget().invokeExact();
        } catch (Throwable ignored) {
            errorLast = ignored;
        }

        return new ConstructorBiFunction(constructor);
    }

    static final class ConstructorFunction
            implements Function {
        final Constructor constructor;

        ConstructorFunction(Constructor constructor) {
            this.constructor = constructor;
        }

        @Override
        public Object apply(Object arg0) {
            try {
                return constructor.newInstance(arg0);
            } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
                throw new JSONException("invoke error", e);
            }
        }
    }

    static final class ConstructorBiFunction
            implements BiFunction {
        final Constructor constructor;

        ConstructorBiFunction(Constructor constructor) {
            this.constructor = constructor;
        }

        @Override
        public Object apply(Object arg0, Object arg1) {
            try {
                return constructor.newInstance(arg0, arg1);
            } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
                throw new JSONException("invoke error", e);
            }
        }
    }

    static final class ReflectBiFunction
            implements BiFunction {
        final Method method;

        ReflectBiFunction(Method method) {
            this.method = method;
        }

        @Override
        public Object apply(Object arg0, Object arg1) {
            try {
                if (Modifier.isStatic(method.getModifiers())) {
                    return method.invoke(null, arg0, arg1);
                } else {
                    return method.invoke(arg0, arg1);
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new JSONException("invoke error", e);
            }
        }
    }

    static final class ReflectSupplier
            implements Supplier {
        final Method method;

        ReflectSupplier(Method method) {
            this.method = method;
        }

        @Override
        public Object get() {
            try {
                return method.invoke(null);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new JSONException("invoke error", e);
            }
        }
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
            errorLast = ignored;
        }
        return new FactoryFunction(method);
    }

    public static ObjIntConsumer createObjIntConsumer(Method method) {
        Class<?> declaringClass = method.getDeclaringClass();
        try {
            MethodHandles.Lookup lookup = JDKUtils.trustedLookup(declaringClass);
            MethodType methodType = MethodType.methodType(void.class, int.class);
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
                    MethodType.methodType(void.class, declaringClass, int.class)
            );
            return (ObjIntConsumer) callSite.getTarget().invokeExact();
        } catch (Throwable ignored) {
            errorLast = ignored;
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
                throw new JSONException("applyAsInt error", e);
            }
        }
    }

    static final class ReflectToLongFunction
            implements ToLongFunction {
        final Method method;

        public ReflectToLongFunction(Method method) {
            this.method = method;
        }

        public long applyAsLong(Object object) {
            try {
                return (Long) method.invoke(object);
            } catch (Exception e) {
                throw new JSONException("applyAsLong error", e);
            }
        }
    }
}
