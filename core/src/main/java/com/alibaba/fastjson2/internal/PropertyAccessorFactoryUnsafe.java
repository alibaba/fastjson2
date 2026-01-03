package com.alibaba.fastjson2.internal;

import com.alibaba.fastjson2.JSONException;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

import static com.alibaba.fastjson2.util.JDKUtils.UNSAFE;

/**
 * A specialized property accessor factory that uses Unsafe operations for field access
 * to provide better performance compared to reflection-based access.
 * This factory creates property accessors that use direct memory access via Unsafe
 * to get and set field values, which is faster than traditional reflection.
 * <p>
 * Note: This factory is named "Unsafe" because it uses sun.misc.Unsafe, which is
 * not part of the standard Java API and may not be available in all JVM implementations.
 * </p>
 * <p>
 * This class extends PropertyAccessorFactory and overrides specific methods to provide
 * Unsafe-based implementations for field access while maintaining compatibility with
 * the parent class's method-based and function-based access patterns.
 * </p>
 */
@SuppressWarnings("ALL")
public final class PropertyAccessorFactoryUnsafe
        extends PropertyAccessorFactoryLambda {
    /**
     * Creates an Unsafe-based property accessor for the specified field.
     * This method analyzes the field type and returns an appropriate
     * accessor implementation optimized for that type using Unsafe operations.
     *
     * @param field the field to create an accessor for
     * @return a PropertyAccessor instance for the specified field using Unsafe operations
     */
    protected PropertyAccessor createInternal(Field field) {
        if (field.getType() == byte.class) {
            return new FieldAccessorUnsafeByteValue(field);
        }
        if (field.getType() == short.class) {
            return new FieldAccessorUnsafeShortValue(field);
        }
        if (field.getType() == int.class) {
            return new FieldAccessorUnsafeIntValue(field);
        }
        if (field.getType() == long.class) {
            return new FieldAccessorUnsafeLongValue(field);
        }
        if (field.getType() == float.class) {
            return new FieldAccessorUnsafeFloatValue(field);
        }
        if (field.getType() == double.class) {
            return new FieldAccessorUnsafeDoubleValue(field);
        }
        if (field.getType() == boolean.class) {
            return new FieldAccessorUnsafeBooleanValue(field);
        }
        if (field.getType() == char.class) {
            return new FieldAccessorUnsafeCharValue(field);
        }
        if (field.getType() == String.class) {
            return new FieldAccessorUnsafeString(field);
        }
        if (field.getType() == BigInteger.class) {
            return new FieldAccessorUnsafeBigInteger(field);
        }
        if (field.getType() == BigDecimal.class) {
            return new FieldAccessorUnsafeBigDecimal(field);
        }
        return new FieldAccessorUnsafeObject(field);
    }

    /**
     * Abstract base class for Unsafe-based field accessors.
     * Provides common functionality for accessing fields using Unsafe operations,
     * which are faster than traditional reflection-based access.
     */
    abstract static class FieldAccessorUnsafe extends FieldAccessor {
        final long fieldOffset;
        public FieldAccessorUnsafe(Field field) {
            super(field);
            this.fieldOffset = UNSAFE.objectFieldOffset(field);
        }
    }

    /**
     * Unsafe-based field accessor implementation for boolean-typed properties.
     * Provides efficient getter and setter operations for boolean fields using Unsafe operations.
     */
    static final class FieldAccessorUnsafeBooleanValue
            extends FieldAccessorUnsafe implements PropertyAccessorBooleanValue
    {
        public FieldAccessorUnsafeBooleanValue(Field field) {
            super(field);
        }

        @Override
        public boolean getBooleanValue(Object object) {
            return UNSAFE.getBoolean(Objects.requireNonNull(object), fieldOffset);
        }

        @Override
        public void setBooleanValue(Object object, boolean value) {
            UNSAFE.putBoolean(Objects.requireNonNull(object), fieldOffset, value);
        }
    }

    /**
     * Unsafe-based field accessor implementation for byte-typed properties.
     * Provides efficient getter and setter operations for byte fields using Unsafe operations.
     */
    static final class FieldAccessorUnsafeByteValue
            extends FieldAccessorUnsafe implements PropertyAccessorByteValue
    {
        public FieldAccessorUnsafeByteValue(Field field) {
            super(field);
        }

        @Override
        public byte getByteValue(Object object) {
            return UNSAFE.getByte(Objects.requireNonNull(object), fieldOffset);
        }

        @Override
        public void setByteValue(Object object, byte value) {
            UNSAFE.putByte(Objects.requireNonNull(object), fieldOffset, value);
        }
    }

    /**
     * Unsafe-based field accessor implementation for short-typed properties.
     * Provides efficient getter and setter operations for short fields using Unsafe operations.
     */
    static final class FieldAccessorUnsafeShortValue
            extends FieldAccessorUnsafe implements PropertyAccessorShortValue
    {
        public FieldAccessorUnsafeShortValue(Field field) {
            super(field);
        }

        @Override
        public short getShortValue(Object object) {
            return UNSAFE.getShort(Objects.requireNonNull(object), fieldOffset);
        }

        @Override
        public void setShortValue(Object object, short value) {
            UNSAFE.putShort(Objects.requireNonNull(object), fieldOffset, value);
        }
    }

    /**
     * Unsafe-based field accessor implementation for int-typed properties.
     * Provides efficient getter and setter operations for int fields using Unsafe operations.
     */
    static final class FieldAccessorUnsafeIntValue
            extends FieldAccessorUnsafe implements PropertyAccessorIntValue
    {
        public FieldAccessorUnsafeIntValue(Field field) {
            super(field);
        }

        @Override
        public int getIntValue(Object object) {
            return UNSAFE.getInt(Objects.requireNonNull(object), fieldOffset);
        }

        @Override
        public void setIntValue(Object object, int value) {
            UNSAFE.putInt(Objects.requireNonNull(object), fieldOffset, value);
        }
    }

    /**
     * Unsafe-based field accessor implementation for long-typed properties.
     * Provides efficient getter and setter operations for long fields using Unsafe operations.
     */
    static final class FieldAccessorUnsafeLongValue
            extends FieldAccessorUnsafe implements PropertyAccessorLongValue
    {
        public FieldAccessorUnsafeLongValue(Field field) {
            super(field);
        }

        @Override
        public long getLongValue(Object object) {
            return UNSAFE.getLong(Objects.requireNonNull(object), fieldOffset);
        }

        @Override
        public void setLongValue(Object object, long value) {
            UNSAFE.putLong(Objects.requireNonNull(object), fieldOffset, value);
        }
    }

    /**
     * Unsafe-based field accessor implementation for float-typed properties.
     * Provides efficient getter and setter operations for float fields using Unsafe operations.
     */
    static final class FieldAccessorUnsafeFloatValue
            extends FieldAccessorUnsafe implements PropertyAccessorFloatValue
    {
        public FieldAccessorUnsafeFloatValue(Field field) {
            super(field);
        }

        @Override
        public float getFloatValue(Object object) {
            return UNSAFE.getFloat(Objects.requireNonNull(object), fieldOffset);
        }

        @Override
        public void setFloatValue(Object object, float value) {
            UNSAFE.putFloat(Objects.requireNonNull(object), fieldOffset, value);
        }
    }

    /**
     * Unsafe-based field accessor implementation for double-typed properties.
     * Provides efficient getter and setter operations for double fields using Unsafe operations.
     */
    static final class FieldAccessorUnsafeDoubleValue
            extends FieldAccessorUnsafe implements PropertyAccessorDoubleValue
    {
        public FieldAccessorUnsafeDoubleValue(Field field) {
            super(field);
        }

        @Override
        public double getDoubleValue(Object object) {
            return UNSAFE.getDouble(Objects.requireNonNull(object), fieldOffset);
        }

        @Override
        public void setDoubleValue(Object object, double value) {
            UNSAFE.putDouble(Objects.requireNonNull(object), fieldOffset, value);
        }
    }

    /**
     * Unsafe-based field accessor implementation for char-typed properties.
     * Provides efficient getter and setter operations for char fields using Unsafe operations.
     */
    static final class FieldAccessorUnsafeCharValue
            extends FieldAccessorUnsafe implements PropertyAccessorCharValue
    {
        public FieldAccessorUnsafeCharValue(Field field) {
            super(field);
        }

        @Override
        public char getCharValue(Object object) {
            return UNSAFE.getChar(Objects.requireNonNull(object), fieldOffset);
        }

        @Override
        public void setCharValue(Object object, char value) {
            UNSAFE.putChar(Objects.requireNonNull(object), fieldOffset, value);
        }
    }

    /**
     * Unsafe-based field accessor implementation for Object-typed properties.
     * Provides efficient getter and setter operations for Object fields using Unsafe operations.
     */
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

    /**
     * Unsafe-based field accessor implementation for String-typed properties.
     * Provides efficient getter and setter operations for String fields using Unsafe operations.
     */
    static final class FieldAccessorUnsafeString extends FieldAccessorUnsafe implements PropertyAccessorString {
        public FieldAccessorUnsafeString(Field field) {
            super(field);
        }

        @Override
        public String getString(Object object) {
            return (String) UNSAFE.getObject(Objects.requireNonNull(object), fieldOffset);
        }

        @Override
        public void setString(Object object, String value) {
            UNSAFE.putObject(Objects.requireNonNull(object), fieldOffset, value);
        }
    }

    /**
     * Unsafe-based field accessor implementation for BigInteger-typed properties.
     * Provides efficient getter and setter operations for BigInteger fields using Unsafe operations.
     */
    static final class FieldAccessorUnsafeBigInteger extends FieldAccessorUnsafe implements PropertyAccessorBigInteger {
        public FieldAccessorUnsafeBigInteger(Field field) {
            super(field);
        }

        @Override
        public BigInteger getBigInteger(Object object) {
            return (BigInteger) UNSAFE.getObject(Objects.requireNonNull(object), fieldOffset);
        }

        @Override
        public void setBigInteger(Object object, BigInteger value) {
            UNSAFE.putObject(Objects.requireNonNull(object), fieldOffset, value);
        }
    }

    /**
     * Unsafe-based field accessor implementation for BigDecimal-typed properties.
     * Provides efficient getter and setter operations for BigDecimal fields using Unsafe operations.
     */
    static final class FieldAccessorUnsafeBigDecimal extends FieldAccessorUnsafe implements PropertyAccessorBigDecimal {
        public FieldAccessorUnsafeBigDecimal(Field field) {
            super(field);
        }

        @Override
        public BigDecimal getBigDecimal(Object object) {
            return (BigDecimal) UNSAFE.getObject(Objects.requireNonNull(object), fieldOffset);
        }

        @Override
        public void setBigDecimal(Object object, BigDecimal value) {
            UNSAFE.putObject(Objects.requireNonNull(object), fieldOffset, value);
        }
    }
}
