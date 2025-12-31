package com.alibaba.fastjson2.internal;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.function.*;
import com.alibaba.fastjson2.util.BeanUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.function.*;

import static com.alibaba.fastjson2.internal.Cast.*;

public class PropertyAccessorFactory {
    public PropertyAccessor create(Field field) {
        return createInternal(field);
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

    protected interface PropertyAccessorBoolean extends PropertyAccessor {
        default Object getObject(Object object) {
            return getBoolean(object);
        }

        default byte getByte(Object object) {
            return toByte(getBoolean(object));
        }

        default char getChar(Object object) {
            return toChar(getBoolean(object));
        }

        default short getShort(Object object) {
            return toShort(getBoolean(object));
        }

        default int getInt(Object object) {
            return toInt(getBoolean(object));
        }

        default long getLong(Object object) {
            return toLong(getBoolean(object));
        }

        default float getFloat(Object object) {
            return toFloat(getBoolean(object));
        }

        default double getDouble(Object object) {
            return toDouble(getBoolean(object));
        }

        default void setObject(Object object, Object value) {
            setBoolean(object, toBoolean(value));
        }

        default void setByte(Object object, byte value) {
            setBoolean(object, toBoolean(value));
        }

        default void setChar(Object object, char value) {
            setBoolean(object, toBoolean(value));
        }

        default void setShort(Object object, short value) {
            setBoolean(object, toBoolean(value));
        }

        default void setInt(Object object, int value) {
            setBoolean(object, toBoolean(value));
        }

        default void setLong(Object object, long value) {
            setBoolean(object, toBoolean(value));
        }

        default void setFloat(Object object, float value) {
            setBoolean(object, toBoolean(value));
        }

        default void setDouble(Object object, double value) {
            setBoolean(object, toBoolean(value));
        }
    }

    protected interface PropertyAccessorByte extends PropertyAccessor {
        @Override
        default Object getObject(Object object) {
            return getByte(object);
        }

        @Override
        default char getChar(Object object) {
            return toChar(getByte(object));
        }

        @Override
        default short getShort(Object object) {
            return toShort(getByte(object));
        }

        @Override
        default int getInt(Object object) {
            return toInt(getByte(object));
        }

        @Override
        default long getLong(Object object) {
            return toLong(getByte(object));
        }

        @Override
        default float getFloat(Object object) {
            return toFloat(getByte(object));
        }

        @Override
        default double getDouble(Object object) {
            return toDouble(getByte(object));
        }

        @Override
        default boolean getBoolean(Object object) {
            return toBoolean(getByte(object));
        }

        @Override
        default void setObject(Object object, Object value) {
            setByte(object, toByte(value));
        }

        @Override
        default void setChar(Object object, char value) {
            setByte(object, (byte) value);
        }

        @Override
        default void setShort(Object object, short value) {
            setByte(object, (byte) value);
        }

        @Override
        default void setInt(Object object, int value) {
            setByte(object, (byte) value);
        }

        @Override
        default void setLong(Object object, long value) {
            setByte(object, (byte) value);
        }

        @Override
        default void setFloat(Object object, float value) {
            setByte(object, (byte) value);
        }

        @Override
        default void setDouble(Object object, double value) {
            setByte(object, (byte) value);
        }

        @Override
        default void setBoolean(Object object, boolean value) {
            setByte(object, toByte(value));
        }
    }

    protected interface PropertyAccessorShort extends PropertyAccessor {
        default Object getObject(Object object) {
            return getShort(object);
        }

        default byte getByte(Object object) {
            return toByte(getShort(object));
        }

        default char getChar(Object object) {
            return toChar(getShort(object));
        }

        default int getInt(Object object) {
            return toInt(getShort(object));
        }

        default long getLong(Object object) {
            return toLong(getShort(object));
        }

        default float getFloat(Object object) {
            return toFloat(getShort(object));
        }

        default double getDouble(Object object) {
            return toDouble(getShort(object));
        }

        default boolean getBoolean(Object object) {
            return toBoolean(getShort(object));
        }

        default void setObject(Object object, Object value) {
            setShort(object, toShort(value));
        }

        default void setByte(Object object, byte value) {
            setShort(object, value);
        }

        default void setChar(Object object, char value) {
            setShort(object, (short) value);
        }

        default void setInt(Object object, int value) {
            setShort(object, (short) value);
        }

        default void setLong(Object object, long value) {
            setShort(object, (short) value);
        }

        default void setFloat(Object object, float value) {
            setShort(object, (short) value);
        }

        default void setDouble(Object object, double value) {
            setShort(object, (short) value);
        }

        default void setBoolean(Object object, boolean value) {
            setShort(object, toShort(value));
        }
    }

    protected interface PropertyAccessorInt extends PropertyAccessor {
        @Override
        default Object getObject(Object object) {
            return getInt(object);
        }

        @Override
        default byte getByte(Object object) {
            return toByte(getInt(object));
        }

        @Override
        default char getChar(Object object) {
            return toChar(getInt(object));
        }

        @Override
        default short getShort(Object object) {
            return toShort(getInt(object));
        }

        @Override
        default long getLong(Object object) {
            return toLong(getInt(object));
        }

        @Override
        default float getFloat(Object object) {
            return toFloat(getInt(object));
        }

        @Override
        default double getDouble(Object object) {
            return toDouble(getInt(object));
        }

        @Override
        default boolean getBoolean(Object object) {
            return toBoolean(getInt(object));
        }

        @Override
        default void setObject(Object object, Object value) {
            setInt(object, toInt(value));
        }

        @Override
        default void setByte(Object object, byte value) {
            setInt(object, value);
        }

        @Override
        default void setShort(Object object, short value) {
            setInt(object, value);
        }

        @Override
        default void setChar(Object object, char value) {
            setInt(object, value);
        }

        @Override
        default void setLong(Object object, long value) {
            setInt(object, (int) value);
        }

        @Override
        default void setFloat(Object object, float value) {
            setInt(object, (int) value);
        }

        @Override
        default void setDouble(Object object, double value) {
            setInt(object, (int) value);
        }

        @Override
        default void setBoolean(Object object, boolean value) {
            setInt(object, toInt(value));
        }
    }

    protected interface PropertyAccessorLong extends PropertyAccessor {
        @Override
        default Object getObject(Object object) {
            return getLong(object);
        }

        @Override
        default byte getByte(Object object) {
            return toByte(getLong(object));
        }

        @Override
        default char getChar(Object object) {
            return toChar(getLong(object));
        }

        @Override
        default short getShort(Object object) {
            return toShort(getLong(object));
        }

        @Override
        default int getInt(Object object) {
            return toInt(getLong(object));
        }

        @Override
        default float getFloat(Object object) {
            return toFloat(getLong(object));
        }

        @Override
        default double getDouble(Object object) {
            return toDouble(getLong(object));
        }

        @Override
        default boolean getBoolean(Object object) {
            return toBoolean(getLong(object));
        }

        @Override
        default void setObject(Object object, Object value) {
            setLong(object, toLong(value));
        }

        @Override
        default void setByte(Object object, byte value) {
            setLong(object, value);
        }

        @Override
        default void setChar(Object object, char value) {
            setLong(object, value);
        }

        @Override
        default void setShort(Object object, short value) {
            setLong(object, value);
        }

        @Override
        default void setInt(Object object, int value) {
            setLong(object, value);
        }

        @Override
        default void setFloat(Object object, float value) {
            setLong(object, (long) value);
        }

        @Override
        default void setDouble(Object object, double value) {
            setLong(object, (long) value);
        }

        @Override
        default void setBoolean(Object object, boolean value) {
            setLong(object, toLong(value));
        }
    }

    interface PropertyAccessorFloat extends PropertyAccessor {
        @Override
        default Object getObject(Object object) {
            return getFloat(object);
        }

        @Override
        default byte getByte(Object object) {
            return toByte(getFloat(object));
        }

        @Override
        default char getChar(Object object) {
            return toChar(getFloat(object));
        }

        @Override
        default short getShort(Object object) {
            return toShort(getFloat(object));
        }

        @Override
        default int getInt(Object object) {
            return toInt(getFloat(object));
        }

        @Override
        default long getLong(Object object) {
            return toLong(getFloat(object));
        }

        @Override
        default double getDouble(Object object) {
            return toDouble(getFloat(object));
        }

        @Override
        default boolean getBoolean(Object object) {
            return toBoolean(getFloat(object));
        }

        @Override
        default void setObject(Object object, Object value) {
            setFloat(object, toFloat(value));
        }

        @Override
        default void setByte(Object object, byte value) {
            setFloat(object, value);
        }

        @Override
        default void setChar(Object object, char value) {
            setFloat(object, value);
        }

        @Override
        default void setShort(Object object, short value) {
            setFloat(object, value);
        }

        @Override
        default void setInt(Object object, int value) {
            setFloat(object, value);
        }

        @Override
        default void setLong(Object object, long value) {
            setFloat(object, value);
        }

        @Override
        default void setDouble(Object object, double value) {
            setFloat(object, (float) value);
        }

        @Override
        default void setBoolean(Object object, boolean value) {
            setFloat(object, toFloat(value));
        }
    }

    protected interface PropertyAccessorDouble extends PropertyAccessor {
        @Override
        default Object getObject(Object object) {
            return getDouble(object);
        }

        @Override
        default byte getByte(Object object) {
            return toByte(getDouble(object));
        }

        @Override
        default char getChar(Object object) {
            return toChar(getDouble(object));
        }

        @Override
        default short getShort(Object object) {
            return toShort(getDouble(object));
        }

        @Override
        default int getInt(Object object) {
            return toInt(getDouble(object));
        }

        @Override
        default long getLong(Object object) {
            return toLong(getDouble(object));
        }

        @Override
        default float getFloat(Object object) {
            return toFloat(getDouble(object));
        }

        @Override
        default boolean getBoolean(Object object) {
            return toBoolean(getDouble(object));
        }

        @Override
        default void setObject(Object object, Object value) {
            setDouble(object, toDouble(value));
        }

        @Override
        default void setByte(Object object, byte value) {
            setDouble(object, toDouble(value));
        }

        @Override
        default void setChar(Object object, char value) {
            setDouble(object, toDouble(value));
        }

        @Override
        default void setShort(Object object, short value) {
            setDouble(object, toDouble(value));
        }

        @Override
        default void setInt(Object object, int value) {
            setDouble(object, toDouble(value));
        }

        @Override
        default void setLong(Object object, long value) {
            setDouble(object, toDouble(value));
        }

        @Override
        default void setFloat(Object object, float value) {
            setDouble(object, value);
        }

        @Override
        default void setBoolean(Object object, boolean value) {
            setDouble(object, toDouble(value));
        }
    }

    protected interface PropertyAccessorChar extends PropertyAccessor {
        @Override
        default Object getObject(Object object) {
            return getChar(object);
        }

        @Override
        default byte getByte(Object object) {
            return toByte(getChar(object));
        }

        @Override
        default short getShort(Object object) {
            return toShort(getChar(object));
        }

        @Override
        default int getInt(Object object) {
            return toInt(getChar(object));
        }

        @Override
        default long getLong(Object object) {
            return toLong(getChar(object));
        }

        @Override
        default float getFloat(Object object) {
            return toFloat(getChar(object));
        }

        @Override
        default double getDouble(Object object) {
            return toDouble(getChar(object));
        }

        @Override
        default boolean getBoolean(Object object) {
            return toBoolean(getChar(object));
        }

        @Override
        default void setObject(Object object, Object value) {
            setChar(object, toChar(value));
        }

        @Override
        default void setByte(Object object, byte value) {
            setChar(object, (char) value);
        }

        @Override
        default void setShort(Object object, short value) {
            setChar(object, (char) value);
        }

        @Override
        default void setInt(Object object, int value) {
            setChar(object, (char) value);
        }

        @Override
        default void setLong(Object object, long value) {
            setChar(object, (char) value);
        }

        @Override
        default void setFloat(Object object, float value) {
            setChar(object, (char) value);
        }

        @Override
        default void setDouble(Object object, double value) {
            setChar(object, (char) value);
        }

        @Override
        default void setBoolean(Object object, boolean value) {
            setChar(object, toChar(value));
        }
    }

    protected interface PropertyAccessorObject extends PropertyAccessor {
        @Override
        default byte getByte(Object object) {
            return toByte(getObject(object));
        }

        @Override
        default char getChar(Object object) {
            return toChar(getObject(object));
        }

        @Override
        default short getShort(Object object) {
            return toShort(getObject(object));
        }

        @Override
        default int getInt(Object object) {
            return toInt(getObject(object));
        }

        @Override
        default long getLong(Object object) {
            return toLong(getObject(object));
        }

        @Override
        default float getFloat(Object object) {
            return toFloat(getObject(object));
        }

        @Override
        default double getDouble(Object object) {
            return toDouble(getObject(object));
        }

        @Override
        default boolean getBoolean(Object object) {
            return toBoolean(getObject(object));
        }

        @Override
        default void setByte(Object object, byte value) {
            setObject(object, value);
        }

        @Override
        default void setShort(Object object, short value) {
            setObject(object, value);
        }

        @Override
        default void setChar(Object object, char value) {
            setObject(object, value);
        }

        @Override
        default void setInt(Object object, int value) {
            setObject(object, value);
        }

        @Override
        default void setLong(Object object, long value) {
            setObject(object, value);
        }

        @Override
        default void setFloat(Object object, float value) {
            setObject(object, value);
        }

        @Override
        default void setDouble(Object object, double value) {
            setObject(object, value);
        }

        @Override
        default void setBoolean(Object object, boolean value) {
            setObject(object, value);
        }
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

        final JSONException errorForGet(Exception e) {
            return new JSONException(field.toString() + " get error", e);
        }

        final JSONException errorForSet(Exception e) {
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
            try {
                return field.getBoolean(object);
            } catch (IllegalAccessException e) {
                throw errorForGet(e);
            }
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
            try {
                field.setBoolean(object, value);
            } catch (IllegalAccessException e) {
                throw errorForSet(e);
            }
        }
    }

    static final class FieldAccessorReflectByte extends FieldAccessorReflect implements PropertyAccessorByte {
        public FieldAccessorReflectByte(Field field) {
            super(field);
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
        public void setByte(Object object, byte value) {
            try {
                field.setByte(object, value);
            } catch (IllegalAccessException e) {
                throw errorForSet(e);
            }
        }
    }

    static final class FieldAccessorReflectShort extends FieldAccessorReflect implements PropertyAccessorShort {
        public FieldAccessorReflectShort(Field field) {
            super(field);
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
        public void setShort(Object object, short value) {
            try {
                field.setShort(object, value);
            } catch (IllegalAccessException e) {
                throw errorForSet(e);
            }
        }
    }

    static final class FieldAccessorReflectInt extends FieldAccessorReflect implements PropertyAccessorInt {
        public FieldAccessorReflectInt(Field field) {
            super(field);
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
        public void setInt(Object object, int value) {
            try {
                field.setInt(object, value);
            } catch (IllegalAccessException e) {
                throw errorForSet(e);
            }
        }
    }

    static final class FieldAccessorReflectLong extends FieldAccessorReflect implements PropertyAccessorLong {
        public FieldAccessorReflectLong(Field field) {
            super(field);
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
        public void setLong(Object object, long value) {
            try {
                field.setLong(object, value);
            } catch (IllegalAccessException e) {
                throw errorForSet(e);
            }
        }
    }

    static final class FieldAccessorReflectFloat extends FieldAccessorReflect implements PropertyAccessorFloat {
        public FieldAccessorReflectFloat(Field field) {
            super(field);
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
        public void setFloat(Object object, float value) {
            try {
                field.setFloat(object, value);
            } catch (IllegalAccessException e) {
                throw errorForSet(e);
            }
        }
    }

    static final class FieldAccessorReflectDouble extends FieldAccessorReflect implements PropertyAccessorDouble {
        public FieldAccessorReflectDouble(Field field) {
            super(field);
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
        public void setDouble(Object object, double value) {
            try {
                field.setDouble(object, value);
            } catch (IllegalAccessException e) {
                throw errorForSet(e);
            }
        }
    }

    static final class FieldAccessorReflectChar extends FieldAccessorReflect implements PropertyAccessorChar {
        public FieldAccessorReflectChar(Field field) {
            super(field);
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
        public void setChar(Object object, char value) {
            try {
                field.setChar(object, value);
            } catch (IllegalAccessException e) {
                throw errorForSet(e);
            }
        }
    }

    static final class FieldAccessorReflectObject extends FieldAccessorReflect implements PropertyAccessorObject {
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
        public void setObject(Object object, Object value) {
            try {
                field.set(object, value);
            } catch (IllegalAccessException e) {
                throw errorForSet(e);
            }
        }
    }

    public PropertyAccessor create(Method method) {
        String methodName = method.getName();
        if (method.getParameterCount() == 0) {
            return create(BeanUtils.getterName(methodName, null), null, null, method, null);
        } else {
            return create(BeanUtils.setterName(methodName, null), null, null, null, method);
        }
    }

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

        if (propertyClass == byte.class) {
            return new MethodAccessorByte(name, propertyType, propertyClass, getter, setter);
        }
        if (propertyClass == short.class) {
            return new MethodAccessorShort(name, propertyType, propertyClass, getter, setter);
        }
        if (propertyClass == int.class) {
            return new MethodAccessorInt(name, propertyType, propertyClass, getter, setter);
        }
        if (propertyClass == long.class) {
            return new MethodAccessorLong(name, propertyType, propertyClass, getter, setter);
        }
        if (propertyClass == float.class) {
            return new MethodAccessorFloat(name, propertyType, propertyClass, getter, setter);
        }
        if (propertyClass == double.class) {
            return new MethodAccessorDouble(name, propertyType, propertyClass, getter, setter);
        }
        if (propertyClass == char.class) {
            return new MethodAccessorChar(name, propertyType, propertyClass, getter, setter);
        }
        if (propertyClass == boolean.class) {
            return new MethodAccessorBoolean(name, propertyType, propertyClass, getter, setter);
        }

        return new MethodAccessorObject(name, propertyType, propertyClass, getter, setter);
    }

    public <T> PropertyAccessor create(String name, ToByteFunction<T> getterFunc, ObjByteConsumer<T> setterFunc) {
        return new FunctionAccessorByte(name, getterFunc, setterFunc);
    }

    public <T> PropertyAccessor create(String name, ToShortFunction<T> getterFunc, ObjShortConsumer<T> setterFunc) {
        return new FunctionAccessorShort(name, getterFunc, setterFunc);
    }

    public <T> PropertyAccessor create(String name, ToIntFunction<T> getterFunc, ObjIntConsumer<T> setterFunc) {
        return new FunctionAccessorInt(name, getterFunc, setterFunc);
    }

    public <T> PropertyAccessor create(String name, ToLongFunction<T> getterFunc, ObjLongConsumer<T> setterFunc) {
        return new FunctionAccessorLong(name, getterFunc, setterFunc);
    }

    public <T> PropertyAccessor create(String name, ToFloatFunction<T> getterFunc, ObjFloatConsumer<T> setterFunc) {
        return new FunctionAccessorFloat(name, getterFunc, setterFunc);
    }

    public <T> PropertyAccessor create(String name, ToDoubleFunction<T> getterFunc, ObjDoubleConsumer<T> setterFunc) {
        return new FunctionAccessorDouble<>(name, getterFunc, setterFunc);
    }

    public <T> PropertyAccessor create(String name, Predicate<T> getterFunc, ObjBoolConsumer<T> setterFunc) {
        return new FunctionAccessorBoolean<>(name, getterFunc, setterFunc);
    }

    public <T> PropertyAccessor create(String name, ToCharFunction<T> getterFunc, ObjCharConsumer<T> setterFunc) {
        return new FunctionAccessorChar(name, getterFunc, setterFunc);
    }

    public <T, V> PropertyAccessor create(String name, Class<?> propertyClass, Type propertyType,
                                      Function<T, V> getterFunc,
                                      BiConsumer<T, V> setterFunc) {
        return new FunctionAccessorObject<T, V>(name, propertyType, propertyClass, getterFunc, setterFunc);
    }

    static final class MethodAccessorChar extends MethodAccessor implements PropertyAccessorChar {
        public MethodAccessorChar(String name, Type propertyType, Class<?> propertyClass, Method getter, Method setter) {
            super(name, propertyType, propertyClass, getter, setter);
        }

        @Override
        public char getChar(Object object) {
            try {
                return (char) getter.invoke(object);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw errorForGet(e);
            }
        }

        @Override
        public void setChar(Object object, char value) {
            try {
                setter.invoke(object, value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw errorForSet(e);
            }
        }
    }

    static final class MethodAccessorByte extends MethodAccessor implements PropertyAccessorByte {
        public MethodAccessorByte(String name, Type propertyType, Class<?> propertyClass, Method getter, Method setter) {
            super(name, propertyType, propertyClass, getter, setter);
        }

        @Override
        public byte getByte(Object object) {
            try {
                return (byte) getter.invoke(object);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw errorForGet(e);
            }
        }

        @Override
        public void setByte(Object object, byte value) {
            try {
                setter.invoke(object, value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw errorForSet(e);
            }
        }
    }

    static final class MethodAccessorShort extends MethodAccessor implements PropertyAccessorShort {
        public MethodAccessorShort(String name, Type propertyType, Class<?> propertyClass, Method getter, Method setter) {
            super(name, propertyType, propertyClass, getter, setter);
        }

        @Override
        public short getShort(Object object) {
            try {
                return (short) getter.invoke(object);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw errorForGet(e);
            }
        }

        @Override
        public void setShort(Object object, short value) {
            try {
                setter.invoke(object, value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw errorForSet(e);
            }
        }
    }

    static final class MethodAccessorInt extends MethodAccessor implements PropertyAccessorInt {
        public MethodAccessorInt(String name, Type propertyType, Class<?> propertyClass, Method getter, Method setter) {
            super(name, propertyType, propertyClass, getter, setter);
        }

        @Override
        public int getInt(Object object) {
            try {
                return (int) getter.invoke(object);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw errorForGet(e);
            }
        }

        @Override
        public void setInt(Object object, int value) {
            try {
                setter.invoke(object, value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw errorForSet(e);
            }
        }
    }

    static final class MethodAccessorLong extends MethodAccessor implements PropertyAccessorLong {
        public MethodAccessorLong(String name, Type propertyType, Class<?> propertyClass, Method getter, Method setter) {
            super(name, propertyType, propertyClass, getter, setter);
        }

        @Override
        public long getLong(Object object) {
            try {
                return (long) getter.invoke(object);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw errorForGet(e);
            }
        }

        @Override
        public void setLong(Object object, long value) {
            try {
                setter.invoke(object, value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw errorForSet(e);
            }
        }
    }

    static final class MethodAccessorFloat extends MethodAccessor implements PropertyAccessorFloat {
        public MethodAccessorFloat(String name, Type propertyType, Class<?> propertyClass, Method getter, Method setter) {
            super(name, propertyType, propertyClass, getter, setter);
        }

        @Override
        public float getFloat(Object object) {
            try {
                return (float) getter.invoke(object);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw errorForGet(e);
            }
        }

        @Override
        public void setFloat(Object object, float value) {
            try {
                setter.invoke(object, value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw errorForSet(e);
            }
        }
    }

    static final class MethodAccessorDouble extends MethodAccessor implements PropertyAccessorDouble {
        public MethodAccessorDouble(String name, Type propertyType, Class<?> propertyClass, Method getter, Method setter) {
            super(name, propertyType, propertyClass, getter, setter);
        }

        @Override
        public double getDouble(Object object) {
            try {
                return (double) getter.invoke(object);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw errorForGet(e);
            }
        }

        @Override
        public void setDouble(Object object, double value) {
            try {
                setter.invoke(object, value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw errorForSet(e);
            }
        }
    }

    static final class MethodAccessorBoolean extends MethodAccessor implements PropertyAccessorBoolean {
        public MethodAccessorBoolean(String name, Type propertyType, Class<?> propertyClass, Method getter, Method setter) {
            super(name, propertyType, propertyClass, getter, setter);
        }

        @Override
        public boolean getBoolean(Object object) {
            try {
                return (boolean) getter.invoke(object);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw errorForGet(e);
            }
        }

        @Override
        public void setBoolean(Object object, boolean value) {
            try {
                setter.invoke(object, value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw errorForSet(e);
            }
        }
    }

    static final class MethodAccessorObject extends MethodAccessor implements PropertyAccessorObject {
        public MethodAccessorObject(String name, Type propertyType, Class<?> propertyClass, Method getter, Method setter) {
            super(name, propertyType, propertyClass, getter, setter);
        }

        @Override
        public Object getObject(Object object) {
            try {
                return getter.invoke(object);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw errorForGet(e);
            }
        }

        @Override
        public void setObject(Object object, Object value) {
            try {
                setter.invoke(object, value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw errorForSet(e);
            }
        }
    }

    static final class FunctionAccessorByte<T> extends FunctionAccessor<T> implements PropertyAccessorByte {
        private final ToByteFunction<T> getterFunc;
        private final ObjByteConsumer<T> setterFunc;

        public FunctionAccessorByte(String name, ToByteFunction<T> getterFunc, ObjByteConsumer<T> setterFunc) {
            super(name, byte.class, byte.class, getterFunc, setterFunc);
            this.getterFunc = getterFunc;
            this.setterFunc = setterFunc;
        }

        @Override
        public byte getByte(Object object) {
            return getterFunc.applyAsByte((T) object);
        }

        @Override
        public void setByte(Object object, byte value) {
            setterFunc.accept((T) object, value);
        }
    }

    static final class FunctionAccessorShort<T> extends FunctionAccessor<T> implements PropertyAccessorShort {
        private final ToShortFunction<T> getterFunc;
        private final ObjShortConsumer<T> setterFunc;

        public FunctionAccessorShort(String name, ToShortFunction<T> getterFunc, ObjShortConsumer<T> setterFunc) {
            super(name, short.class, short.class, getterFunc, setterFunc);
            this.getterFunc = getterFunc;
            this.setterFunc = setterFunc;
        }

        @Override
        public short getShort(Object object) {
            return getterFunc.applyAsShort((T) object);
        }

        @Override
        public void setShort(Object object, short value) {
            setterFunc.accept((T) object, value);
        }
    }

    static final class FunctionAccessorInt<T> extends FunctionAccessor<T> implements PropertyAccessorInt {
        private final ToIntFunction<T> getterFunc;
        private final ObjIntConsumer<T> setterFunc;

        public FunctionAccessorInt(String name, ToIntFunction<T> getterFunc, ObjIntConsumer<T> setterFunc) {
            super(name, int.class, int.class, getterFunc, setterFunc);
            this.getterFunc = getterFunc;
            this.setterFunc = setterFunc;
        }

        @Override
        public int getInt(Object object) {
            return getterFunc.applyAsInt((T) object);
        }

        @Override
        public void setInt(Object object, int value) {
            setterFunc.accept((T) object, value);
        }
    }

    static final class FunctionAccessorLong<T> extends FunctionAccessor<T> implements PropertyAccessorLong {
        private final ToLongFunction<T> getterFunc;
        private final ObjLongConsumer<T> setterFunc;

        public FunctionAccessorLong(String name, ToLongFunction<T> getterFunc, ObjLongConsumer<T> setterFunc) {
            super(name, long.class, long.class, getterFunc, setterFunc);
            this.getterFunc = getterFunc;
            this.setterFunc = setterFunc;
        }

        @Override
        public long getLong(Object object) {
            return getterFunc.applyAsLong((T) object);
        }

        @Override
        public void setLong(Object object, long value) {
            setterFunc.accept((T) object, value);
        }
    }

    static final class FunctionAccessorFloat<T> extends FunctionAccessor<T> implements PropertyAccessorFloat {
        private final ToFloatFunction<T> getterFunc;
        private final ObjFloatConsumer<T> setterFunc;

        public FunctionAccessorFloat(String name, ToFloatFunction<T> getterFunc, ObjFloatConsumer<T> setterFunc) {
            super(name, float.class, float.class, getterFunc, setterFunc);
            this.getterFunc = getterFunc;
            this.setterFunc = setterFunc;
        }

        @Override
        public float getFloat(Object object) {
            return getterFunc.applyAsFloat((T) object);
        }

        @Override
        public void setFloat(Object object, float value) {
            setterFunc.accept((T) object, value);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    static final class FunctionAccessorDouble<T> extends FunctionAccessor implements PropertyAccessorDouble {
        private final ToDoubleFunction<T> getterFunc;
        private final ObjDoubleConsumer<T> setterFunc;

        public FunctionAccessorDouble(String name, ToDoubleFunction<T> getterFunc, ObjDoubleConsumer<T> setterFunc) {
            super(name, double.class, double.class, getterFunc, setterFunc);
            this.getterFunc = getterFunc;
            this.setterFunc = setterFunc;
        }

        @Override
        public double getDouble(Object object) {
            return getterFunc.applyAsDouble((T) object);
        }

        @Override
        public void setDouble(Object object, double value) {
            setterFunc.accept((T) object, value);
        }
    }

    @SuppressWarnings("unchecked")
    static final class FunctionAccessorBoolean<T> extends FunctionAccessor<T> implements PropertyAccessorBoolean {
        private final Predicate<T> getterFunc;
        private final ObjBoolConsumer<T> setterFunc;

        public FunctionAccessorBoolean(String name, Predicate<T> getterFunc, ObjBoolConsumer<T> setterFunc) {
            super(name, boolean.class, boolean.class, getterFunc, setterFunc);
            this.getterFunc = getterFunc;
            this.setterFunc = setterFunc;
        }

        @Override
        public boolean getBoolean(Object object) {
            return getterFunc.test((T) object);
        }

        @Override
        public void setBoolean(Object object, boolean value) {
            setterFunc.accept((T) object, value);
        }
    }

    static final class FunctionAccessorObject<T, V> extends FunctionAccessor implements PropertyAccessorObject {
        private final Function<T, V> getterFunc;
        private final BiConsumer<T, V> setterFunc;

        public FunctionAccessorObject(String name,
                Type propertyType,
                Class<?> propertyClass,
                Function<T, V> getterFunc,
                BiConsumer<T, V> setterFunc) {
            super(name, propertyType, propertyClass, getterFunc, setterFunc);
            this.getterFunc = getterFunc;
            this.setterFunc = setterFunc;
        }

        @Override
        public Object getObject(Object object) {
            return getterFunc.apply((T) object);
        }

        @Override
        public void setObject(Object object, Object value) {
            setterFunc.accept((T) object, (V) value);
        }
    }

    static final class FunctionAccessorChar<T> extends FunctionAccessor<T> implements PropertyAccessorChar {
        private final ToCharFunction<T> getterFunc;
        private final ObjCharConsumer<T> setterFunc;
        public FunctionAccessorChar(String name, ToCharFunction<T> getterFunc, ObjCharConsumer<T> setterFunc) {
            super(name, char.class, char.class, getterFunc, setterFunc);
            this.getterFunc = getterFunc;
            this.setterFunc = setterFunc;
        }

        @Override
        public char getChar(Object object) {
            return getterFunc.applyAsChar((T) object);
        }

        @Override
        public void setChar(Object object, char value) {
            setterFunc.accept((T) object, value);
        }
    }
}
