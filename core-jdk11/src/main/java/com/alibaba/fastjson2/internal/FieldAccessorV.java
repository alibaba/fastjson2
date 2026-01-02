package com.alibaba.fastjson2.internal;

import com.alibaba.fastjson2.JSONException;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;

public final class FieldAccessorV extends FieldAccessor {
    final VarHandle varHandle;
    public FieldAccessorV(Field field) {
        this(MethodHandles.lookup(), field);
    }

    public FieldAccessorV(MethodHandles.Lookup lookup, Field field) {
        super(field);
        try {
            this.varHandle = lookup.findVarHandle(field.getDeclaringClass(), field.getName(), field.getType());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new JSONException("findVarHandle error", e);
        }
    }

    @Override
    public Object getObject(Object object) {
        return varHandle.get(object);
    }

    @Override
    public byte getByte(Object object) {
        return (byte) varHandle.get(object);
    }

    @Override
    public char getChar(Object object) {
        return (char) varHandle.get(object);
    }

    @Override
    public short getShort(Object object) {
        return (short) varHandle.get(object);
    }

    @Override
    public int getInt(Object object) {
        return (int) varHandle.get(object);
    }

    @Override
    public long getLong(Object object) {
        return (long) varHandle.get(object);
    }

    @Override
    public float getFloat(Object object) {
        return (float) varHandle.get(object);
    }

    @Override
    public double getDouble(Object object) {
        return (double) varHandle.get(object);
    }

    @Override
    public boolean getBoolean(Object object) {
        return (boolean) varHandle.get(object);
    }

    @Override
    public String getString(Object object) {
        return (String) varHandle.get(object);
    }

    @Override
    public BigInteger getBigInteger(Object object) {
        return (BigInteger) varHandle.get(object);
    }

    @Override
    public BigDecimal getBigDecimal(Object object) {
        return (BigDecimal) varHandle.get(object);
    }

    @Override
    public void setObject(Object object, Object value) {
        varHandle.set(object, value);
    }

    @Override
    public void setByte(Object object, byte value) {
        varHandle.set(object, value);
    }

    @Override
    public void setShort(Object object, short value) {
        varHandle.set(object, value);
    }

    @Override
    public void setInt(Object object, int value) {
        varHandle.set(object, value);
    }

    @Override
    public void setLong(Object object, long value) {
        varHandle.set(object, value);
    }

    @Override
    public void setFloat(Object object, float value) {
        varHandle.set(object, value);
    }

    @Override
    public void setDouble(Object object, double value) {
        varHandle.set(object, value);
    }

    @Override
    public void setChar(Object object, char value) {
        varHandle.set(object, value);
    }

    @Override
    public void setBoolean(Object object, boolean value) {
        varHandle.set(object, value);
    }

    @Override
    public void setString(Object object, String value) {
        varHandle.set(object, value);
    }

    @Override
    public void setBigInteger(Object object, BigInteger value) {
        varHandle.set(object, value);
    }

    @Override
    public void setBigDecimal(Object object, BigDecimal value) {
        varHandle.set(object, value);
    }
}
