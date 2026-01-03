package com.alibaba.fastjson2.internal;

import com.alibaba.fastjson2.util.JDKUtils;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Property accessor factory that uses VarHandle for field access.
 * This implementation is available on JDK 11+ and provides high-performance
 * property access using the VarHandle API which is more efficient than
 * traditional reflection or Unsafe-based approaches.
 */
public class PropertyAccessorFactoryVarHandle
        extends PropertyAccessorFactoryLambda {
    public PropertyAccessorFactoryVarHandle() {
    }

    protected MethodHandles.Lookup lookup(Class<?> declaringClass) {
        if (Conf.USE_UNSAFE) {
            return JDKUtils.trustedLookup(declaringClass);
        } else {
            return MethodHandles.lookup().in(declaringClass);
        }
    }

    /**
     * Creates a property accessor for the given field using VarHandle-based implementation.
     * Different accessor implementations are created based on the field type to provide
     * optimal performance for each specific type.
     *
     * @param field the field for which to create an accessor
     * @return a property accessor appropriate for the field type
     */
    protected PropertyAccessor createInternal(Field field) {
        MethodHandles.Lookup lookup = lookup(field.getDeclaringClass());
        VarHandle varHandle;
        Class<?> fieldType = field.getType();
        try {
            varHandle = lookup.findVarHandle(field.getDeclaringClass(), field.getName(), fieldType);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // ignore
            return super.createInternal(field);
        }

        if (fieldType == byte.class) {
            return new FieldAccessorVarHandleByteValue(field, varHandle);
        }
        if (fieldType == short.class) {
            return new FieldAccessorVarHandleShortValue(field, varHandle);
        }
        if (fieldType == int.class) {
            return new FieldAccessorVarHandleIntValue(field, varHandle);
        }
        if (fieldType == long.class) {
            return new FieldAccessorVarHandleLongValue(field, varHandle);
        }
        if (fieldType == float.class) {
            return new FieldAccessorVarHandleFloatValue(field, varHandle);
        }
        if (fieldType == double.class) {
            return new FieldAccessorVarHandleDoubleValue(field, varHandle);
        }
        if (fieldType == boolean.class) {
            return new FieldAccessorVarHandleBooleanValue(field, varHandle);
        }
        if (fieldType == char.class) {
            return new FieldAccessorVarHandleCharValue(field, varHandle);
        }
        if (fieldType == String.class) {
            return new FieldAccessorVarHandleString(field, varHandle);
        }
        if (fieldType == BigInteger.class) {
            return new FieldAccessorVarHandleBigInteger(field, varHandle);
        }
        if (fieldType == BigDecimal.class) {
            return new FieldAccessorVarHandleBigDecimal(field, varHandle);
        }
        if (fieldType == Boolean.class) {
            return new FieldAccessorVarHandleBoolean(field, varHandle);
        }
        if (fieldType == Byte.class) {
            return new FieldAccessorVarHandleByte(field, varHandle);
        }
        if (fieldType == Character.class) {
            return new FieldAccessorVarHandleCharacter(field, varHandle);
        }
        if (fieldType == Short.class) {
            return new FieldAccessorVarHandleShort(field, varHandle);
        }
        if (fieldType == Integer.class) {
            return new FieldAccessorVarHandleInteger(field, varHandle);
        }
        if (fieldType == Long.class) {
            return new FieldAccessorVarHandleLong(field, varHandle);
        }
        if (fieldType == Float.class) {
            return new FieldAccessorVarHandleFloat(field, varHandle);
        }
        if (fieldType == Double.class) {
            return new FieldAccessorVarHandleDouble(field, varHandle);
        }
        return new FieldAccessorVarHandleObject(field, varHandle);
    }

    /**
     * Base class for VarHandle-based field accessors.
     * Uses VarHandle for efficient field access which is faster than traditional reflection.
     */
    abstract static class FieldAccessorVarHandle extends FieldAccessor {
        final VarHandle varHandle;

        /**
         * Creates a field accessor using VarHandle for the given field.
         *
         * @param field the field to access
         * @param varHandle the VarHandle to use for field access
         */
        public FieldAccessorVarHandle(Field field, VarHandle varHandle) {
            super(field);
            this.varHandle = varHandle;
        }
    }

    /**
     * Field accessor implementation for boolean fields using VarHandle.
     * Provides efficient boolean field access operations.
     */
    static final class FieldAccessorVarHandleBooleanValue
            extends FieldAccessorVarHandle
            implements PropertyAccessorBooleanValue
    {
        public FieldAccessorVarHandleBooleanValue(Field field, VarHandle varHandle) {
            super(field, varHandle);
        }

        @Override
        public boolean getBooleanValue(Object object) {
            return (boolean) varHandle.get(object);
        }

        @Override
        public void setBooleanValue(Object object, boolean value) {
            varHandle.set(object, value);
        }
    }

    /**
     * Field accessor implementation for byte fields using VarHandle.
     * Provides efficient byte field access operations.
     */
    static final class FieldAccessorVarHandleByteValue
            extends FieldAccessorVarHandle
            implements PropertyAccessorByteValue
    {
        public FieldAccessorVarHandleByteValue(Field field, VarHandle varHandle) {
            super(field, varHandle);
        }
        @Override
        public byte getByteValue(Object object) {
            return (byte) varHandle.get(object);
        }
        @Override
        public void setByteValue(Object object, byte value) {
            varHandle.set(object, value);
        }
    }

    /**
     * Field accessor implementation for char fields using VarHandle.
     * Provides efficient char field access operations.
     */
    static final class FieldAccessorVarHandleCharValue
            extends FieldAccessorVarHandle
            implements PropertyAccessorCharValue
    {
        public FieldAccessorVarHandleCharValue(Field field, VarHandle varHandle) {
            super(field, varHandle);
        }
        @Override
        public char getCharValue(Object object) {
            return (char) varHandle.get(object);
        }
        @Override
        public void setCharValue(Object object, char value) {
            varHandle.set(object, value);
        }
    }

    /**
     * Field accessor implementation for short fields using VarHandle.
     * Provides efficient short field access operations.
     */
    static final class FieldAccessorVarHandleShortValue
            extends FieldAccessorVarHandle
            implements PropertyAccessorShortValue
    {
        public FieldAccessorVarHandleShortValue(Field field, VarHandle varHandle) {
            super(field, varHandle);
        }
        @Override
        public short getShortValue(Object object) {
            return (short) varHandle.get(object);
        }
        @Override
        public void setShortValue(Object object, short value) {
            varHandle.set(object, value);
        }
    }

    /**
     * Field accessor implementation for int fields using VarHandle.
     * Provides efficient int field access operations.
     */
    static final class FieldAccessorVarHandleIntValue
            extends FieldAccessorVarHandle
            implements PropertyAccessorIntValue
    {
        public FieldAccessorVarHandleIntValue(Field field, VarHandle varHandle) {
            super(field, varHandle);
        }
        @Override
        public int getIntValue(Object object) {
            return (int) varHandle.get(object);
        }
        @Override
        public void setIntValue(Object object, int value) {
            varHandle.set(object, value);
        }
    }

    /**
     * Field accessor implementation for Boolean fields using VarHandle.
     * Provides efficient Boolean field access operations.
     */
    static final class FieldAccessorVarHandleBoolean
            extends FieldAccessorVarHandle
            implements PropertyAccessorBoolean
    {
        public FieldAccessorVarHandleBoolean(Field field, VarHandle varHandle) {
            super(field, varHandle);
        }
        @Override
        public Boolean getBoolean(Object object) {
            return (Boolean) varHandle.get(object);
        }
        @Override
        public void setBoolean(Object object, Boolean value) {
            varHandle.set(object, value);
        }
    }

    /**
     * Field accessor implementation for Byte fields using VarHandle.
     * Provides efficient Byte field access operations.
     */
    static final class FieldAccessorVarHandleByte
            extends FieldAccessorVarHandle
            implements PropertyAccessorByte
    {
        public FieldAccessorVarHandleByte(Field field, VarHandle varHandle) {
            super(field, varHandle);
        }
        @Override
        public Byte getByte(Object object) {
            return (Byte) varHandle.get(object);
        }
        @Override
        public void setByte(Object object, Byte value) {
            varHandle.set(object, value);
        }
    }

    /**
     * Field accessor implementation for Character fields using VarHandle.
     * Provides efficient Character field access operations.
     */
    static final class FieldAccessorVarHandleCharacter
            extends FieldAccessorVarHandle
            implements PropertyAccessorCharacter
    {
        public FieldAccessorVarHandleCharacter(Field field, VarHandle varHandle) {
            super(field, varHandle);
        }
        @Override
        public Character getCharacter(Object object) {
            return (Character) varHandle.get(object);
        }
        @Override
        public void setCharacter(Object object, Character value) {
            varHandle.set(object, value);
        }
    }

    /**
     * Field accessor implementation for Short fields using VarHandle.
     * Provides efficient Short field access operations.
     */
    static final class FieldAccessorVarHandleShort
            extends FieldAccessorVarHandle
            implements PropertyAccessorShort
    {
        public FieldAccessorVarHandleShort(Field field, VarHandle varHandle) {
            super(field, varHandle);
        }
        @Override
        public Short getShort(Object object) {
            return (Short) varHandle.get(object);
        }
        @Override
        public void setShort(Object object, Short value) {
            varHandle.set(object, value);
        }
    }

    /**
     * Field accessor implementation for Integer fields using VarHandle.
     * Provides efficient Integer field access operations.
     */
    static final class FieldAccessorVarHandleInteger
            extends FieldAccessorVarHandle
            implements PropertyAccessorInteger
    {
        public FieldAccessorVarHandleInteger(Field field, VarHandle varHandle) {
            super(field, varHandle);
        }
        @Override
        public Integer getInteger(Object object) {
            return (Integer) varHandle.get(object);
        }
        @Override
        public void setInteger(Object object, Integer value) {
            varHandle.set(object, value);
        }
    }

    /**
     * Field accessor implementation for Long fields using VarHandle.
     * Provides efficient Long field access operations.
     */
    static final class FieldAccessorVarHandleLong
            extends FieldAccessorVarHandle
            implements PropertyAccessorLong
    {
        public FieldAccessorVarHandleLong(Field field, VarHandle varHandle) {
            super(field, varHandle);
        }
        @Override
        public Long getLong(Object object) {
            return (Long) varHandle.get(object);
        }
        @Override
        public void setLong(Object object, Long value) {
            varHandle.set(object, value);
        }
    }

    /**
     * Field accessor implementation for Float fields using VarHandle.
     * Provides efficient Float field access operations.
     */
    static final class FieldAccessorVarHandleFloat
            extends FieldAccessorVarHandle
            implements PropertyAccessorFloat
    {
        public FieldAccessorVarHandleFloat(Field field, VarHandle varHandle) {
            super(field, varHandle);
        }
        @Override
        public Float getFloat(Object object) {
            return (Float) varHandle.get(object);
        }
        @Override
        public void setFloat(Object object, Float value) {
            varHandle.set(object, value);
        }
    }

    /**
     * Field accessor implementation for Double fields using VarHandle.
     * Provides efficient Double field access operations.
     */
    static final class FieldAccessorVarHandleDouble
            extends FieldAccessorVarHandle
            implements PropertyAccessorDouble
    {
        public FieldAccessorVarHandleDouble(Field field, VarHandle varHandle) {
            super(field, varHandle);
        }
        @Override
        public Double getDouble(Object object) {
            return (Double) varHandle.get(object);
        }
        @Override
        public void setDouble(Object object, Double value) {
            varHandle.set(object, value);
        }
    }

    /**
     * Field accessor implementation for long fields using VarHandle.
     * Provides efficient long field access operations.
     */
    static final class FieldAccessorVarHandleLongValue
            extends FieldAccessorVarHandle
            implements PropertyAccessorLongValue
    {
        public FieldAccessorVarHandleLongValue(Field field, VarHandle varHandle) {
            super(field, varHandle);
        }
        @Override
        public long getLongValue(Object object) {
            return (long) varHandle.get(object);
        }
        @Override
        public void setLongValue(Object object, long value) {
            varHandle.set(object, value);
        }
    }

    /**
     * Field accessor implementation for float fields using VarHandle.
     * Provides efficient float field access operations.
     */
    static final class FieldAccessorVarHandleFloatValue
            extends FieldAccessorVarHandle
            implements PropertyAccessorFloatValue
    {
        public FieldAccessorVarHandleFloatValue(Field field, VarHandle varHandle) {
            super(field, varHandle);
        }
        @Override
        public float getFloatValue(Object object) {
            return (float) varHandle.get(object);
        }
        @Override
        public void setFloatValue(Object object, float value) {
            varHandle.set(object, value);
        }
    }

    /**
     * Field accessor implementation for double fields using VarHandle.
     * Provides efficient double field access operations.
     */
    static final class FieldAccessorVarHandleDoubleValue
            extends FieldAccessorVarHandle
            implements PropertyAccessorDoubleValue
    {
        public FieldAccessorVarHandleDoubleValue(Field field, VarHandle varHandle) {
            super(field, varHandle);
        }
        @Override
        public double getDoubleValue(Object object) {
            return (double) varHandle.get(object);
        }
        @Override
        public void setDoubleValue(Object object, double value) {
            varHandle.set(object, value);
        }
    }

    /**
     * Field accessor implementation for Object fields using VarHandle.
     * Provides efficient object field access operations for reference types.
     */
    static final class FieldAccessorVarHandleObject extends FieldAccessorVarHandle
            implements PropertyAccessorObject {
        public FieldAccessorVarHandleObject(Field field, VarHandle varHandle) {
            super(field, varHandle);
        }
        @Override
        public Object getObject(Object object) {
            return varHandle.get(object);
        }
        @Override
        public void setObject(Object object, Object value) {
            varHandle.set(object, value);
        }
    }

    /**
     * Field accessor implementation for String fields using VarHandle.
     * Provides efficient String field access operations.
     */
    static final class FieldAccessorVarHandleString extends FieldAccessorVarHandle
            implements PropertyAccessorString {
        public FieldAccessorVarHandleString(Field field, VarHandle varHandle) {
            super(field, varHandle);
        }
        @Override
        public String getString(Object object) {
            return (String) varHandle.get(object);
        }
        @Override
        public void setString(Object object, String value) {
            varHandle.set(object, value);
        }
    }

    /**
     * Field accessor implementation for BigInteger fields using VarHandle.
     * Provides efficient BigInteger field access operations.
     */
    static final class FieldAccessorVarHandleBigInteger extends FieldAccessorVarHandle
            implements PropertyAccessorBigInteger {
        public FieldAccessorVarHandleBigInteger(Field field, VarHandle varHandle) {
            super(field, varHandle);
        }
        @Override
        public BigInteger getBigInteger(Object object) {
            return (BigInteger) varHandle.get(object);
        }
        @Override
        public void setBigInteger(Object object, BigInteger value) {
            varHandle.set(object, value);
        }
    }

    /**
     * Field accessor implementation for BigDecimal fields using VarHandle.
     * Provides efficient BigDecimal field access operations.
     */
    static final class FieldAccessorVarHandleBigDecimal extends FieldAccessorVarHandle
            implements PropertyAccessorBigDecimal {
        public FieldAccessorVarHandleBigDecimal(Field field, VarHandle varHandle) {
            super(field, varHandle);
        }
        @Override
        public BigDecimal getBigDecimal(Object object) {
            return (BigDecimal) varHandle.get(object);
        }
        @Override
        public void setBigDecimal(Object object, BigDecimal value) {
            varHandle.set(object, value);
        }
    }
}
