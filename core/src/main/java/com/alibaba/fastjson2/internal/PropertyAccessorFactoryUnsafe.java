package com.alibaba.fastjson2.internal;

import com.alibaba.fastjson2.JSONException;

import java.lang.reflect.Field;
import java.util.Objects;

import static com.alibaba.fastjson2.util.JDKUtils.UNSAFE;

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
}
