package com.alibaba.fastjson2.internal;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.function.*;
import com.alibaba.fastjson2.util.JDKUtils;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.function.*;

import static com.alibaba.fastjson2.util.JDKUtils.UNSAFE;

@SuppressWarnings("ALL")
public final class PropertyAccessorFactoryUnsafe
        extends PropertyAccessorFactory {
    protected PropertyAccessor createInternal(Field field) {
        if (field.getType() == byte.class) {
            return new FieldAccessorUnsafeByte(field);
        }
        if (field.getType() == short.class) {
            return new FieldAccessorUnsafeShort(field);
        }
        if (field.getType() == int.class) {
            return new FieldAccessorUnsafeInt(field);
        }
        if (field.getType() == long.class) {
            return new FieldAccessorUnsafeLong(field);
        }
        if (field.getType() == float.class) {
            return new FieldAccessorUnsafeFloat(field);
        }
        if (field.getType() == double.class) {
            return new FieldAccessorUnsafeDouble(field);
        }
        if (field.getType() == boolean.class) {
            return new FieldAccessorUnsafeBoolean(field);
        }
        if (field.getType() == char.class) {
            return new FieldAccessorUnsafeChar(field);
        }
        return new FieldAccessorUnsafeObject(field);
    }

    abstract static class FieldAccessorUnsafe extends FieldAccessor {
        final long fieldOffset;
        public FieldAccessorUnsafe(Field field) {
            super(field);
            this.fieldOffset = UNSAFE.objectFieldOffset(field);
        }
    }

    static final class FieldAccessorUnsafeBoolean extends FieldAccessorUnsafe implements PropertyAccessorBoolean {
        public FieldAccessorUnsafeBoolean(Field field) {
            super(field);
        }

        @Override
        public boolean getBoolean(Object object) {
            return UNSAFE.getBoolean(Objects.requireNonNull(object), fieldOffset);
        }

        @Override
        public void setBoolean(Object object, boolean value) {
            UNSAFE.putBoolean(Objects.requireNonNull(object), fieldOffset, value);
        }
    }

    static final class FieldAccessorUnsafeByte extends FieldAccessorUnsafe implements PropertyAccessorByte {
        public FieldAccessorUnsafeByte(Field field) {
            super(field);
        }

        @Override
        public byte getByte(Object object) {
            return UNSAFE.getByte(Objects.requireNonNull(object), fieldOffset);
        }

        @Override
        public void setByte(Object object, byte value) {
            UNSAFE.putByte(Objects.requireNonNull(object), fieldOffset, value);
        }
    }

    static final class FieldAccessorUnsafeShort extends FieldAccessorUnsafe implements PropertyAccessorShort {
        public FieldAccessorUnsafeShort(Field field) {
            super(field);
        }

        @Override
        public short getShort(Object object) {
            return UNSAFE.getShort(Objects.requireNonNull(object), fieldOffset);
        }

        @Override
        public void setShort(Object object, short value) {
            UNSAFE.putShort(Objects.requireNonNull(object), fieldOffset, value);
        }
    }

    static final class FieldAccessorUnsafeInt extends FieldAccessorUnsafe implements PropertyAccessorInt {
        public FieldAccessorUnsafeInt(Field field) {
            super(field);
        }

        @Override
        public int getInt(Object object) {
            return UNSAFE.getInt(Objects.requireNonNull(object), fieldOffset);
        }

        @Override
        public void setInt(Object object, int value) {
            UNSAFE.putInt(Objects.requireNonNull(object), fieldOffset, value);
        }
    }

    static final class FieldAccessorUnsafeLong extends FieldAccessorUnsafe implements PropertyAccessorLong {
        public FieldAccessorUnsafeLong(Field field) {
            super(field);
        }

        @Override
        public long getLong(Object object) {
            return UNSAFE.getLong(Objects.requireNonNull(object), fieldOffset);
        }

        @Override
        public void setLong(Object object, long value) {
            UNSAFE.putLong(Objects.requireNonNull(object), fieldOffset, value);
        }
    }

    static final class FieldAccessorUnsafeFloat extends FieldAccessorUnsafe implements PropertyAccessorFloat {
        public FieldAccessorUnsafeFloat(Field field) {
            super(field);
        }

        @Override
        public float getFloat(Object object) {
            return UNSAFE.getFloat(Objects.requireNonNull(object), fieldOffset);
        }

        @Override
        public void setFloat(Object object, float value) {
            UNSAFE.putFloat(Objects.requireNonNull(object), fieldOffset, value);
        }
    }

    static final class FieldAccessorUnsafeDouble extends FieldAccessorUnsafe implements PropertyAccessorDouble {
        public FieldAccessorUnsafeDouble(Field field) {
            super(field);
        }

        @Override
        public double getDouble(Object object) {
            return UNSAFE.getDouble(Objects.requireNonNull(object), fieldOffset);
        }

        @Override
        public void setDouble(Object object, double value) {
            UNSAFE.putDouble(Objects.requireNonNull(object), fieldOffset, value);
        }
    }

    static final class FieldAccessorUnsafeChar extends FieldAccessorUnsafe implements PropertyAccessorChar {
        public FieldAccessorUnsafeChar(Field field) {
            super(field);
        }

        @Override
        public char getChar(Object object) {
            return UNSAFE.getChar(Objects.requireNonNull(object), fieldOffset);
        }

        @Override
        public void setChar(Object object, char value) {
            UNSAFE.putChar(Objects.requireNonNull(object), fieldOffset, value);
        }
    }

    static final class FieldAccessorUnsafeObject extends FieldAccessorUnsafe implements PropertyAccessorObject {
        public FieldAccessorUnsafeObject(Field field) {
            super(field);
        }

        @Override
        public Object getObject(Object object) {
            return UNSAFE.getObject(Objects.requireNonNull(object), fieldOffset);
        }

        @Override
        public void setObject(Object object, Object value) {
            UNSAFE.putObject(Objects.requireNonNull(object), fieldOffset, typeCheck(value));
        }

        private Object typeCheck(Object value) {
            if (value == null || propertyClass.isAssignableFrom(value.getClass())) {
                return value;
            }
            throw typeCheckError(value);
        }

        private JSONException typeCheckError(Object value) {
            return new JSONException("set " + name() + " error, type not support " + value.getClass());
        }
    }

    public PropertyAccessor create(String name, Method getter, Method setter) {
        return super.create(name, null, null, getter, setter);
    }

    public PropertyAccessor create(String name, Class<?> propertyClass, Type propertyType, Method getter, Method setter) {
        if (propertyClass == null) {
            if (getter != null) {
                propertyClass = getter.getReturnType();
            } else {
                propertyClass = setter.getParameterTypes()[0];
            }
        }

        Class<?> declaringClass;
        if (getter != null) {
            declaringClass = getter.getDeclaringClass();
        } else {
            declaringClass = setter.getDeclaringClass();
        }
        boolean lambda = declaringClass.getName().contains("$$Lambda");

        if (!lambda && (setter == null || !isChainableSetter(setter))) {
            if (propertyClass == boolean.class) {
                return create(name, getBoolean(getter), setBoolean(setter));
            }
            if (JDKUtils.JVM_VERSION == 8) {
                if (propertyClass == byte.class) {
                    return create(name, getByte(getter), setByte(setter));
                }
                if (propertyClass == short.class) {
                    return create(name, getShort(getter), setShort(setter));
                }
                if (propertyClass == char.class) {
                    return create(name, getChar(getter), setChar(setter));
                }
            }
            if (propertyClass == int.class) {
                return create(name, getInt(getter), setInt(setter));
            }
            if (propertyClass == long.class) {
                return create(name, getLong(getter), setLong(setter));
            }
            if (propertyClass == float.class) {
                return create(name, getFloat(getter), setFloat(setter));
            }
            if (propertyClass == double.class) {
                return create(name, getDouble(getter), setDouble(setter));
            }
            if (!propertyClass.isPrimitive()) {
                if (propertyType == null) {
                    if (getter != null) {
                        propertyType = getter.getGenericReturnType();
                    } else {
                        propertyType = setter.getGenericParameterTypes()[0];
                    }
                }
                return create(name, propertyClass, propertyType, getObject(getter), setObject(setter));
            }
        }

        return super.create(name, propertyClass, propertyType, getter, setter);
    }

    static void validateMethodAndReturnType(Method method, Class<?> expectedReturnType) {
        if (!method.getReturnType().equals(expectedReturnType)) {
            throw validateMethodAndReturnTypeEror(method, expectedReturnType);
        }
    }

    private static IllegalArgumentException validateMethodAndReturnTypeEror(Method method, Class<?> expectedReturnType) {
        return new IllegalArgumentException(
                "Method return type mismatch. Expected: " + expectedReturnType.getSimpleName() +
                        ", Actual: " + method.getReturnType().getSimpleName());
    }

    public Predicate<Object> getBoolean(Method method) {
        if (method == null) {
            return null;
        }
        validateMethodAndReturnType(method, boolean.class);
        MethodHandles.Lookup lookup = JDKUtils.trustedLookup(method.getDeclaringClass());
        try {
            MethodHandle handle = lookup.unreflect(method);
            return (Predicate<Object>) LambdaMetafactory.metafactory(
                    lookup,
                    "test",
                    MethodType.methodType(Predicate.class),
                    MethodType.methodType(boolean.class, Object.class),
                    handle,
                    MethodType.methodType(boolean.class, method.getDeclaringClass())
            ).getTarget().invokeExact();
        } catch (Throwable e) {
            throw new RuntimeException("Failed to create lambda for method: " + method, e);
        }
    }

    public ObjBoolConsumer setBoolean(Method method) {
        if (method == null) {
            return null;
        }

        validateMethodAndParameterType(method, boolean.class);
        MethodHandles.Lookup lookup = JDKUtils.trustedLookup(method.getDeclaringClass());
        try {
            MethodHandle handle = lookup.unreflect(method);
            BiConsumer<Object, Boolean> biConsumer = (BiConsumer<Object, Boolean>) LambdaMetafactory.metafactory(
                    lookup,
                    "accept",
                    MethodType.methodType(BiConsumer.class),
                    MethodType.methodType(void.class, Object.class, Object.class),
                    handle,
                    MethodType.methodType(void.class, method.getDeclaringClass(), Boolean.class)
            ).getTarget().invokeExact();
            return biConsumer::accept;
        } catch (Throwable e) {
            throw new RuntimeException("Failed to create lambda for method: " + method, e);
        }
    }

    public ToByteFunction<Object> getByte(Method method) {
        if (method == null) {
            return null;
        }
        return o -> (byte) getInt(method).applyAsInt(o);
    }

    public ToShortFunction<Object> getShort(Method method) {
        if (method == null) {
            return null;
        }
        return o -> (short) getInt(method).applyAsInt(o);
    }

    public ToCharFunction<Object> getChar(Method method) {
        if (method == null) {
            return null;
        }
        return o -> (char) getInt(method).applyAsInt(o);
    }

    public ToIntFunction<Object> getInt(Method method) {
        if (method == null) {
            return null;
        }
        Class<?> returnType = method.getReturnType();
        if (!returnType.equals(int.class) && !returnType.equals(short.class) && !returnType.equals(byte.class) && !returnType.equals(char.class)) {
            throw validateMethodAndReturnTypeEror(method, int.class);
        }

        MethodHandles.Lookup lookup = JDKUtils.trustedLookup(method.getDeclaringClass());
        try {
            MethodHandle handle = lookup.unreflect(method);
            return (ToIntFunction<Object>) LambdaMetafactory.metafactory(
                    lookup,
                    "applyAsInt",
                    MethodType.methodType(ToIntFunction.class),
                    MethodType.methodType(int.class, Object.class),
                    handle,
                    MethodType.methodType(int.class, method.getDeclaringClass())
            ).getTarget().invokeExact();
        } catch (Throwable e) {
            throw new RuntimeException("Failed to create lambda for method: " + method, e);
        }
    }

    public ToLongFunction<Object> getLong(Method method) {
        if (method == null) {
            return null;
        }
        validateMethodAndReturnType(method, long.class);
        MethodHandles.Lookup lookup = JDKUtils.trustedLookup(method.getDeclaringClass());
        try {
            MethodHandle handle = lookup.unreflect(method);
            return (ToLongFunction<Object>) LambdaMetafactory.metafactory(
                    lookup,
                    "applyAsLong",
                    MethodType.methodType(ToLongFunction.class),
                    MethodType.methodType(long.class, Object.class),
                    handle,
                    MethodType.methodType(long.class, method.getDeclaringClass())
            ).getTarget().invokeExact();
        } catch (Throwable e) {
            throw new RuntimeException("Failed to create lambda for method: " + method, e);
        }
    }

    public Function<Object, Object> getObject(Method method) {
        Class<?> declaringClass = method.getDeclaringClass();
        MethodHandles.Lookup lookup = JDKUtils.trustedLookup(declaringClass);
        try {
            MethodHandle handle = lookup.unreflect(method);
            return (Function<Object, Object>) LambdaMetafactory.metafactory(
                    lookup,
                    "apply",
                    MethodType.methodType(Function.class),
                    MethodType.methodType(Object.class, Object.class),
                    handle,
                    MethodType.methodType(method.getReturnType(), method.getDeclaringClass())
            ).getTarget().invokeExact();
        } catch (Throwable e) {
            throw new RuntimeException("Failed to create lambda for method: " + method, e);
        }
    }

    public ObjByteConsumer<Object> setByte(Method method) {
        if (method == null) {
            return null;
        }
        return (o, v) -> setInt(method).accept(o, (int) v);
    }

    public ObjCharConsumer<Object> setChar(Method method) {
        if (method == null) {
            return null;
        }
        return (o, v) -> setInt(method).accept(o, (int) v);
    }

    public ObjShortConsumer<Object> setShort(Method method) {
        if (method == null) {
            return null;
        }
        return (o, v) -> setInt(method).accept(o, (int) v);
    }

    public ObjIntConsumer<Object> setInt(Method method) {
        if (method == null) {
            return null;
        }
        Class<?>[] parameterTypes = method.getParameterTypes();
        Class<?> paramType;
        if (parameterTypes.length != 1 || (!(paramType = parameterTypes[0]).equals(int.class) && !paramType.equals(short.class) && !paramType.equals(byte.class) && !paramType.equals(char.class))) {
            throw validateMethodAndParameterTypeError(int.class, parameterTypes);
        }
        MethodHandles.Lookup lookup = JDKUtils.trustedLookup(method.getDeclaringClass());
        try {
            MethodHandle handle = lookup.unreflect(method);
            return (ObjIntConsumer<Object>) LambdaMetafactory.metafactory(
                    lookup,
                    "accept",
                    MethodType.methodType(ObjIntConsumer.class),
                    MethodType.methodType(void.class, Object.class, int.class),
                    handle,
                    MethodType.methodType(void.class, method.getDeclaringClass(), paramType)
            ).getTarget().invokeExact();
        } catch (Throwable e) {
            throw new RuntimeException("Failed to create lambda for method: " + method, e);
        }
    }

    public ObjLongConsumer<Object> setLong(Method method) {
        if (method == null) {
            return null;
        }

        validateMethodAndParameterType(method, long.class);
        MethodHandles.Lookup lookup = JDKUtils.trustedLookup(method.getDeclaringClass());
        try {
            MethodHandle handle = lookup.unreflect(method);
            return (ObjLongConsumer<Object>) LambdaMetafactory.metafactory(
                    lookup,
                    "accept",
                    MethodType.methodType(ObjLongConsumer.class),
                    MethodType.methodType(void.class, Object.class, long.class),
                    handle,
                    MethodType.methodType(void.class, method.getDeclaringClass(), long.class)
            ).getTarget().invokeExact();
        } catch (Throwable e) {
            throw new RuntimeException("Failed to create lambda for method: " + method, e);
        }
    }

    public ToFloatFunction<Object> getFloat(Method method) {
        if (method == null) {
            return null;
        }

        validateMethodAndReturnType(method, float.class);
        MethodHandles.Lookup lookup = JDKUtils.trustedLookup(method.getDeclaringClass());
        try {
            MethodHandle handle = lookup.unreflect(method);
            ToDoubleFunction<Object> toDouble = (ToDoubleFunction<Object>) LambdaMetafactory.metafactory(
                    lookup,
                    "applyAsDouble",
                    MethodType.methodType(ToDoubleFunction.class),
                    MethodType.methodType(double.class, Object.class),
                    handle,
                    MethodType.methodType(float.class, method.getDeclaringClass())
            ).getTarget().invokeExact();

            return (ToFloatFunction<Object>) (obj) -> (float) toDouble.applyAsDouble(obj);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to create lambda for method: " + method, e);
        }
    }

    public ObjFloatConsumer setFloat(Method method) {
        if (method == null) {
            return null;
        }

        validateMethodAndParameterType(method, float.class);
        MethodHandles.Lookup lookup = JDKUtils.trustedLookup(method.getDeclaringClass());
        try {
            MethodHandle handle = lookup.unreflect(method);
            BiConsumer<Object, Float> setDouble = (BiConsumer<Object, Float>) LambdaMetafactory.metafactory(
                    lookup,
                    "accept",
                    MethodType.methodType(BiConsumer.class),
                    MethodType.methodType(void.class, Object.class, Object.class),
                    handle,
                    MethodType.methodType(void.class, method.getDeclaringClass(), Float.class)
            ).getTarget().invokeExact();
            return (obj, value) -> setDouble.accept(obj, (float) value);
        } catch (Throwable e) {
            throw new RuntimeException("Failed to create lambda for method: " + method, e);
        }
    }

    public ToDoubleFunction<Object> getDouble(Method method) {
        if (method == null) {
            return null;
        }
        validateMethodAndReturnType(method, double.class);
        MethodHandles.Lookup lookup = JDKUtils.trustedLookup(method.getDeclaringClass());
        try {
            MethodHandle handle = lookup.unreflect(method);
            return (ToDoubleFunction<Object>) LambdaMetafactory.metafactory(
                    lookup,
                    "applyAsDouble",
                    MethodType.methodType(ToDoubleFunction.class),
                    MethodType.methodType(double.class, Object.class),
                    handle,
                    MethodType.methodType(double.class, method.getDeclaringClass())
            ).getTarget().invokeExact();
        } catch (Throwable e) {
            throw new RuntimeException("Failed to create lambda for method: " + method, e);
        }
    }

    public ObjDoubleConsumer<Object> setDouble(Method method) {
        if (method == null) {
            return null;
        }

        validateMethodAndParameterType(method, double.class);
        MethodHandles.Lookup lookup = JDKUtils.trustedLookup(method.getDeclaringClass());
        try {
            MethodHandle handle = lookup.unreflect(method);
            return (ObjDoubleConsumer<Object>) LambdaMetafactory.metafactory(
                    lookup,
                    "accept",
                    MethodType.methodType(ObjDoubleConsumer.class),
                    MethodType.methodType(void.class, Object.class, double.class),
                    handle,
                    MethodType.methodType(void.class, method.getDeclaringClass(), double.class)
            ).getTarget().invokeExact();
        } catch (Throwable e) {
            throw new RuntimeException("Failed to create lambda for method: " + method, e);
        }
    }

    public BiConsumer<Object, Object> setObject(Method method) {
        if (method == null) {
            return null;
        }

        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length != 1) {
            throw new IllegalArgumentException("Method must have exactly one parameter");
        }
        MethodHandles.Lookup lookup = JDKUtils.trustedLookup(method.getDeclaringClass());
        try {
            MethodHandle handle = lookup.unreflect(method);
            return (BiConsumer<Object, Object>) LambdaMetafactory.metafactory(
                    lookup,
                    "accept",
                    MethodType.methodType(BiConsumer.class),
                    MethodType.methodType(void.class, Object.class, Object.class),
                    handle,
                    MethodType.methodType(void.class, method.getDeclaringClass(), method.getParameterTypes()[0])
            ).getTarget().invokeExact();
        } catch (Throwable e) {
            throw new RuntimeException("Failed to create lambda for method: " + method, e);
        }
    }

    static boolean isChainableSetter(Method method) {
        return method.getReturnType() == method.getDeclaringClass();
    }

    static void validateMethodAndParameterType(Method method, Class<?> expectedParameterType) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length != 1 || !parameterTypes[0].equals(expectedParameterType)) {
            throw validateMethodAndParameterTypeError(expectedParameterType, parameterTypes);
        }
    }

    private static IllegalArgumentException validateMethodAndParameterTypeError(Class<?> expectedParameterType, Class<?>[] parameterTypes) {
        return new IllegalArgumentException(
                "Method parameter type mismatch. Expected: " + expectedParameterType.getSimpleName() +
                        ", Actual: " + (parameterTypes.length > 0 ? parameterTypes[0].getSimpleName() : "no parameters"));
    }
}
