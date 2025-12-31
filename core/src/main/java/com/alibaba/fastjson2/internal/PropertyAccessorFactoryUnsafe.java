package com.alibaba.fastjson2.internal;

import java.lang.reflect.Field;
import java.util.Objects;

import static com.alibaba.fastjson2.internal.Cast.*;
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

    static final class FieldAccessorUnsafeBoolean extends FieldAccessor {
        private final long fieldOffset;

        public FieldAccessorUnsafeBoolean(Field field) {
            super(field);
            this.fieldOffset = UNSAFE.objectFieldOffset(field);
        }

        @Override
        public Object getObject(Object object) {
            return getBoolean(object);
        }

        @Override
        public byte getByte(Object object) {
            return toByte(getBoolean(object));
        }

        @Override
        public char getChar(Object object) {
            return toChar(getBoolean(object));
        }

        @Override
        public short getShort(Object object) {
            return toShort(getBoolean(object));
        }

        @Override
        public int getInt(Object object) {
            return toInt(getBoolean(object));
        }

        @Override
        public long getLong(Object object) {
            return toLong(getBoolean(object));
        }

        @Override
        public float getFloat(Object object) {
            return toFloat(getBoolean(object));
        }

        @Override
        public double getDouble(Object object) {
            return toDouble(getBoolean(object));
        }

        @Override
        public boolean getBoolean(Object object) {
            return UNSAFE.getBoolean(Objects.requireNonNull(object), fieldOffset);
        }

        @Override
        public void setObject(Object object, Object value) {
            setBoolean(object, toBoolean(value));
        }

        @Override
        public void setByte(Object object, byte value) {
            setBoolean(object, toBoolean(value));
        }

        @Override
        public void setChar(Object object, char value) {
            setBoolean(object, toBoolean(value));
        }

        @Override
        public void setShort(Object object, short value) {
            setBoolean(object, toBoolean(value));
        }

        @Override
        public void setInt(Object object, int value) {
            setBoolean(object, toBoolean(value));
        }

        @Override
        public void setLong(Object object, long value) {
            setBoolean(object, toBoolean(value));
        }

        @Override
        public void setFloat(Object object, float value) {
            setBoolean(object, toBoolean(value));
        }

        @Override
        public void setDouble(Object object, double value) {
            setBoolean(object, toBoolean(value));
        }

        @Override
        public void setBoolean(Object object, boolean value) {
            UNSAFE.putBoolean(Objects.requireNonNull(object), fieldOffset, value);
        }
    }

    static final class FieldAccessorUnsafeByte extends FieldAccessor {
        private final long fieldOffset;

        public FieldAccessorUnsafeByte(Field field) {
            super(field);
            this.fieldOffset = UNSAFE.objectFieldOffset(field);
        }

        @Override
        public Object getObject(Object object) {
            return getByte(object);
        }

        @Override
        public byte getByte(Object object) {
            return UNSAFE.getByte(Objects.requireNonNull(object), fieldOffset);
        }

        @Override
        public char getChar(Object object) {
            return toChar(getByte(object));
        }

        @Override
        public short getShort(Object object) {
            return toShort(getByte(object));
        }

        @Override
        public int getInt(Object object) {
            return toInt(getByte(object));
        }

        @Override
        public long getLong(Object object) {
            return toLong(getByte(object));
        }

        @Override
        public float getFloat(Object object) {
            return toFloat(getByte(object));
        }

        @Override
        public double getDouble(Object object) {
            return toDouble(getByte(object));
        }

        @Override
        public boolean getBoolean(Object object) {
            return toBoolean(getByte(object));
        }

        @Override
        public void setObject(Object object, Object value) {
            setByte(object, toByte(value));
        }

        @Override
        public void setByte(Object object, byte value) {
            UNSAFE.putByte(Objects.requireNonNull(object), fieldOffset, value);
        }

        @Override
        public void setChar(Object object, char value) {
            setByte(object, (byte) value);
        }

        @Override
        public void setShort(Object object, short value) {
            setByte(object, (byte) value);
        }

        @Override
        public void setInt(Object object, int value) {
            setByte(object, (byte) value);
        }

        @Override
        public void setLong(Object object, long value) {
            setByte(object, (byte) value);
        }

        @Override
        public void setFloat(Object object, float value) {
            setByte(object, (byte) value);
        }

        @Override
        public void setDouble(Object object, double value) {
            setByte(object, (byte) value);
        }

        @Override
        public void setBoolean(Object object, boolean value) {
            setByte(object, toByte(value));
        }
    }

    static final class FieldAccessorUnsafeShort extends FieldAccessor {
        private final long fieldOffset;

        public FieldAccessorUnsafeShort(Field field) {
            super(field);
            this.fieldOffset = UNSAFE.objectFieldOffset(field);
        }

        @Override
        public Object getObject(Object object) {
            return getShort(object);
        }

        @Override
        public byte getByte(Object object) {
            return toByte(getShort(object));
        }

        @Override
        public char getChar(Object object) {
            return toChar(getShort(object));
        }

        @Override
        public short getShort(Object object) {
            return UNSAFE.getShort(Objects.requireNonNull(object), fieldOffset);
        }

        @Override
        public int getInt(Object object) {
            return toInt(getShort(object));
        }

        @Override
        public long getLong(Object object) {
            return toLong(getShort(object));
        }

        @Override
        public float getFloat(Object object) {
            return toFloat(getShort(object));
        }

        @Override
        public double getDouble(Object object) {
            return toDouble(getShort(object));
        }

        @Override
        public boolean getBoolean(Object object) {
            return toBoolean(getShort(object));
        }

        @Override
        public void setObject(Object object, Object value) {
            setShort(object, toShort(value));
        }

        @Override
        public void setByte(Object object, byte value) {
            setShort(object, value);
        }

        @Override
        public void setChar(Object object, char value) {
            setShort(object, (short) value);
        }

        @Override
        public void setShort(Object object, short value) {
            UNSAFE.putShort(Objects.requireNonNull(object), fieldOffset, value);
        }

        @Override
        public void setInt(Object object, int value) {
            setShort(object, (short) value);
        }

        @Override
        public void setLong(Object object, long value) {
            setShort(object, (short) value);
        }

        @Override
        public void setFloat(Object object, float value) {
            setShort(object, (short) value);
        }

        @Override
        public void setDouble(Object object, double value) {
            setShort(object, (short) value);
        }

        @Override
        public void setBoolean(Object object, boolean value) {
            setShort(object, toShort(value));
        }
    }

    static final class FieldAccessorUnsafeInt extends FieldAccessor {
        private final long fieldOffset;

        public FieldAccessorUnsafeInt(Field field) {
            super(field);
            this.fieldOffset = UNSAFE.objectFieldOffset(field);
        }

        @Override
        public Object getObject(Object object) {
            return getInt(object);
        }

        @Override
        public byte getByte(Object object) {
            return toByte(getInt(object));
        }

        @Override
        public char getChar(Object object) {
            return toChar(getInt(object));
        }

        @Override
        public short getShort(Object object) {
            return toShort(getInt(object));
        }

        @Override
        public int getInt(Object object) {
            return UNSAFE.getInt(Objects.requireNonNull(object), fieldOffset);
        }

        @Override
        public long getLong(Object object) {
            return toLong(getInt(object));
        }

        @Override
        public float getFloat(Object object) {
            return toFloat(getInt(object));
        }

        @Override
        public double getDouble(Object object) {
            return toDouble(getInt(object));
        }

        @Override
        public boolean getBoolean(Object object) {
            return toBoolean(getInt(object));
        }

        @Override
        public void setObject(Object object, Object value) {
            setInt(object, toInt(value));
        }

        @Override
        public void setByte(Object object, byte value) {
            setInt(object, value);
        }

        @Override
        public void setShort(Object object, short value) {
            setInt(object, value);
        }

        @Override
        public void setChar(Object object, char value) {
            setInt(object, value);
        }

        @Override
        public void setInt(Object object, int value) {
            UNSAFE.putInt(Objects.requireNonNull(object), fieldOffset, value);
        }

        @Override
        public void setLong(Object object, long value) {
            setInt(object, (int) value);
        }

        @Override
        public void setFloat(Object object, float value) {
            setInt(object, (int) value);
        }

        @Override
        public void setDouble(Object object, double value) {
            setInt(object, (int) value);
        }

        @Override
        public void setBoolean(Object object, boolean value) {
            setInt(object, toInt(value));
        }
    }

    static final class FieldAccessorUnsafeLong extends FieldAccessor {
        private final long fieldOffset;

        public FieldAccessorUnsafeLong(Field field) {
            super(field);
            this.fieldOffset = UNSAFE.objectFieldOffset(field);
        }

        @Override
        public Object getObject(Object object) {
            return getLong(object);
        }

        @Override
        public byte getByte(Object object) {
            return toByte(getLong(object));
        }

        @Override
        public char getChar(Object object) {
            return toChar(getLong(object));
        }

        @Override
        public short getShort(Object object) {
            return toShort(getLong(object));
        }

        @Override
        public int getInt(Object object) {
            return toInt(getLong(object));
        }

        @Override
        public long getLong(Object object) {
            return UNSAFE.getLong(Objects.requireNonNull(object), fieldOffset);
        }

        @Override
        public float getFloat(Object object) {
            return toFloat(getLong(object));
        }

        @Override
        public double getDouble(Object object) {
            return toDouble(getLong(object));
        }

        @Override
        public boolean getBoolean(Object object) {
            return getLong(object) != 0;
        }

        @Override
        public void setObject(Object object, Object value) {
            setLong(object, toLong(value));
        }

        @Override
        public void setByte(Object object, byte value) {
            setLong(object, value);
        }

        @Override
        public void setChar(Object object, char value) {
            setLong(object, value);
        }

        @Override
        public void setShort(Object object, short value) {
            setLong(object, value);
        }

        @Override
        public void setInt(Object object, int value) {
            setLong(object, value);
        }

        @Override
        public void setLong(Object object, long value) {
            UNSAFE.putLong(Objects.requireNonNull(object), fieldOffset, value);
        }

        @Override
        public void setFloat(Object object, float value) {
            setLong(object, (long) value);
        }

        @Override
        public void setDouble(Object object, double value) {
            setLong(object, (long) value);
        }

        @Override
        public void setBoolean(Object object, boolean value) {
            setLong(object, toLong(value));
        }
    }

    static final class FieldAccessorUnsafeFloat extends FieldAccessor {
        private final long fieldOffset;

        public FieldAccessorUnsafeFloat(Field field) {
            super(field);
            this.fieldOffset = UNSAFE.objectFieldOffset(field);
        }

        @Override
        public Object getObject(Object object) {
            return getFloat(object);
        }

        @Override
        public byte getByte(Object object) {
            return toByte(getFloat(object));
        }

        @Override
        public char getChar(Object object) {
            return toChar(getFloat(object));
        }

        @Override
        public short getShort(Object object) {
            return toShort(getFloat(object));
        }

        @Override
        public int getInt(Object object) {
            return toInt(getFloat(object));
        }

        @Override
        public long getLong(Object object) {
            return toLong(getFloat(object));
        }

        @Override
        public float getFloat(Object object) {
            return UNSAFE.getFloat(Objects.requireNonNull(object), fieldOffset);
        }

        @Override
        public double getDouble(Object object) {
            return toDouble(getFloat(object));
        }

        @Override
        public boolean getBoolean(Object object) {
            return toBoolean(getFloat(object));
        }

        @Override
        public void setObject(Object object, Object value) {
            setFloat(object, toFloat(value));
        }

        @Override
        public void setByte(Object object, byte value) {
            setFloat(object, value);
        }

        @Override
        public void setChar(Object object, char value) {
            setFloat(object, value);
        }

        @Override
        public void setShort(Object object, short value) {
            setFloat(object, value);
        }

        @Override
        public void setInt(Object object, int value) {
            setFloat(object, value);
        }

        @Override
        public void setLong(Object object, long value) {
            setFloat(object, value);
        }

        @Override
        public void setFloat(Object object, float value) {
            UNSAFE.putFloat(Objects.requireNonNull(object), fieldOffset, value);
        }

        @Override
        public void setDouble(Object object, double value) {
            setFloat(object, (float) value);
        }

        @Override
        public void setBoolean(Object object, boolean value) {
            setFloat(object, toFloat(value));
        }
    }

    static final class FieldAccessorUnsafeDouble extends FieldAccessor {
        private final long fieldOffset;

        public FieldAccessorUnsafeDouble(Field field) {
            super(field);
            this.fieldOffset = UNSAFE.objectFieldOffset(field);
        }

        @Override
        public Object getObject(Object object) {
            return getDouble(object);
        }

        @Override
        public byte getByte(Object object) {
            return toByte(getDouble(object));
        }

        @Override
        public char getChar(Object object) {
            return toChar(getDouble(object));
        }

        @Override
        public short getShort(Object object) {
            return toShort(getDouble(object));
        }

        @Override
        public int getInt(Object object) {
            return toInt(getDouble(object));
        }

        @Override
        public long getLong(Object object) {
            return toLong(getDouble(object));
        }

        @Override
        public float getFloat(Object object) {
            return toFloat(getDouble(object));
        }

        @Override
        public double getDouble(Object object) {
            return UNSAFE.getDouble(Objects.requireNonNull(object), fieldOffset);
        }

        @Override
        public boolean getBoolean(Object object) {
            return toBoolean(getDouble(object));
        }

        @Override
        public void setObject(Object object, Object value) {
            setDouble(object, toDouble(value));
        }

        @Override
        public void setByte(Object object, byte value) {
            setDouble(object, toDouble(value));
        }

        @Override
        public void setChar(Object object, char value) {
            setDouble(object, toDouble(value));
        }

        @Override
        public void setShort(Object object, short value) {
            setDouble(object, toDouble(value));
        }

        @Override
        public void setInt(Object object, int value) {
            setDouble(object, toDouble(value));
        }

        @Override
        public void setLong(Object object, long value) {
            setDouble(object, toDouble(value));
        }

        @Override
        public void setFloat(Object object, float value) {
            setDouble(object, toDouble(value));
        }

        @Override
        public void setDouble(Object object, double value) {
            UNSAFE.putDouble(Objects.requireNonNull(object), fieldOffset, value);
        }

        @Override
        public void setBoolean(Object object, boolean value) {
            setDouble(object, toDouble(value));
        }
    }

    static final class FieldAccessorUnsafeChar extends FieldAccessor {
        private final long fieldOffset;

        public FieldAccessorUnsafeChar(Field field) {
            super(field);
            this.fieldOffset = UNSAFE.objectFieldOffset(field);
        }

        @Override
        public Object getObject(Object object) {
            return getChar(object);
        }

        @Override
        public byte getByte(Object object) {
            return toByte(getChar(object));
        }

        @Override
        public char getChar(Object object) {
            return UNSAFE.getChar(Objects.requireNonNull(object), fieldOffset);
        }

        @Override
        public short getShort(Object object) {
            return toShort(getChar(object));
        }

        @Override
        public int getInt(Object object) {
            return toInt(getChar(object));
        }

        @Override
        public long getLong(Object object) {
            return toLong(getChar(object));
        }

        @Override
        public float getFloat(Object object) {
            return toFloat(getChar(object));
        }

        @Override
        public double getDouble(Object object) {
            return toDouble(getChar(object));
        }

        @Override
        public boolean getBoolean(Object object) {
            return toBoolean(getChar(object));
        }

        @Override
        public void setObject(Object object, Object value) {
            setChar(object, toChar(value));
        }

        @Override
        public void setByte(Object object, byte value) {
            setChar(object, (char) value);
        }

        @Override
        public void setChar(Object object, char value) {
            UNSAFE.putChar(Objects.requireNonNull(object), fieldOffset, value);
        }

        @Override
        public void setShort(Object object, short value) {
            setChar(object, (char) value);
        }

        @Override
        public void setInt(Object object, int value) {
            setChar(object, (char) value);
        }

        @Override
        public void setLong(Object object, long value) {
            setChar(object, (char) value);
        }

        @Override
        public void setFloat(Object object, float value) {
            setChar(object, (char) value);
        }

        @Override
        public void setDouble(Object object, double value) {
            setChar(object, (char) value);
        }

        @Override
        public void setBoolean(Object object, boolean value) {
            setChar(object, toChar(value));
        }
    }

    static final class FieldAccessorUnsafeObject extends FieldAccessor {
        private final long fieldOffset;

        public FieldAccessorUnsafeObject(Field field) {
            super(field);
            this.fieldOffset = UNSAFE.objectFieldOffset(field);
        }

        @Override
        public Object getObject(Object object) {
            return UNSAFE.getObject(Objects.requireNonNull(object), fieldOffset);
        }

        @Override
        public byte getByte(Object object) {
            return toByte(getObject(object));
        }

        @Override
        public char getChar(Object object) {
            return toChar(getObject(object));
        }

        @Override
        public short getShort(Object object) {
            return toShort(getObject(object));
        }

        @Override
        public int getInt(Object object) {
            return toInt(getObject(object));
        }

        @Override
        public long getLong(Object object) {
            return toLong(getObject(object));
        }

        @Override
        public float getFloat(Object object) {
            return toFloat(getObject(object));
        }

        @Override
        public double getDouble(Object object) {
            return toDouble(getObject(object));
        }

        @Override
        public boolean getBoolean(Object object) {
            return toBoolean(getObject(object));
        }

        @Override
        public void setObject(Object object, Object value) {
            UNSAFE.putObject(Objects.requireNonNull(object), fieldOffset, value);
        }

        @Override
        public void setByte(Object object, byte value) {
            setObject(object, value);
        }

        @Override
        public void setShort(Object object, short value) {
            setObject(object, value);
        }

        @Override
        public void setChar(Object object, char value) {
            setObject(object, value);
        }

        @Override
        public void setInt(Object object, int value) {
            setObject(object, value);
        }

        @Override
        public void setLong(Object object, long value) {
            setObject(object, value);
        }

        @Override
        public void setFloat(Object object, float value) {
            setObject(object, value);
        }

        @Override
        public void setDouble(Object object, double value) {
            setObject(object, value);
        }

        @Override
        public void setBoolean(Object object, boolean value) {
            setObject(object, value);
        }
    }
}
