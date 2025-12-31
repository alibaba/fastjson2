package com.alibaba.fastjson2.internal;

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
            return (byte) (getBoolean(object) ? 1 : 0);
        }

        @Override
        public char getChar(Object object) {
            return (char) (getBoolean(object) ? 1 : 0);
        }

        @Override
        public short getShort(Object object) {
            return (short) (getBoolean(object) ? 1 : 0);
        }

        @Override
        public int getInt(Object object) {
            return getBoolean(object) ? 1 : 0;
        }

        @Override
        public long getLong(Object object) {
            return getBoolean(object) ? 1L : 0L;
        }

        @Override
        public float getFloat(Object object) {
            return getBoolean(object) ? 1.0f : 0.0f;
        }

        @Override
        public double getDouble(Object object) {
            return getBoolean(object) ? 1.0 : 0.0;
        }

        @Override
        public boolean getBoolean(Object object) {
            return UNSAFE.getBoolean(Objects.requireNonNull(object), fieldOffset);
        }

        @Override
        public void setObject(Object object, Object value) {
            setBoolean(object, value instanceof Boolean ? (Boolean) value : false);
        }

        @Override
        public void setByte(Object object, byte value) {
            setBoolean(object, value != 0);
        }

        @Override
        public void setChar(Object object, char value) {
            setBoolean(object, value != '0');
        }

        @Override
        public void setShort(Object object, short value) {
            setBoolean(object, value != 0);
        }

        @Override
        public void setInt(Object object, int value) {
            setBoolean(object, value != 0);
        }

        @Override
        public void setLong(Object object, long value) {
            setBoolean(object, value != 0);
        }

        @Override
        public void setFloat(Object object, float value) {
            setBoolean(object, value != 0.0F);
        }

        @Override
        public void setDouble(Object object, double value) {
            setBoolean(object, value != 0.0);
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
            return (char) getByte(object);
        }

        @Override
        public short getShort(Object object) {
            return getByte(object);
        }

        @Override
        public int getInt(Object object) {
            return getByte(object);
        }

        @Override
        public long getLong(Object object) {
            return getByte(object);
        }

        @Override
        public float getFloat(Object object) {
            return getByte(object);
        }

        @Override
        public double getDouble(Object object) {
            return getByte(object);
        }

        @Override
        public boolean getBoolean(Object object) {
            return getByte(object) != 0;
        }

        @Override
        public void setObject(Object object, Object value) {
            setByte(object, value instanceof Number ? ((Number) value).byteValue() : (byte) 0);
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
            setByte(object, (byte) (value ? 1 : 0));
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
            return (byte) getShort(object);
        }

        @Override
        public char getChar(Object object) {
            return (char) getShort(object);
        }

        @Override
        public short getShort(Object object) {
            return UNSAFE.getShort(Objects.requireNonNull(object), fieldOffset);
        }

        @Override
        public int getInt(Object object) {
            return getShort(object);
        }

        @Override
        public long getLong(Object object) {
            return getShort(object);
        }

        @Override
        public float getFloat(Object object) {
            return getShort(object);
        }

        @Override
        public double getDouble(Object object) {
            return getShort(object);
        }

        @Override
        public boolean getBoolean(Object object) {
            return getShort(object) != 0;
        }

        @Override
        public void setObject(Object object, Object value) {
            setShort(object, value instanceof Number ? ((Number) value).shortValue() : (short) 0);
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
            setShort(object, (short) (value ? 1 : 0));
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
            return (byte) getInt(object);
        }

        @Override
        public char getChar(Object object) {
            return (char) getInt(object);
        }

        @Override
        public short getShort(Object object) {
            return (short) getInt(object);
        }

        @Override
        public int getInt(Object object) {
            return UNSAFE.getInt(Objects.requireNonNull(object), fieldOffset);
        }

        @Override
        public long getLong(Object object) {
            return getInt(object);
        }

        @Override
        public float getFloat(Object object) {
            return getInt(object);
        }

        @Override
        public double getDouble(Object object) {
            return getInt(object);
        }

        @Override
        public boolean getBoolean(Object object) {
            return getInt(object) != 0;
        }

        @Override
        public void setObject(Object object, Object value) {
            setInt(object, value instanceof Number ? ((Number) value).intValue() : 0);
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
            setInt(object, value ? 1 : 0);
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
            return (byte) getLong(object);
        }

        @Override
        public char getChar(Object object) {
            return (char) getLong(object);
        }

        @Override
        public short getShort(Object object) {
            return (short) getLong(object);
        }

        @Override
        public int getInt(Object object) {
            return (int) getLong(object);
        }

        @Override
        public long getLong(Object object) {
            return UNSAFE.getLong(Objects.requireNonNull(object), fieldOffset);
        }

        @Override
        public float getFloat(Object object) {
            return getLong(object);
        }

        @Override
        public double getDouble(Object object) {
            return getLong(object);
        }

        @Override
        public boolean getBoolean(Object object) {
            return getLong(object) != 0;
        }

        @Override
        public void setObject(Object object, Object value) {
            setLong(object, value instanceof Number ? ((Number) value).longValue() : 0L);
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
            setLong(object, value ? 1L : 0L);
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
            return (byte) getFloat(object);
        }

        @Override
        public char getChar(Object object) {
            return (char) getFloat(object);
        }

        @Override
        public short getShort(Object object) {
            return (short) getFloat(object);
        }

        @Override
        public int getInt(Object object) {
            return (int) getFloat(object);
        }

        @Override
        public long getLong(Object object) {
            return (long) getFloat(object);
        }

        @Override
        public float getFloat(Object object) {
            return UNSAFE.getFloat(Objects.requireNonNull(object), fieldOffset);
        }

        @Override
        public double getDouble(Object object) {
            return getFloat(object);
        }

        @Override
        public boolean getBoolean(Object object) {
            return getFloat(object) != 0.0f;
        }

        @Override
        public void setObject(Object object, Object value) {
            setFloat(object, value instanceof Number ? ((Number) value).floatValue() : 0.0f);
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
            setFloat(object, value ? 1.0f : 0.0f);
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
            return (byte) getDouble(object);
        }

        @Override
        public char getChar(Object object) {
            return (char) getDouble(object);
        }

        @Override
        public short getShort(Object object) {
            return (short) getDouble(object);
        }

        @Override
        public int getInt(Object object) {
            return (int) getDouble(object);
        }

        @Override
        public long getLong(Object object) {
            return (long) getDouble(object);
        }

        @Override
        public float getFloat(Object object) {
            return (float) getDouble(object);
        }

        @Override
        public double getDouble(Object object) {
            return UNSAFE.getDouble(Objects.requireNonNull(object), fieldOffset);
        }

        @Override
        public boolean getBoolean(Object object) {
            return getDouble(object) != 0.0;
        }

        @Override
        public void setObject(Object object, Object value) {
            setDouble(object, value instanceof Number ? ((Number) value).doubleValue() : 0.0);
        }

        @Override
        public void setByte(Object object, byte value) {
            setDouble(object, value);
        }

        @Override
        public void setChar(Object object, char value) {
            setDouble(object, value);
        }

        @Override
        public void setShort(Object object, short value) {
            setDouble(object, value);
        }

        @Override
        public void setInt(Object object, int value) {
            setDouble(object, value);
        }

        @Override
        public void setLong(Object object, long value) {
            setDouble(object, value);
        }

        @Override
        public void setFloat(Object object, float value) {
            setDouble(object, value);
        }

        @Override
        public void setDouble(Object object, double value) {
            UNSAFE.putDouble(Objects.requireNonNull(object), fieldOffset, value);
        }

        @Override
        public void setBoolean(Object object, boolean value) {
            setDouble(object, value ? 1.0 : 0.0);
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
            return (byte) getChar(object);
        }

        @Override
        public char getChar(Object object) {
            return UNSAFE.getChar(Objects.requireNonNull(object), fieldOffset);
        }

        @Override
        public short getShort(Object object) {
            return (short) getChar(object);
        }

        @Override
        public int getInt(Object object) {
            return getChar(object);
        }

        @Override
        public long getLong(Object object) {
            return getChar(object);
        }

        @Override
        public float getFloat(Object object) {
            return getChar(object);
        }

        @Override
        public double getDouble(Object object) {
            return getChar(object);
        }

        @Override
        public boolean getBoolean(Object object) {
            return getChar(object) != 0;
        }

        @Override
        public void setObject(Object object, Object value) {
            setChar(object, value instanceof Character ? (Character) value : (char) 0);
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
            setChar(object, (char) (value ? 1 : 0));
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
            return ((Number) getObject(object)).byteValue();
        }

        @Override
        public char getChar(Object object) {
            return (Character) getObject(object);
        }

        @Override
        public short getShort(Object object) {
            return ((Number) getObject(object)).shortValue();
        }

        @Override
        public int getInt(Object object) {
            return ((Number) getObject(object)).intValue();
        }

        @Override
        public long getLong(Object object) {
            return ((Number) getObject(object)).longValue();
        }

        @Override
        public float getFloat(Object object) {
            return ((Number) getObject(object)).floatValue();
        }

        @Override
        public double getDouble(Object object) {
            return ((Number) getObject(object)).doubleValue();
        }

        @Override
        public boolean getBoolean(Object object) {
            return (Boolean) getObject(object);
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
