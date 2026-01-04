package com.alibaba.fastjson2.internal;

import com.alibaba.fastjson2.util.JDKUtils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Property accessor factory that uses MethodHandles.Lookup for field access.
 * This implementation uses MethodHandles.Lookup's unreflectGetter/unreflectSetter
 * instead of VarHandle for field access, providing an alternative way to access
 * object properties efficiently.
 */
public class PropertyAccessorFactoryMethodHandle
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

        // Additional methods required by PropertyAccessorObject hierarchy that PropertyAccessorNumber implements
        public Number getNumber(Object object) {
            return (Number) getObject(object);
        }

        public void setNumber(Object object, Number value) {
            setObject(object, value);
        }

        public String getString(Object object) {
            Number num = (Number) getObject(object);
            return num != null ? num.toString() : null;
        }

        public void setString(Object object, String value) {
            setObject(object, value != null ? Double.valueOf(value) : null);
        }

        public BigInteger getBigInteger(Object object) {
            Number num = (Number) getObject(object);
            return num != null ? BigInteger.valueOf(num.longValue()) : null;
        }

        public void setBigInteger(Object object, BigInteger value) {
            setObject(object, value != null ? value : null);
        }

        public BigDecimal getBigDecimal(Object object) {
            Number num = (Number) getObject(object);
            return num != null ? new BigDecimal(num.toString()) : null;
        }

        public void setBigDecimal(Object object, BigDecimal value) {
            setObject(object, value != null ? value : null);
        }
    }
}
