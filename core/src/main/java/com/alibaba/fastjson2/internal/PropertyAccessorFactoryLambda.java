package com.alibaba.fastjson2.internal;

import com.alibaba.fastjson2.function.*;
import com.alibaba.fastjson2.util.JDKUtils;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.function.*;

@SuppressWarnings("ALL")
public abstract class PropertyAccessorFactoryLambda extends PropertyAccessorFactory {
    private MethodHandles.Lookup lookup(Method method) {
        return lookup(method.getDeclaringClass());
    }

    protected MethodHandles.Lookup lookup(Class<?> declaringClass) {
        if (Conf.USE_UNSAFE) {
            return JDKUtils.trustedLookup(declaringClass);
        } else {
            return MethodHandles.lookup().in(declaringClass);
        }
    }

    /**
     * Creates a property accessor using getter and/or setter methods.
     * This method delegates to the parent class's implementation.
     *
     * @param name the property name
     * @param getter the getter method (optional, may be null)
     * @param setter the setter method (optional, may be null)
     * @return a PropertyAccessor instance for the specified getter/setter methods
     */
    public PropertyAccessor create(String name, Method getter, Method setter) {
        return super.create(name, null, null, getter, setter);
    }

    /**
     * Creates a property accessor using getter and/or setter methods with explicit type information.
     * This method attempts to optimize access by using LambdaMetafactory to create
     * efficient functional interfaces for property access when possible.
     *
     * @param name the property name
     * @param propertyClass the class of the property value
     * @param propertyType the generic type of the property value
     * @param getter the getter method (optional, may be null)
     * @param setter the setter method (optional, may be null)
     * @return a PropertyAccessor instance for the specified getter/setter methods
     */
    public PropertyAccessor create(String name, Class<?> propertyClass, Type propertyType, Method getter, Method setter) {
        if (propertyClass == null) {
            if (getter != null) {
                propertyClass = getter.getReturnType();
            } else {
                Class<?>[] parameterTypes = setter.getParameterTypes();
                if (parameterTypes.length == 1) {
                    propertyClass = parameterTypes[0];
                } else if (parameterTypes.length == 2 && String.class.equals(parameterTypes[0])) {
                    propertyClass = parameterTypes[1];
                }
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
                        Type[] parameterTypes = setter.getGenericParameterTypes();
                        if (parameterTypes.length == 1) {
                            propertyType = parameterTypes[0];
                        } else if (parameterTypes.length == 2 && String.class.equals(parameterTypes[0])) {
                            propertyType = parameterTypes[1];
                        }
                    }
                }
                return create(name, propertyClass, propertyType, getObject(getter), setObject(name, setter));
            }
        }

        return super.create(name, propertyClass, propertyType, getter, setter);
    }

    /**
     * Validates that the method has the expected return type.
     * Throws an IllegalArgumentException if the return type doesn't match.
     *
     * @param method the method to validate
     * @param expectedReturnType the expected return type
     */
    static void validateMethodAndReturnType(Method method, Class<?> expectedReturnType) {
        if (!method.getReturnType().equals(expectedReturnType)) {
            throw validateMethodAndReturnTypeEror(method, expectedReturnType);
        }
    }

    /**
     * Creates an IllegalArgumentException for method return type validation errors.
     *
     * @param method the method with the mismatched return type
     * @param expectedReturnType the expected return type
     * @return an IllegalArgumentException with details about the error
     */
    private static IllegalArgumentException validateMethodAndReturnTypeEror(Method method, Class<?> expectedReturnType) {
        return new IllegalArgumentException(
                "Method return type mismatch. Expected: " + expectedReturnType.getSimpleName() +
                        ", Actual: " + method.getReturnType().getSimpleName());
    }

    /**
     * Creates a Predicate functional interface to access a boolean property via the given getter method.
     * Uses LambdaMetafactory to create an efficient functional interface that wraps the method call.
     *
     * @param method the getter method for the boolean property
     * @return a Predicate that can access the boolean property, or null if method is null
     */
    public Predicate<Object> getBoolean(Method method) {
        if (method == null) {
            return null;
        }
        validateMethodAndReturnType(method, boolean.class);
        MethodHandles.Lookup lookup = lookup(method);
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

    /**
     * Creates an ObjBoolConsumer functional interface to set a boolean property via the given setter method.
     * Uses LambdaMetafactory to create an efficient functional interface that wraps the method call.
     *
     * @param method the setter method for the boolean property
     * @return an ObjBoolConsumer that can set the boolean property, or null if method is null
     */
    public ObjBoolConsumer setBoolean(Method method) {
        if (method == null) {
            return null;
        }

        validateMethodAndParameterType(method, boolean.class);
        MethodHandles.Lookup lookup = lookup(method);
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

    /**
     * Creates a ToByteFunction functional interface to access a byte property via the given getter method.
     * For JDK 8, this implementation delegates to getInt and converts the result to byte.
     *
     * @param method the getter method for the byte property
     * @return a ToByteFunction that can access the byte property, or null if method is null
     */
    public ToByteFunction<Object> getByte(Method method) {
        if (method == null) {
            return null;
        }
        return o -> (byte) getInt(method).applyAsInt(o);
    }

    /**
     * Creates a ToShortFunction functional interface to access a short property via the given getter method.
     * For JDK 8, this implementation delegates to getInt and converts the result to short.
     *
     * @param method the getter method for the short property
     * @return a ToShortFunction that can access the short property, or null if method is null
     */
    public ToShortFunction<Object> getShort(Method method) {
        if (method == null) {
            return null;
        }
        return o -> (short) getInt(method).applyAsInt(o);
    }

    /**
     * Creates a ToCharFunction functional interface to access a char property via the given getter method.
     * For JDK 8, this implementation delegates to getInt and converts the result to char.
     *
     * @param method the getter method for the char property
     * @return a ToCharFunction that can access the char property, or null if method is null
     */
    public ToCharFunction<Object> getChar(Method method) {
        if (method == null) {
            return null;
        }
        return o -> (char) getInt(method).applyAsInt(o);
    }

    /**
     * Creates a ToIntFunction functional interface to access an int property via the given getter method.
     * Uses LambdaMetafactory to create an efficient functional interface that wraps the method call.
     *
     * @param method the getter method for the int property
     * @return a ToIntFunction that can access the int property, or null if method is null
     */
    public ToIntFunction<Object> getInt(Method method) {
        if (method == null) {
            return null;
        }
        Class<?> returnType = method.getReturnType();
        if (!returnType.equals(int.class) && !returnType.equals(short.class) && !returnType.equals(byte.class) && !returnType.equals(char.class)) {
            throw validateMethodAndReturnTypeEror(method, int.class);
        }

        MethodHandles.Lookup lookup = lookup(method);
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
        MethodHandles.Lookup lookup = lookup(method);
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
        if (method == null) {
            return null;
        }
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
        MethodHandles.Lookup lookup = lookup(method);
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
        MethodHandles.Lookup lookup = lookup(method);
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
        MethodHandles.Lookup lookup = lookup(method);
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
        MethodHandles.Lookup lookup = lookup(method);
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
        MethodHandles.Lookup lookup = lookup(method);
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
        MethodHandles.Lookup lookup = lookup(method);
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

    /**
     * Creates a BiConsumer functional interface to set an Object property via the given setter method.
     * Uses LambdaMetafactory to create an efficient functional interface that wraps the method call.
     *
     * @param method the setter method for the Object property
     * @return a BiConsumer that can set the Object property, or null if method is null
     */
    public BiConsumer<Object, Object> setObject(Method method) {
        return setObject(null, method);
    }

    /**
     * Creates a BiConsumer functional interface to set an Object property via the given setter method,
     * with the option to pass a property name for chainable setters.
     * Uses LambdaMetafactory to create an efficient functional interface that wraps the method call.
     *
     * @param name the property name (for chainable setters)
     * @param method the setter method for the Object property
     * @return a BiConsumer that can set the Object property, or null if method is null
     */
    public BiConsumer<Object, Object> setObject(String name, Method method) {
        if (method == null) {
            return null;
        }

        MethodHandles.Lookup lookup = lookup(method);

        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length == 2
                && parameterTypes[0].equals(String.class)
        ) {
            BiFunction<String, Object, Object> biFunction;
            try {
                MethodHandle handle = lookup.unreflect(method);
                biFunction = (BiFunction<String, Object, Object>) LambdaMetafactory.metafactory(
                        lookup,
                        "accept",
                        MethodType.methodType(BiFunction.class),
                        MethodType.methodType(void.class, Object.class, Object.class, Object.class),
                        handle,
                        MethodType.methodType(void.class, method.getDeclaringClass(), String.class, method.getParameterTypes()[1])
                ).getTarget().invokeExact();
            } catch (Throwable e) {
                throw new RuntimeException("Failed to create lambda for method: " + method, e);
            }
            return (obj, value) -> biFunction.apply(name, value);
        }

        if (parameterTypes.length != 1) {
            throw new IllegalArgumentException("Method must have exactly one parameter");
        }

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

    /**
     * Checks if the given method is a chainable setter (fluent setter).
     * A chainable setter is one that returns the same type as the declaring class,
     * allowing for method chaining.
     *
     * @param method the method to check
     * @return true if the method is a chainable setter, false otherwise
     */
    static boolean isChainableSetter(Method method) {
        return method.getReturnType() == method.getDeclaringClass();
    }

    /**
     * Validates that the method has exactly one parameter of the expected type.
     * Throws an IllegalArgumentException if the parameter type doesn't match.
     *
     * @param method the method to validate
     * @param expectedParameterType the expected parameter type
     */
    static void validateMethodAndParameterType(Method method, Class<?> expectedParameterType) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length != 1 || !parameterTypes[0].equals(expectedParameterType)) {
            throw validateMethodAndParameterTypeError(expectedParameterType, parameterTypes);
        }
    }

    /**
     * Creates an IllegalArgumentException for method parameter type validation errors.
     *
     * @param expectedParameterType the expected parameter type
     * @param parameterTypes the actual parameter types
     * @return an IllegalArgumentException with details about the error
     */
    private static IllegalArgumentException validateMethodAndParameterTypeError(Class<?> expectedParameterType, Class<?>[] parameterTypes) {
        return new IllegalArgumentException(
                "Method parameter type mismatch. Expected: " + expectedParameterType.getSimpleName() +
                        ", Actual: " + (parameterTypes.length > 0 ? parameterTypes[0].getSimpleName() : "no parameters"));
    }
}
