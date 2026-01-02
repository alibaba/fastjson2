package com.alibaba.fastjson2.internal;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.function.*;
import com.alibaba.fastjson2.util.JDKUtils;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.*;

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
        try {
            varHandle = lookup.findVarHandle(field.getDeclaringClass(), field.getName(), field.getType());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new JSONException("findVarHandle error", e);
        }

        if (field.getType() == byte.class) {
            return new FieldAccessorVarHandleByte(field, varHandle);
        }
        if (field.getType() == short.class) {
            return new FieldAccessorVarHandleShort(field, varHandle);
        }
        if (field.getType() == int.class) {
            return new FieldAccessorVarHandleInt(field, varHandle);
        }
        if (field.getType() == long.class) {
            return new FieldAccessorVarHandleLong(field, varHandle);
        }
        if (field.getType() == float.class) {
            return new FieldAccessorVarHandleFloat(field, varHandle);
        }
        if (field.getType() == double.class) {
            return new FieldAccessorVarHandleDouble(field, varHandle);
        }
        if (field.getType() == boolean.class) {
            return new FieldAccessorVarHandleBoolean(field, varHandle);
        }
        if (field.getType() == char.class) {
            return new FieldAccessorVarHandleChar(field, varHandle);
        }
        if (field.getType() == String.class) {
            return new FieldAccessorVarHandleString(field, varHandle);
        }
        if (field.getType() == BigInteger.class) {
            return new FieldAccessorVarHandleBigInteger(field, varHandle);
        }
        if (field.getType() == BigDecimal.class) {
            return new FieldAccessorVarHandleBigDecimal(field, varHandle);
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
    static final class FieldAccessorVarHandleBoolean extends FieldAccessorVarHandle
            implements PropertyAccessorBoolean {
        public FieldAccessorVarHandleBoolean(Field field, VarHandle varHandle) {
            super(field, varHandle);
        }

        @Override
        public boolean getBoolean(Object object) {
            return (boolean) varHandle.get(object);
        }

        @Override
        public void setBoolean(Object object, boolean value) {
            varHandle.set(object, value);
        }
    }

    /**
     * Field accessor implementation for byte fields using VarHandle.
     * Provides efficient byte field access operations.
     */
    static final class FieldAccessorVarHandleByte extends FieldAccessorVarHandle
            implements PropertyAccessorByte {
        public FieldAccessorVarHandleByte(Field field, VarHandle varHandle) {
            super(field, varHandle);
        }
        @Override
        public byte getByte(Object object) {
            return (byte) varHandle.get(object);
        }
        @Override
        public void setByte(Object object, byte value) {
            varHandle.set(object, value);
        }
    }

    /**
     * Field accessor implementation for char fields using VarHandle.
     * Provides efficient char field access operations.
     */
    static final class FieldAccessorVarHandleChar extends FieldAccessorVarHandle
            implements PropertyAccessorChar {
        public FieldAccessorVarHandleChar(Field field, VarHandle varHandle) {
            super(field, varHandle);
        }
        @Override
        public char getChar(Object object) {
            return (char) varHandle.get(object);
        }
        @Override
        public void setChar(Object object, char value) {
            varHandle.set(object, value);
        }
    }

    /**
     * Field accessor implementation for short fields using VarHandle.
     * Provides efficient short field access operations.
     */
    static final class FieldAccessorVarHandleShort extends FieldAccessorVarHandle
            implements PropertyAccessorShort {
        public FieldAccessorVarHandleShort(Field field, VarHandle varHandle) {
            super(field, varHandle);
        }
        @Override
        public short getShort(Object object) {
            return (short) varHandle.get(object);
        }
        @Override
        public void setShort(Object object, short value) {
            varHandle.set(object, value);
        }
    }

    /**
     * Field accessor implementation for int fields using VarHandle.
     * Provides efficient int field access operations.
     */
    static final class FieldAccessorVarHandleInt extends FieldAccessorVarHandle
            implements PropertyAccessorInt {
        public FieldAccessorVarHandleInt(Field field, VarHandle varHandle) {
            super(field, varHandle);
        }
        @Override
        public int getInt(Object object) {
            return (int) varHandle.get(object);
        }
        @Override
        public void setInt(Object object, int value) {
            varHandle.set(object, value);
        }
    }

    /**
     * Field accessor implementation for long fields using VarHandle.
     * Provides efficient long field access operations.
     */
    static final class FieldAccessorVarHandleLong extends FieldAccessorVarHandle
            implements PropertyAccessorLong {
        public FieldAccessorVarHandleLong(Field field, VarHandle varHandle) {
            super(field, varHandle);
        }
        @Override
        public long getLong(Object object) {
            return (long) varHandle.get(object);
        }
        @Override
        public void setLong(Object object, long value) {
            varHandle.set(object, value);
        }
    }

    /**
     * Field accessor implementation for float fields using VarHandle.
     * Provides efficient float field access operations.
     */
    static final class FieldAccessorVarHandleFloat extends FieldAccessorVarHandle
            implements PropertyAccessorFloat {
        public FieldAccessorVarHandleFloat(Field field, VarHandle varHandle) {
            super(field, varHandle);
        }
        @Override
        public float getFloat(Object object) {
            return (float) varHandle.get(object);
        }
        @Override
        public void setFloat(Object object, float value) {
            varHandle.set(object, value);
        }
    }

    /**
     * Field accessor implementation for double fields using VarHandle.
     * Provides efficient double field access operations.
     */
    static final class FieldAccessorVarHandleDouble extends FieldAccessorVarHandle
            implements PropertyAccessorDouble {
        public FieldAccessorVarHandleDouble(Field field, VarHandle varHandle) {
            super(field, varHandle);
        }
        @Override
        public double getDouble(Object object) {
            return (double) varHandle.get(object);
        }
        @Override
        public void setDouble(Object object, double value) {
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
