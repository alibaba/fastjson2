package com.alibaba.fastjson2.internal;

import com.alibaba.fastjson2.JSONException;

import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.alibaba.fastjson2.internal.Cast.*;

public class PropertyAccessorFactory {
    protected final ConcurrentMap<Object, PropertyAccessor> cache = new ConcurrentHashMap<>();

    public PropertyAccessor create(Field field) {
        PropertyAccessor accessor = cache.get(field);
        if (accessor == null) {
            accessor = createInternal(field);
            cache.put(field, accessor);
        }
        return accessor;
    }

    protected PropertyAccessor createInternal(Field field) {
        if (field.getType() == byte.class) {
            return new FieldAccessorReflectByte(field);
        }
        if (field.getType() == short.class) {
            return new FieldAccessorReflectShort(field);
        }
        if (field.getType() == int.class) {
            return new FieldAccessorReflectInt(field);
        }
        if (field.getType() == long.class) {
            return new FieldAccessorReflectLong(field);
        }
        if (field.getType() == float.class) {
            return new FieldAccessorReflectFloat(field);
        }
        if (field.getType() == double.class) {
            return new FieldAccessorReflectDouble(field);
        }
        if (field.getType() == boolean.class) {
            return new FieldAccessorReflectBoolean(field);
        }
        if (field.getType() == char.class) {
            return new FieldAccessorReflectChar(field);
        }
        return new FieldAccessorReflectObject(field);
    }

    abstract static class FieldAccessorReflect extends FieldAccessor {
        public FieldAccessorReflect(Field field) {
            super(field);
            try {
                field.setAccessible(true);
            } catch (RuntimeException e) {
                throw errorOnSetAccessible(field, e);
            }
        }

        private static JSONException errorOnSetAccessible(Field field, RuntimeException e) {
            return new JSONException(field.toString() + " setAccessible error", e);
        }

        final JSONException errorForGet(IllegalAccessException e) {
            return new JSONException(field.toString() + " get error", e);
        }

        final JSONException errorForSet(IllegalAccessException e) {
            return new JSONException(field.toString() + " set error", e);
        }
    }

    static final class FieldAccessorReflectBoolean extends FieldAccessorReflect {
        public FieldAccessorReflectBoolean(Field field) {
            super(field);
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
            try {
                return field.getBoolean(object);
            } catch (IllegalAccessException e) {
                throw errorForGet(e);
            }
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
            try {
                field.setBoolean(object, value);
            } catch (IllegalAccessException e) {
                throw errorForSet(e);
            }
        }
    }

    static final class FieldAccessorReflectByte extends FieldAccessorReflect {
        public FieldAccessorReflectByte(Field field) {
            super(field);
        }

        @Override
        public Object getObject(Object object) {
            return getByte(object);
        }

        @Override
        public byte getByte(Object object) {
            try {
                return field.getByte(object);
            } catch (IllegalAccessException e) {
                throw errorForGet(e);
            }
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
            return getByte(object) != 0;
        }

        @Override
        public void setObject(Object object, Object value) {
            setByte(object, value instanceof Number ? ((Number) value).byteValue() : (byte) 0);
        }

        @Override
        public void setByte(Object object, byte value) {
            try {
                field.setByte(object, value);
            } catch (IllegalAccessException e) {
                throw errorForSet(e);
            }
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

    static final class FieldAccessorReflectShort extends FieldAccessorReflect {
        public FieldAccessorReflectShort(Field field) {
            super(field);
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
            try {
                return field.getShort(object);
            } catch (IllegalAccessException e) {
                throw errorForGet(e);
            }
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
            try {
                field.setShort(object, value);
            } catch (IllegalAccessException e) {
                throw errorForSet(e);
            }
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

    static final class FieldAccessorReflectInt extends FieldAccessorReflect {
        public FieldAccessorReflectInt(Field field) {
            super(field);
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
            try {
                return field.getInt(object);
            } catch (IllegalAccessException e) {
                throw errorForGet(e);
            }
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
            try {
                field.setInt(object, value);
            } catch (IllegalAccessException e) {
                throw errorForSet(e);
            }
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

    static final class FieldAccessorReflectLong extends FieldAccessorReflect {
        public FieldAccessorReflectLong(Field field) {
            super(field);
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
            try {
                return field.getLong(object);
            } catch (IllegalAccessException e) {
                throw errorForGet(e);
            }
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
            try {
                field.setLong(object, value);
            } catch (IllegalAccessException e) {
                throw errorForSet(e);
            }
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

    static final class FieldAccessorReflectFloat extends FieldAccessorReflect {
        public FieldAccessorReflectFloat(Field field) {
            super(field);
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
            try {
                return field.getFloat(object);
            } catch (IllegalAccessException e) {
                throw errorForGet(e);
            }
        }

        @Override
        public double getDouble(Object object) {
            return toDouble(getFloat(object));
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
            try {
                field.setFloat(object, value);
            } catch (IllegalAccessException e) {
                throw errorForSet(e);
            }
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

    static final class FieldAccessorReflectDouble extends FieldAccessorReflect {
        public FieldAccessorReflectDouble(Field field) {
            super(field);
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
            try {
                return field.getDouble(object);
            } catch (IllegalAccessException e) {
                throw errorForGet(e);
            }
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
            try {
                field.setDouble(object, value);
            } catch (IllegalAccessException e) {
                throw errorForSet(e);
            }
        }

        @Override
        public void setBoolean(Object object, boolean value) {
            setDouble(object, value ? 1.0 : 0.0);
        }
    }

    static final class FieldAccessorReflectChar extends FieldAccessorReflect {
        public FieldAccessorReflectChar(Field field) {
            super(field);
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
            try {
                return field.getChar(object);
            } catch (IllegalAccessException e) {
                throw errorForGet(e);
            }
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
            try {
                field.setChar(object, value);
            } catch (IllegalAccessException e) {
                throw errorForSet(e);
            }
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

    static final class FieldAccessorReflectObject extends FieldAccessorReflect {
        public FieldAccessorReflectObject(Field field) {
            super(field);
        }

        @Override
        public Object getObject(Object object) {
            try {
                return field.get(object);
            } catch (IllegalAccessException e) {
                throw errorForGet(e);
            }
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
            try {
                field.set(object, value);
            } catch (IllegalAccessException e) {
                throw errorForSet(e);
            }
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
