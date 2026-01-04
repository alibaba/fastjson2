package com.alibaba.fastjson2.introspect;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.internal.Conf;
import com.alibaba.fastjson2.util.BeanUtils;
import com.alibaba.fastjson2.util.JDKUtils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.Supplier;

/**
 * Property accessor factory that uses MethodHandles.Lookup for field access.
 * This implementation uses MethodHandles.Lookup's unreflectGetter/unreflectSetter
 * instead of VarHandle for field access, providing an alternative way to access
 * object properties efficiently.
 */
public final class PropertyAccessorFactoryMethodHandle
        extends PropertyAccessorFactoryLambda {
    public PropertyAccessorFactoryMethodHandle() {
    }

    protected MethodHandles.Lookup lookup(Class<?> declaringClass) {
        if (Conf.USE_UNSAFE) {
            return JDKUtils.trustedLookup(declaringClass);
        } else {
            return MethodHandles.lookup().in(declaringClass);
        }
    }

    /**
     * Creates a Supplier that can instantiate objects using the given constructor
     * via MethodHandle for better performance than traditional reflection.
     * If the MethodHandle approach fails, it falls back to the parent class implementation.
     *
     * @param constructor the constructor to use for object instantiation
     * @return a Supplier that creates new instances using the provided constructor
     */
    public Supplier createSupplier(Constructor constructor) {
        try {
            MethodHandles.Lookup lookup = lookup(constructor.getDeclaringClass());
            MethodHandle methodHandle = lookup.unreflectConstructor(constructor);
            return () -> {
                try {
                    return methodHandle.invoke();
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            };
        } catch (Throwable ignored) {
            // ignore
            return super.createSupplier(constructor);
        }
    }

    /**
     * Creates a property accessor for the given field using MethodHandle-based implementation.
     * Different accessor implementations are created based on the field type to provide
     * optimal performance for each specific type.
     *
     * @param field the field for which to create an accessor
     * @return a property accessor appropriate for the field type
     */
    protected PropertyAccessor createInternal(Field field) {
        MethodHandles.Lookup lookup = lookup(field.getDeclaringClass());
        MethodHandle getter;
        MethodHandle setter;
        Class<?> fieldType = field.getType();
        try {
            getter = lookup.unreflectGetter(field);
            setter = lookup.unreflectSetter(field);
        } catch (IllegalAccessException e) {
            // ignore
            return super.createInternal(field);
        }

        if (fieldType == byte.class) {
            return new FieldAccessorMethodHandleByteValue(field, getter, setter);
        }
        if (fieldType == short.class) {
            return new FieldAccessorMethodHandleShortValue(field, getter, setter);
        }
        if (fieldType == int.class) {
            return new FieldAccessorMethodHandleIntValue(field, getter, setter);
        }
        if (fieldType == long.class) {
            return new FieldAccessorMethodHandleLongValue(field, getter, setter);
        }
        if (fieldType == float.class) {
            return new FieldAccessorMethodHandleFloatValue(field, getter, setter);
        }
        if (fieldType == double.class) {
            return new FieldAccessorMethodHandleDoubleValue(field, getter, setter);
        }
        if (fieldType == boolean.class) {
            return new FieldAccessorMethodHandleBooleanValue(field, getter, setter);
        }
        if (fieldType == char.class) {
            return new FieldAccessorMethodHandleCharValue(field, getter, setter);
        }
        if (fieldType == String.class) {
            return new FieldAccessorMethodHandleString(field, getter, setter);
        }
        if (fieldType == BigInteger.class) {
            return new FieldAccessorMethodHandleBigInteger(field, getter, setter);
        }
        if (fieldType == BigDecimal.class) {
            return new FieldAccessorMethodHandleBigDecimal(field, getter, setter);
        }
        if (fieldType == Boolean.class) {
            return new FieldAccessorMethodHandleBoolean(field, getter, setter);
        }
        if (fieldType == Byte.class) {
            return new FieldAccessorMethodHandleByte(field, getter, setter);
        }
        if (fieldType == Character.class) {
            return new FieldAccessorMethodHandleCharacter(field, getter, setter);
        }
        if (fieldType == Short.class) {
            return new FieldAccessorMethodHandleShort(field, getter, setter);
        }
        if (fieldType == Integer.class) {
            return new FieldAccessorMethodHandleInteger(field, getter, setter);
        }
        if (fieldType == Long.class) {
            return new FieldAccessorMethodHandleLong(field, getter, setter);
        }
        if (fieldType == Float.class) {
            return new FieldAccessorMethodHandleFloat(field, getter, setter);
        }
        if (fieldType == Double.class) {
            return new FieldAccessorMethodHandleDouble(field, getter, setter);
        }
        if (fieldType == Number.class) {
            return new FieldAccessorMethodHandleNumber(field, getter, setter);
        }
        return new FieldAccessorMethodHandleObject(field, getter, setter);
    }

    /**
     * Creates a property accessor using getter and/or setter methods.
     * This method attempts to create MethodHandle-based accessors for efficient method calls.
     * Validates that the getter has no parameters and the setter has one parameter.
     * The property type is inferred from the getter's return type or setter's parameter type.
     *
     * @param name the property name
     * @param propertyClass the class of the property value
     * @param propertyType the generic type of the property value
     * @param getter the getter method (optional, may be null)
     * @param setter the setter method (optional, may be null)
     * @return a PropertyAccessor instance for the specified getter/setter methods
     * @throws JSONException if the getter or setter method signatures are invalid
     */
    public PropertyAccessor create(String name, Class<?> propertyClass, Type propertyType, Method getter, Method setter) {
        if (getter != null) {
            if (getter.getParameterCount() != 0) {
                throw new JSONException("create PropertyAccessor error, method parameterCount is not 0");
            }

            if (name == null) {
                name = BeanUtils.getterName(getter.getName(), null);
            }
            Class<?> returnClass = getter.getReturnType();
            if (propertyClass == null) {
                propertyClass = returnClass;
            } else if (!propertyClass.equals(returnClass)) {
                throw new JSONException("create PropertyAccessor error, propertyClass not match");
            }

            Type returnType = getter.getGenericReturnType();
            if (propertyType == null) {
                propertyType = returnType;
            } else if (!propertyType.equals(propertyType)) {
                throw new JSONException("create PropertyAccessor error, propertyType not match");
            }
        }

        if (setter != null) {
            if (setter.getParameterCount() != 1) {
                throw new JSONException("create PropertyAccessor error, method parameterCount is not 1");
            }

            if (name == null) {
                name = BeanUtils.setterName(setter.getName(), null);
            }

            Class<?>[] parameterClasses = setter.getParameterTypes();
            Type[] parameterTypes = setter.getGenericParameterTypes();
            Class<?> parameterClass = parameterClasses[0];
            Type parameterType = parameterTypes[0];

            if (propertyClass == null) {
                propertyClass = parameterClass;
            } else if (!propertyClass.equals(parameterClass)) {
                throw new JSONException("create PropertyAccessor error, propertyClass not match");
            }

            if (propertyType == null) {
                propertyType = parameterType;
            } else if (!propertyType.equals(parameterType)) {
                throw new JSONException("create PropertyAccessor error, propertyType not match");
            }
        }

        if (propertyClass == void.class || propertyClass == Void.class) {
            throw new JSONException("create PropertyAccessor error, method returnType is void");
        }

        // Try to create MethodHandle-based method accessors
        MethodHandles.Lookup lookup = lookup(getter != null ? getter.getDeclaringClass() : setter.getDeclaringClass());
        MethodHandle getterHandle = null;
        MethodHandle setterHandle = null;

        try {
            if (getter != null) {
                getterHandle = lookup.unreflect(getter);
            }
            if (setter != null) {
                setterHandle = lookup.unreflect(setter);
            }
        } catch (IllegalAccessException e) {
            // Fall back to parent implementation if MethodHandle creation fails
            return super.create(name, propertyClass, propertyType, getter, setter);
        }

        if (propertyClass == byte.class) {
            return new MethodAccessorMethodHandleByteValue(name, propertyType, propertyClass, getterHandle, setterHandle);
        }
        if (propertyClass == short.class) {
            return new MethodAccessorMethodHandleShortValue(name, propertyType, propertyClass, getterHandle, setterHandle);
        }
        if (propertyClass == int.class) {
            return new MethodAccessorMethodHandleIntValue(name, propertyType, propertyClass, getterHandle, setterHandle);
        }
        if (propertyClass == long.class) {
            return new MethodAccessorMethodHandleLongValue(name, propertyType, propertyClass, getterHandle, setterHandle);
        }
        if (propertyClass == float.class) {
            return new MethodAccessorMethodHandleFloatValue(name, propertyType, propertyClass, getterHandle, setterHandle);
        }
        if (propertyClass == double.class) {
            return new MethodAccessorMethodHandleDoubleValue(name, propertyType, propertyClass, getterHandle, setterHandle);
        }
        if (propertyClass == char.class) {
            return new MethodAccessorMethodHandleCharValue(name, propertyType, propertyClass, getterHandle, setterHandle);
        }
        if (propertyClass == boolean.class) {
            return new MethodAccessorMethodHandleBooleanValue(name, propertyType, propertyClass, getterHandle, setterHandle);
        }
        if (propertyClass == String.class) {
            return new MethodAccessorMethodHandleString(name, propertyType, propertyClass, getterHandle, setterHandle);
        }
        if (propertyClass == BigInteger.class) {
            return new MethodAccessorMethodHandleBigInteger(name, propertyType, propertyClass, getterHandle, setterHandle);
        }
        if (propertyClass == BigDecimal.class) {
            return new MethodAccessorMethodHandleBigDecimal(name, propertyType, propertyClass, getterHandle, setterHandle);
        }
        if (propertyClass == Boolean.class) {
            return new MethodAccessorMethodHandleBoolean(name, propertyType, propertyClass, getterHandle, setterHandle);
        }
        if (propertyClass == Byte.class) {
            return new MethodAccessorMethodHandleByte(name, propertyType, propertyClass, getterHandle, setterHandle);
        }
        if (propertyClass == Character.class) {
            return new MethodAccessorMethodHandleCharacter(name, propertyType, propertyClass, getterHandle, setterHandle);
        }
        if (propertyClass == Short.class) {
            return new MethodAccessorMethodHandleShort(name, propertyType, propertyClass, getterHandle, setterHandle);
        }
        if (propertyClass == Integer.class) {
            return new MethodAccessorMethodHandleInteger(name, propertyType, propertyClass, getterHandle, setterHandle);
        }
        if (propertyClass == Long.class) {
            return new MethodAccessorMethodHandleLong(name, propertyType, propertyClass, getterHandle, setterHandle);
        }
        if (propertyClass == Float.class) {
            return new MethodAccessorMethodHandleFloat(name, propertyType, propertyClass, getterHandle, setterHandle);
        }
        if (propertyClass == Double.class) {
            return new MethodAccessorMethodHandleDouble(name, propertyType, propertyClass, getterHandle, setterHandle);
        }
        if (propertyClass == Number.class) {
            return new MethodAccessorMethodHandleNumber(name, propertyType, propertyClass, getterHandle, setterHandle);
        }

        return new MethodAccessorMethodHandleObject(name, propertyType, propertyClass, getterHandle, setterHandle);
    }

    /**
     * Base class for MethodHandle-based field accessors.
     * Uses MethodHandle for efficient field access which is faster than traditional reflection.
     */
    abstract static class FieldAccessorMethodHandle extends FieldAccessor {
        final MethodHandle getter;
        final MethodHandle setter;

        /**
         * Creates a field accessor using MethodHandle for the given field.
         *
         * @param field the field to access
         * @param getter the MethodHandle to use for getting field values
         * @param setter the MethodHandle to use for setting field values
         */
        public FieldAccessorMethodHandle(Field field, MethodHandle getter, MethodHandle setter) {
            super(field);
            this.getter = getter;
            this.setter = setter;
        }
    }

    /**
     * Base class for MethodHandle-based method accessors.
     * Uses MethodHandle for efficient method access which is faster than traditional reflection.
     */
    abstract static class MethodAccessorMethodHandle implements PropertyAccessor {
        final String name;
        final Type propertyType;
        final Class<?> propertyClass;
        final MethodHandle getter;
        final MethodHandle setter;

        /**
         * Creates a method accessor using MethodHandle for the given getter and setter.
         *
         * @param name the property name
         * @param propertyType the generic type of the property
         * @param propertyClass the class of the property
         * @param getter the MethodHandle to use for getting property values
         * @param setter the MethodHandle to use for setting property values
         */
        public MethodAccessorMethodHandle(String name, Type propertyType, Class<?> propertyClass, MethodHandle getter, MethodHandle setter) {
            this.name = name;
            this.propertyType = propertyType;
            this.propertyClass = propertyClass;
            this.getter = getter;
            this.setter = setter;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public Class<?> propertyClass() {
            return propertyClass;
        }

        @Override
        public Type propertyType() {
            return propertyType;
        }

        @Override
        public boolean supportGet() {
            return getter != null;
        }

        @Override
        public boolean supportSet() {
            return setter != null;
        }

        /**
         * Creates a JSON exception for getter errors.
         *
         * @param e the exception that occurred during getting
         * @return a JSONException with details about the getter error
         */
        final JSONException errorForGet(Throwable e) {
            return new JSONException(name.concat(" get error"), e);
        }

        /**
         * Creates a JSON exception for setter errors.
         *
         * @param e the exception that occurred during setting
         * @return a JSONException with details about the setter error
         */
        final JSONException errorForSet(Throwable e) {
            return new JSONException(name.concat(" set error"), e);
        }
    }

    /**
     * Method accessor implementation for boolean-typed properties using MethodHandle.
     * Provides efficient getter and setter operations for boolean properties via MethodHandle invocation.
     */
    static final class MethodAccessorMethodHandleBooleanValue
            extends MethodAccessorMethodHandle
            implements PropertyAccessorBooleanValue
    {
        public MethodAccessorMethodHandleBooleanValue(String name, Type propertyType, Class<?> propertyClass, MethodHandle getter, MethodHandle setter) {
            super(name, propertyType, propertyClass, getter, setter);
        }

        @Override
        public boolean getBooleanValue(Object object) {
            try {
                return (boolean) getter.invoke(object);
            } catch (Throwable e) {
                throw errorForGet(e);
            }
        }

        @Override
        public void setBooleanValue(Object object, boolean value) {
            try {
                setter.invoke(object, value);
            } catch (Throwable e) {
                throw errorForSet(e);
            }
        }
    }

    /**
     * Method accessor implementation for byte-typed properties using MethodHandle.
     * Provides efficient getter and setter operations for byte properties via MethodHandle invocation.
     */
    static final class MethodAccessorMethodHandleByteValue
            extends MethodAccessorMethodHandle
            implements PropertyAccessorByteValue
    {
        public MethodAccessorMethodHandleByteValue(String name, Type propertyType, Class<?> propertyClass, MethodHandle getter, MethodHandle setter) {
            super(name, propertyType, propertyClass, getter, setter);
        }

        @Override
        public byte getByteValue(Object object) {
            try {
                return (byte) getter.invoke(object);
            } catch (Throwable e) {
                throw errorForGet(e);
            }
        }

        @Override
        public void setByteValue(Object object, byte value) {
            try {
                setter.invoke(object, value);
            } catch (Throwable e) {
                throw errorForSet(e);
            }
        }
    }

    /**
     * Method accessor implementation for char-typed properties using MethodHandle.
     * Provides efficient getter and setter operations for char properties via MethodHandle invocation.
     */
    static final class MethodAccessorMethodHandleCharValue
            extends MethodAccessorMethodHandle
            implements PropertyAccessorCharValue
    {
        public MethodAccessorMethodHandleCharValue(String name, Type propertyType, Class<?> propertyClass, MethodHandle getter, MethodHandle setter) {
            super(name, propertyType, propertyClass, getter, setter);
        }

        @Override
        public char getCharValue(Object object) {
            try {
                return (char) getter.invoke(object);
            } catch (Throwable e) {
                throw errorForGet(e);
            }
        }

        @Override
        public void setCharValue(Object object, char value) {
            try {
                setter.invoke(object, value);
            } catch (Throwable e) {
                throw errorForSet(e);
            }
        }
    }

    /**
     * Method accessor implementation for short-typed properties using MethodHandle.
     * Provides efficient getter and setter operations for short properties via MethodHandle invocation.
     */
    static final class MethodAccessorMethodHandleShortValue
            extends MethodAccessorMethodHandle
            implements PropertyAccessorShortValue
    {
        public MethodAccessorMethodHandleShortValue(String name, Type propertyType, Class<?> propertyClass, MethodHandle getter, MethodHandle setter) {
            super(name, propertyType, propertyClass, getter, setter);
        }

        @Override
        public short getShortValue(Object object) {
            try {
                return (short) getter.invoke(object);
            } catch (Throwable e) {
                throw errorForGet(e);
            }
        }

        @Override
        public void setShortValue(Object object, short value) {
            try {
                setter.invoke(object, value);
            } catch (Throwable e) {
                throw errorForSet(e);
            }
        }
    }

    /**
     * Method accessor implementation for int-typed properties using MethodHandle.
     * Provides efficient getter and setter operations for int properties via MethodHandle invocation.
     */
    static final class MethodAccessorMethodHandleIntValue
            extends MethodAccessorMethodHandle
            implements PropertyAccessorIntValue
    {
        public MethodAccessorMethodHandleIntValue(String name, Type propertyType, Class<?> propertyClass, MethodHandle getter, MethodHandle setter) {
            super(name, propertyType, propertyClass, getter, setter);
        }

        @Override
        public int getIntValue(Object object) {
            try {
                return (int) getter.invoke(object);
            } catch (Throwable e) {
                throw errorForGet(e);
            }
        }

        @Override
        public void setIntValue(Object object, int value) {
            try {
                setter.invoke(object, value);
            } catch (Throwable e) {
                throw errorForSet(e);
            }
        }
    }

    /**
     * Method accessor implementation for long-typed properties using MethodHandle.
     * Provides efficient getter and setter operations for long properties via MethodHandle invocation.
     */
    static final class MethodAccessorMethodHandleLongValue
            extends MethodAccessorMethodHandle
            implements PropertyAccessorLongValue
    {
        public MethodAccessorMethodHandleLongValue(String name, Type propertyType, Class<?> propertyClass, MethodHandle getter, MethodHandle setter) {
            super(name, propertyType, propertyClass, getter, setter);
        }

        @Override
        public long getLongValue(Object object) {
            try {
                return (long) getter.invoke(object);
            } catch (Throwable e) {
                throw errorForGet(e);
            }
        }

        @Override
        public void setLongValue(Object object, long value) {
            try {
                setter.invoke(object, value);
            } catch (Throwable e) {
                throw errorForSet(e);
            }
        }
    }

    /**
     * Method accessor implementation for float-typed properties using MethodHandle.
     * Provides efficient getter and setter operations for float properties via MethodHandle invocation.
     */
    static final class MethodAccessorMethodHandleFloatValue
            extends MethodAccessorMethodHandle
            implements PropertyAccessorFloatValue
    {
        public MethodAccessorMethodHandleFloatValue(String name, Type propertyType, Class<?> propertyClass, MethodHandle getter, MethodHandle setter) {
            super(name, propertyType, propertyClass, getter, setter);
        }

        @Override
        public float getFloatValue(Object object) {
            try {
                return (float) getter.invoke(object);
            } catch (Throwable e) {
                throw errorForGet(e);
            }
        }

        @Override
        public void setFloatValue(Object object, float value) {
            try {
                setter.invoke(object, value);
            } catch (Throwable e) {
                throw errorForSet(e);
            }
        }
    }

    /**
     * Method accessor implementation for double-typed properties using MethodHandle.
     * Provides efficient getter and setter operations for double properties via MethodHandle invocation.
     */
    static final class MethodAccessorMethodHandleDoubleValue
            extends MethodAccessorMethodHandle
            implements PropertyAccessorDoubleValue
    {
        public MethodAccessorMethodHandleDoubleValue(String name, Type propertyType, Class<?> propertyClass, MethodHandle getter, MethodHandle setter) {
            super(name, propertyType, propertyClass, getter, setter);
        }

        @Override
        public double getDoubleValue(Object object) {
            try {
                return (double) getter.invoke(object);
            } catch (Throwable e) {
                throw errorForGet(e);
            }
        }

        @Override
        public void setDoubleValue(Object object, double value) {
            try {
                setter.invoke(object, value);
            } catch (Throwable e) {
                throw errorForSet(e);
            }
        }
    }

    /**
     * Method accessor implementation for Boolean-typed properties using MethodHandle.
     * Provides efficient getter and setter operations for Boolean properties via MethodHandle invocation.
     */
    static final class MethodAccessorMethodHandleBoolean
            extends MethodAccessorMethodHandle
            implements PropertyAccessorBoolean
    {
        public MethodAccessorMethodHandleBoolean(String name, Type propertyType, Class<?> propertyClass, MethodHandle getter, MethodHandle setter) {
            super(name, propertyType, propertyClass, getter, setter);
        }

        @Override
        public Boolean getBoolean(Object object) {
            try {
                return (Boolean) getter.invoke(object);
            } catch (Throwable e) {
                throw errorForGet(e);
            }
        }

        @Override
        public void setBoolean(Object object, Boolean value) {
            try {
                setter.invoke(object, value);
            } catch (Throwable e) {
                throw errorForSet(e);
            }
        }
    }

    /**
     * Method accessor implementation for Byte-typed properties using MethodHandle.
     * Provides efficient getter and setter operations for Byte properties via MethodHandle invocation.
     */
    static final class MethodAccessorMethodHandleByte
            extends MethodAccessorMethodHandle
            implements PropertyAccessorByte
    {
        public MethodAccessorMethodHandleByte(String name, Type propertyType, Class<?> propertyClass, MethodHandle getter, MethodHandle setter) {
            super(name, propertyType, propertyClass, getter, setter);
        }

        @Override
        public Byte getByte(Object object) {
            try {
                return (Byte) getter.invoke(object);
            } catch (Throwable e) {
                throw errorForGet(e);
            }
        }

        @Override
        public void setByte(Object object, Byte value) {
            try {
                setter.invoke(object, value);
            } catch (Throwable e) {
                throw errorForSet(e);
            }
        }
    }

    /**
     * Method accessor implementation for Character-typed properties using MethodHandle.
     * Provides efficient getter and setter operations for Character properties via MethodHandle invocation.
     */
    static final class MethodAccessorMethodHandleCharacter
            extends MethodAccessorMethodHandle
            implements PropertyAccessorCharacter
    {
        public MethodAccessorMethodHandleCharacter(String name, Type propertyType, Class<?> propertyClass, MethodHandle getter, MethodHandle setter) {
            super(name, propertyType, propertyClass, getter, setter);
        }

        @Override
        public Character getCharacter(Object object) {
            try {
                return (Character) getter.invoke(object);
            } catch (Throwable e) {
                throw errorForGet(e);
            }
        }

        @Override
        public void setCharacter(Object object, Character value) {
            try {
                setter.invoke(object, value);
            } catch (Throwable e) {
                throw errorForSet(e);
            }
        }
    }

    /**
     * Method accessor implementation for Short-typed properties using MethodHandle.
     * Provides efficient getter and setter operations for Short properties via MethodHandle invocation.
     */
    static final class MethodAccessorMethodHandleShort
            extends MethodAccessorMethodHandle
            implements PropertyAccessorShort
    {
        public MethodAccessorMethodHandleShort(String name, Type propertyType, Class<?> propertyClass, MethodHandle getter, MethodHandle setter) {
            super(name, propertyType, propertyClass, getter, setter);
        }

        @Override
        public Short getShort(Object object) {
            try {
                return (Short) getter.invoke(object);
            } catch (Throwable e) {
                throw errorForGet(e);
            }
        }

        @Override
        public void setShort(Object object, Short value) {
            try {
                setter.invoke(object, value);
            } catch (Throwable e) {
                throw errorForSet(e);
            }
        }
    }

    /**
     * Method accessor implementation for Integer-typed properties using MethodHandle.
     * Provides efficient getter and setter operations for Integer properties via MethodHandle invocation.
     */
    static final class MethodAccessorMethodHandleInteger
            extends MethodAccessorMethodHandle
            implements PropertyAccessorInteger
    {
        public MethodAccessorMethodHandleInteger(String name, Type propertyType, Class<?> propertyClass, MethodHandle getter, MethodHandle setter) {
            super(name, propertyType, propertyClass, getter, setter);
        }

        @Override
        public Integer getInteger(Object object) {
            try {
                return (Integer) getter.invoke(object);
            } catch (Throwable e) {
                throw errorForGet(e);
            }
        }

        @Override
        public void setInteger(Object object, Integer value) {
            try {
                setter.invoke(object, value);
            } catch (Throwable e) {
                throw errorForSet(e);
            }
        }
    }

    /**
     * Method accessor implementation for Long-typed properties using MethodHandle.
     * Provides efficient getter and setter operations for Long properties via MethodHandle invocation.
     */
    static final class MethodAccessorMethodHandleLong
            extends MethodAccessorMethodHandle
            implements PropertyAccessorLong
    {
        public MethodAccessorMethodHandleLong(String name, Type propertyType, Class<?> propertyClass, MethodHandle getter, MethodHandle setter) {
            super(name, propertyType, propertyClass, getter, setter);
        }

        @Override
        public Long getLong(Object object) {
            try {
                return (Long) getter.invoke(object);
            } catch (Throwable e) {
                throw errorForGet(e);
            }
        }

        @Override
        public void setLong(Object object, Long value) {
            try {
                setter.invoke(object, value);
            } catch (Throwable e) {
                throw errorForSet(e);
            }
        }
    }

    /**
     * Method accessor implementation for Float-typed properties using MethodHandle.
     * Provides efficient getter and setter operations for Float properties via MethodHandle invocation.
     */
    static final class MethodAccessorMethodHandleFloat
            extends MethodAccessorMethodHandle
            implements PropertyAccessorFloat
    {
        public MethodAccessorMethodHandleFloat(String name, Type propertyType, Class<?> propertyClass, MethodHandle getter, MethodHandle setter) {
            super(name, propertyType, propertyClass, getter, setter);
        }

        @Override
        public Float getFloat(Object object) {
            try {
                return (Float) getter.invoke(object);
            } catch (Throwable e) {
                throw errorForGet(e);
            }
        }

        @Override
        public void setFloat(Object object, Float value) {
            try {
                setter.invoke(object, value);
            } catch (Throwable e) {
                throw errorForSet(e);
            }
        }
    }

    /**
     * Method accessor implementation for Double-typed properties using MethodHandle.
     * Provides efficient getter and setter operations for Double properties via MethodHandle invocation.
     */
    static final class MethodAccessorMethodHandleDouble
            extends MethodAccessorMethodHandle
            implements PropertyAccessorDouble
    {
        public MethodAccessorMethodHandleDouble(String name, Type propertyType, Class<?> propertyClass, MethodHandle getter, MethodHandle setter) {
            super(name, propertyType, propertyClass, getter, setter);
        }

        @Override
        public Double getDouble(Object object) {
            try {
                return (Double) getter.invoke(object);
            } catch (Throwable e) {
                throw errorForGet(e);
            }
        }

        @Override
        public void setDouble(Object object, Double value) {
            try {
                setter.invoke(object, value);
            } catch (Throwable e) {
                throw errorForSet(e);
            }
        }
    }

    /**
     * Method accessor implementation for String-typed properties using MethodHandle.
     * Provides efficient getter and setter operations for String properties via MethodHandle invocation.
     */
    static final class MethodAccessorMethodHandleString
            extends MethodAccessorMethodHandle
            implements PropertyAccessorString
    {
        public MethodAccessorMethodHandleString(String name, Type propertyType, Class<?> propertyClass, MethodHandle getter, MethodHandle setter) {
            super(name, propertyType, propertyClass, getter, setter);
        }

        @Override
        public String getString(Object object) {
            try {
                return (String) getter.invoke(object);
            } catch (Throwable e) {
                throw errorForGet(e);
            }
        }

        @Override
        public void setString(Object object, String value) {
            try {
                setter.invoke(object, value);
            } catch (Throwable e) {
                throw errorForSet(e);
            }
        }
    }

    /**
     * Method accessor implementation for BigInteger-typed properties using MethodHandle.
     * Provides efficient getter and setter operations for BigInteger properties via MethodHandle invocation.
     */
    static final class MethodAccessorMethodHandleBigInteger
            extends MethodAccessorMethodHandle
            implements PropertyAccessorBigInteger
    {
        public MethodAccessorMethodHandleBigInteger(String name, Type propertyType, Class<?> propertyClass, MethodHandle getter, MethodHandle setter) {
            super(name, propertyType, propertyClass, getter, setter);
        }

        @Override
        public BigInteger getBigInteger(Object object) {
            try {
                return (BigInteger) getter.invoke(object);
            } catch (Throwable e) {
                throw errorForGet(e);
            }
        }

        @Override
        public void setBigInteger(Object object, BigInteger value) {
            try {
                setter.invoke(object, value);
            } catch (Throwable e) {
                throw errorForSet(e);
            }
        }
    }

    /**
     * Method accessor implementation for BigDecimal-typed properties using MethodHandle.
     * Provides efficient getter and setter operations for BigDecimal properties via MethodHandle invocation.
     */
    static final class MethodAccessorMethodHandleBigDecimal
            extends MethodAccessorMethodHandle
            implements PropertyAccessorBigDecimal
    {
        public MethodAccessorMethodHandleBigDecimal(String name, Type propertyType, Class<?> propertyClass, MethodHandle getter, MethodHandle setter) {
            super(name, propertyType, propertyClass, getter, setter);
        }

        @Override
        public BigDecimal getBigDecimal(Object object) {
            try {
                return (BigDecimal) getter.invoke(object);
            } catch (Throwable e) {
                throw errorForGet(e);
            }
        }

        @Override
        public void setBigDecimal(Object object, BigDecimal value) {
            try {
                setter.invoke(object, value);
            } catch (Throwable e) {
                throw errorForSet(e);
            }
        }
    }

    /**
     * Method accessor implementation for Number-typed properties using MethodHandle.
     * Provides efficient getter and setter operations for Number properties via MethodHandle invocation.
     */
    static final class MethodAccessorMethodHandleNumber
            extends MethodAccessorMethodHandle
            implements PropertyAccessorNumber
    {
        public MethodAccessorMethodHandleNumber(String name, Type propertyType, Class<?> propertyClass, MethodHandle getter, MethodHandle setter) {
            super(name, propertyType, propertyClass, getter, setter);
        }

        @Override
        public Number getNumber(Object object) {
            try {
                return (Number) getter.invoke(object);
            } catch (Throwable e) {
                throw errorForGet(e);
            }
        }

        @Override
        public void setNumber(Object object, Number value) {
            try {
                setter.invoke(object, value);
            } catch (Throwable e) {
                throw errorForSet(e);
            }
        }

        @Override
        public byte getByteValue(Object object) {
            Number num = getNumber(object);
            return num != null ? num.byteValue() : 0;
        }

        @Override
        public char getCharValue(Object object) {
            Number num = getNumber(object);
            return num != null ? (char) num.intValue() : 0;
        }

        @Override
        public short getShortValue(Object object) {
            Number num = getNumber(object);
            return num != null ? num.shortValue() : 0;
        }

        @Override
        public int getIntValue(Object object) {
            Number num = getNumber(object);
            return num != null ? num.intValue() : 0;
        }

        @Override
        public long getLongValue(Object object) {
            Number num = getNumber(object);
            return num != null ? num.longValue() : 0L;
        }

        @Override
        public float getFloatValue(Object object) {
            Number num = getNumber(object);
            return num != null ? num.floatValue() : 0.0f;
        }

        @Override
        public double getDoubleValue(Object object) {
            Number num = getNumber(object);
            return num != null ? num.doubleValue() : 0.0;
        }

        @Override
        public boolean getBooleanValue(Object object) {
            Number num = getNumber(object);
            return num != null && num.intValue() != 0;
        }

        @Override
        public void setByteValue(Object object, byte value) {
            setNumber(object, value);
        }

        @Override
        public void setCharValue(Object object, char value) {
            setNumber(object, (int) value);
        }

        @Override
        public void setShortValue(Object object, short value) {
            setNumber(object, value);
        }

        @Override
        public void setIntValue(Object object, int value) {
            setNumber(object, value);
        }

        @Override
        public void setLongValue(Object object, long value) {
            setNumber(object, value);
        }

        @Override
        public void setFloatValue(Object object, float value) {
            setNumber(object, value);
        }

        @Override
        public void setDoubleValue(Object object, double value) {
            setNumber(object, value);
        }

        @Override
        public void setBooleanValue(Object object, boolean value) {
            setNumber(object, value ? 1 : 0);
        }

        // Additional methods required by PropertyAccessorObject hierarchy that PropertyAccessorNumber implements
        public String getString(Object object) {
            Number num = getNumber(object);
            return num != null ? num.toString() : null;
        }

        public void setString(Object object, String value) {
            setNumber(object, value != null ? Double.valueOf(value) : null);
        }

        public BigInteger getBigInteger(Object object) {
            Number num = getNumber(object);
            return num != null ? BigInteger.valueOf(num.longValue()) : null;
        }

        public void setBigInteger(Object object, BigInteger value) {
            setNumber(object, value != null ? value : null);
        }

        public BigDecimal getBigDecimal(Object object) {
            Number num = getNumber(object);
            return num != null ? new BigDecimal(num.toString()) : null;
        }

        public void setBigDecimal(Object object, BigDecimal value) {
            setNumber(object, value != null ? value : null);
        }

        @Override
        public Object getObject(Object object) {
            return getNumber(object);
        }

        @Override
        public void setObject(Object object, Object value) {
            setNumber(object, (Number) value);
        }
    }

    /**
     * Method accessor implementation for Object-typed properties using MethodHandle.
     * Provides efficient getter and setter operations for Object properties via MethodHandle invocation.
     */
    static final class MethodAccessorMethodHandleObject
            extends MethodAccessorMethodHandle
            implements PropertyAccessorObject
    {
        public MethodAccessorMethodHandleObject(String name, Type propertyType, Class<?> propertyClass, MethodHandle getter, MethodHandle setter) {
            super(name, propertyType, propertyClass, getter, setter);
        }

        @Override
        public Object getObject(Object object) {
            try {
                return getter.invoke(object);
            } catch (Throwable e) {
                throw errorForGet(e);
            }
        }

        @Override
        public void setObject(Object object, Object value) {
            try {
                setter.invoke(object, value);
            } catch (Throwable e) {
                throw errorForSet(e);
            }
        }
    }

    /**
     * Field accessor implementation for boolean fields using MethodHandle.
     * Provides efficient boolean field access operations.
     */
    static final class FieldAccessorMethodHandleBooleanValue
            extends FieldAccessorMethodHandle
            implements PropertyAccessorBooleanValue
    {
        public FieldAccessorMethodHandleBooleanValue(Field field, MethodHandle getter, MethodHandle setter) {
            super(field, getter, setter);
        }

        @Override
        public boolean getBooleanValue(Object object) {
            try {
                return (boolean) getter.invoke(object);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void setBooleanValue(Object object, boolean value) {
            try {
                setter.invoke(object, value);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Field accessor implementation for byte fields using MethodHandle.
     * Provides efficient byte field access operations.
     */
    static final class FieldAccessorMethodHandleByteValue
            extends FieldAccessorMethodHandle
            implements PropertyAccessorByteValue
    {
        public FieldAccessorMethodHandleByteValue(Field field, MethodHandle getter, MethodHandle setter) {
            super(field, getter, setter);
        }
        @Override
        public byte getByteValue(Object object) {
            try {
                return (byte) getter.invoke(object);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        @Override
        public void setByteValue(Object object, byte value) {
            try {
                setter.invoke(object, value);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Field accessor implementation for char fields using MethodHandle.
     * Provides efficient char field access operations.
     */
    static final class FieldAccessorMethodHandleCharValue
            extends FieldAccessorMethodHandle
            implements PropertyAccessorCharValue
    {
        public FieldAccessorMethodHandleCharValue(Field field, MethodHandle getter, MethodHandle setter) {
            super(field, getter, setter);
        }
        @Override
        public char getCharValue(Object object) {
            try {
                return (char) getter.invoke(object);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        @Override
        public void setCharValue(Object object, char value) {
            try {
                setter.invoke(object, value);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Field accessor implementation for short fields using MethodHandle.
     * Provides efficient short field access operations.
     */
    static final class FieldAccessorMethodHandleShortValue
            extends FieldAccessorMethodHandle
            implements PropertyAccessorShortValue
    {
        public FieldAccessorMethodHandleShortValue(Field field, MethodHandle getter, MethodHandle setter) {
            super(field, getter, setter);
        }
        @Override
        public short getShortValue(Object object) {
            try {
                return (short) getter.invoke(object);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        @Override
        public void setShortValue(Object object, short value) {
            try {
                setter.invoke(object, value);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Field accessor implementation for int fields using MethodHandle.
     * Provides efficient int field access operations.
     */
    static final class FieldAccessorMethodHandleIntValue
            extends FieldAccessorMethodHandle
            implements PropertyAccessorIntValue
    {
        public FieldAccessorMethodHandleIntValue(Field field, MethodHandle getter, MethodHandle setter) {
            super(field, getter, setter);
        }
        @Override
        public int getIntValue(Object object) {
            try {
                return (int) getter.invoke(object);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        @Override
        public void setIntValue(Object object, int value) {
            try {
                setter.invoke(object, value);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Field accessor implementation for Boolean fields using MethodHandle.
     * Provides efficient Boolean field access operations.
     */
    static final class FieldAccessorMethodHandleBoolean
            extends FieldAccessorMethodHandle
            implements PropertyAccessorBoolean
    {
        public FieldAccessorMethodHandleBoolean(Field field, MethodHandle getter, MethodHandle setter) {
            super(field, getter, setter);
        }
        @Override
        public Boolean getBoolean(Object object) {
            try {
                return (Boolean) getter.invoke(object);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        @Override
        public void setBoolean(Object object, Boolean value) {
            try {
                setter.invoke(object, value);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Field accessor implementation for Byte fields using MethodHandle.
     * Provides efficient Byte field access operations.
     */
    static final class FieldAccessorMethodHandleByte
            extends FieldAccessorMethodHandle
            implements PropertyAccessorByte
    {
        public FieldAccessorMethodHandleByte(Field field, MethodHandle getter, MethodHandle setter) {
            super(field, getter, setter);
        }
        @Override
        public Byte getByte(Object object) {
            try {
                return (Byte) getter.invoke(object);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        @Override
        public void setByte(Object object, Byte value) {
            try {
                setter.invoke(object, value);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Field accessor implementation for Character fields using MethodHandle.
     * Provides efficient Character field access operations.
     */
    static final class FieldAccessorMethodHandleCharacter
            extends FieldAccessorMethodHandle
            implements PropertyAccessorCharacter
    {
        public FieldAccessorMethodHandleCharacter(Field field, MethodHandle getter, MethodHandle setter) {
            super(field, getter, setter);
        }
        @Override
        public Character getCharacter(Object object) {
            try {
                return (Character) getter.invoke(object);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        @Override
        public void setCharacter(Object object, Character value) {
            try {
                setter.invoke(object, value);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Field accessor implementation for Short fields using MethodHandle.
     * Provides efficient Short field access operations.
     */
    static final class FieldAccessorMethodHandleShort
            extends FieldAccessorMethodHandle
            implements PropertyAccessorShort
    {
        public FieldAccessorMethodHandleShort(Field field, MethodHandle getter, MethodHandle setter) {
            super(field, getter, setter);
        }
        @Override
        public Short getShort(Object object) {
            try {
                return (Short) getter.invoke(object);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        @Override
        public void setShort(Object object, Short value) {
            try {
                setter.invoke(object, value);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Field accessor implementation for Integer fields using MethodHandle.
     * Provides efficient Integer field access operations.
     */
    static final class FieldAccessorMethodHandleInteger
            extends FieldAccessorMethodHandle
            implements PropertyAccessorInteger
    {
        public FieldAccessorMethodHandleInteger(Field field, MethodHandle getter, MethodHandle setter) {
            super(field, getter, setter);
        }
        @Override
        public Integer getInteger(Object object) {
            try {
                return (Integer) getter.invoke(object);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        @Override
        public void setInteger(Object object, Integer value) {
            try {
                setter.invoke(object, value);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Field accessor implementation for Long fields using MethodHandle.
     * Provides efficient Long field access operations.
     */
    static final class FieldAccessorMethodHandleLong
            extends FieldAccessorMethodHandle
            implements PropertyAccessorLong
    {
        public FieldAccessorMethodHandleLong(Field field, MethodHandle getter, MethodHandle setter) {
            super(field, getter, setter);
        }
        @Override
        public Long getLong(Object object) {
            try {
                return (Long) getter.invoke(object);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        @Override
        public void setLong(Object object, Long value) {
            try {
                setter.invoke(object, value);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Field accessor implementation for Float fields using MethodHandle.
     * Provides efficient Float field access operations.
     */
    static final class FieldAccessorMethodHandleFloat
            extends FieldAccessorMethodHandle
            implements PropertyAccessorFloat
    {
        public FieldAccessorMethodHandleFloat(Field field, MethodHandle getter, MethodHandle setter) {
            super(field, getter, setter);
        }
        @Override
        public Float getFloat(Object object) {
            try {
                return (Float) getter.invoke(object);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        @Override
        public void setFloat(Object object, Float value) {
            try {
                setter.invoke(object, value);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Field accessor implementation for Double fields using MethodHandle.
     * Provides efficient Double field access operations.
     */
    static final class FieldAccessorMethodHandleDouble
            extends FieldAccessorMethodHandle
            implements PropertyAccessorDouble
    {
        public FieldAccessorMethodHandleDouble(Field field, MethodHandle getter, MethodHandle setter) {
            super(field, getter, setter);
        }
        @Override
        public Double getDouble(Object object) {
            try {
                return (Double) getter.invoke(object);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        @Override
        public void setDouble(Object object, Double value) {
            try {
                setter.invoke(object, value);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Field accessor implementation for long fields using MethodHandle.
     * Provides efficient long field access operations.
     */
    static final class FieldAccessorMethodHandleLongValue
            extends FieldAccessorMethodHandle
            implements PropertyAccessorLongValue
    {
        public FieldAccessorMethodHandleLongValue(Field field, MethodHandle getter, MethodHandle setter) {
            super(field, getter, setter);
        }
        @Override
        public long getLongValue(Object object) {
            try {
                return (long) getter.invoke(object);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        @Override
        public void setLongValue(Object object, long value) {
            try {
                setter.invoke(object, value);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Field accessor implementation for float fields using MethodHandle.
     * Provides efficient float field access operations.
     */
    static final class FieldAccessorMethodHandleFloatValue
            extends FieldAccessorMethodHandle
            implements PropertyAccessorFloatValue
    {
        public FieldAccessorMethodHandleFloatValue(Field field, MethodHandle getter, MethodHandle setter) {
            super(field, getter, setter);
        }
        @Override
        public float getFloatValue(Object object) {
            try {
                return (float) getter.invoke(object);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        @Override
        public void setFloatValue(Object object, float value) {
            try {
                setter.invoke(object, value);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Field accessor implementation for double fields using MethodHandle.
     * Provides efficient double field access operations.
     */
    static final class FieldAccessorMethodHandleDoubleValue
            extends FieldAccessorMethodHandle
            implements PropertyAccessorDoubleValue
    {
        public FieldAccessorMethodHandleDoubleValue(Field field, MethodHandle getter, MethodHandle setter) {
            super(field, getter, setter);
        }
        @Override
        public double getDoubleValue(Object object) {
            try {
                return (double) getter.invoke(object);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        @Override
        public void setDoubleValue(Object object, double value) {
            try {
                setter.invoke(object, value);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Field accessor implementation for Object fields using MethodHandle.
     * Provides efficient object field access operations for reference types.
     */
    static final class FieldAccessorMethodHandleObject extends FieldAccessorMethodHandle
            implements PropertyAccessorObject {
        public FieldAccessorMethodHandleObject(Field field, MethodHandle getter, MethodHandle setter) {
            super(field, getter, setter);
        }
        @Override
        public Object getObject(Object object) {
            try {
                return getter.invoke(object);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        @Override
        public void setObject(Object object, Object value) {
            try {
                setter.invoke(object, value);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Field accessor implementation for String fields using MethodHandle.
     * Provides efficient String field access operations.
     */
    static final class FieldAccessorMethodHandleString extends FieldAccessorMethodHandle
            implements PropertyAccessorString {
        public FieldAccessorMethodHandleString(Field field, MethodHandle getter, MethodHandle setter) {
            super(field, getter, setter);
        }
        @Override
        public String getString(Object object) {
            try {
                return (String) getter.invoke(object);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        @Override
        public void setString(Object object, String value) {
            try {
                setter.invoke(object, value);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Field accessor implementation for BigInteger fields using MethodHandle.
     * Provides efficient BigInteger field access operations.
     */
    static final class FieldAccessorMethodHandleBigInteger extends FieldAccessorMethodHandle
            implements PropertyAccessorBigInteger {
        public FieldAccessorMethodHandleBigInteger(Field field, MethodHandle getter, MethodHandle setter) {
            super(field, getter, setter);
        }
        @Override
        public BigInteger getBigInteger(Object object) {
            try {
                return (BigInteger) getter.invoke(object);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        @Override
        public void setBigInteger(Object object, BigInteger value) {
            try {
                setter.invoke(object, value);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Field accessor implementation for BigDecimal fields using MethodHandle.
     * Provides efficient BigDecimal field access operations.
     */
    static final class FieldAccessorMethodHandleBigDecimal extends FieldAccessorMethodHandle
            implements PropertyAccessorBigDecimal {
        public FieldAccessorMethodHandleBigDecimal(Field field, MethodHandle getter, MethodHandle setter) {
            super(field, getter, setter);
        }
        @Override
        public BigDecimal getBigDecimal(Object object) {
            try {
                return (BigDecimal) getter.invoke(object);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        @Override
        public void setBigDecimal(Object object, BigDecimal value) {
            try {
                setter.invoke(object, value);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Field accessor implementation for Number fields using MethodHandle.
     * Provides efficient Number field access operations.
     */
    static final class FieldAccessorMethodHandleNumber
            extends FieldAccessorMethodHandle
            implements PropertyAccessorObject {
        public FieldAccessorMethodHandleNumber(Field field, MethodHandle getter, MethodHandle setter) {
            super(field, getter, setter);
        }

        @Override
        public Number getObject(Object object) {
            try {
                return (Number) getter.invoke(object);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void setObject(Object object, Object value) {
            try {
                setter.invoke(object, value);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public byte getByteValue(Object object) {
            Number num = (Number) getObject(object);
            return num != null ? num.byteValue() : 0;
        }

        @Override
        public char getCharValue(Object object) {
            Number num = (Number) getObject(object);
            return num != null ? (char) num.intValue() : 0;
        }

        @Override
        public short getShortValue(Object object) {
            Number num = (Number) getObject(object);
            return num != null ? num.shortValue() : 0;
        }

        @Override
        public int getIntValue(Object object) {
            Number num = (Number) getObject(object);
            return num != null ? num.intValue() : 0;
        }

        @Override
        public long getLongValue(Object object) {
            Number num = (Number) getObject(object);
            return num != null ? num.longValue() : 0L;
        }

        @Override
        public float getFloatValue(Object object) {
            Number num = (Number) getObject(object);
            return num != null ? num.floatValue() : 0.0f;
        }

        @Override
        public double getDoubleValue(Object object) {
            Number num = (Number) getObject(object);
            return num != null ? num.doubleValue() : 0.0;
        }

        @Override
        public boolean getBooleanValue(Object object) {
            Number num = (Number) getObject(object);
            return num != null && num.intValue() != 0;
        }

        @Override
        public void setByteValue(Object object, byte value) {
            setObject(object, value);
        }

        @Override
        public void setCharValue(Object object, char value) {
            setObject(object, (int) value);
        }

        @Override
        public void setShortValue(Object object, short value) {
            setObject(object, value);
        }

        @Override
        public void setIntValue(Object object, int value) {
            setObject(object, value);
        }

        @Override
        public void setLongValue(Object object, long value) {
            setObject(object, value);
        }

        @Override
        public void setFloatValue(Object object, float value) {
            setObject(object, value);
        }

        @Override
        public void setDoubleValue(Object object, double value) {
            setObject(object, value);
        }

        @Override
        public void setBooleanValue(Object object, boolean value) {
            setObject(object, value ? 1 : 0);
        }
    }
}
