package com.alibaba.fastjson2.internal;

import java.lang.reflect.Field;
import java.util.Objects;

import static com.alibaba.fastjson2.util.JDKUtils.UNSAFE;

public final class FieldAccessorUnsafe extends FieldAccessor {
    private final long fieldOffset;

    public FieldAccessorUnsafe(Field field) {
        super(field);
        this.fieldOffset = UNSAFE.objectFieldOffset(field);
    }

    @Override
    public Object getObject(Object object) {
        return UNSAFE.getObject(Objects.requireNonNull(object), fieldOffset);
    }

    @Override
    public byte getByte(Object object) {
        return UNSAFE.getByte(Objects.requireNonNull(object), fieldOffset);
    }

    @Override
    public char getChar(Object object) {
        return UNSAFE.getChar(Objects.requireNonNull(object), fieldOffset);
    }

    @Override
    public short getShort(Object object) {
        return UNSAFE.getShort(Objects.requireNonNull(object), fieldOffset);
    }

    @Override
    public int getInt(Object object) {
        return UNSAFE.getInt(Objects.requireNonNull(object), fieldOffset);
    }

    @Override
    public long getLong(Object object) {
        return UNSAFE.getLong(Objects.requireNonNull(object), fieldOffset);
    }

    @Override
    public float getFloat(Object object) {
        return UNSAFE.getFloat(Objects.requireNonNull(object), fieldOffset);
    }

    @Override
    public double getDouble(Object object) {
        return UNSAFE.getDouble(Objects.requireNonNull(object), fieldOffset);
    }

    @Override
    public boolean getBoolean(Object object) {
        return UNSAFE.getBoolean(Objects.requireNonNull(object), fieldOffset);
    }

    @Override
    public void setObject(Object object, Object value) {
        UNSAFE.putObject(Objects.requireNonNull(object), fieldOffset, value);
    }

    @Override
    public void setByte(Object object, byte value) {
        UNSAFE.putByte(Objects.requireNonNull(object), fieldOffset, value);
    }

    @Override
    public void setShort(Object object, short value) {
        UNSAFE.putShort(Objects.requireNonNull(object), fieldOffset, value);
    }

    @Override
    public void setChar(Object object, char value) {
        UNSAFE.putChar(Objects.requireNonNull(object), fieldOffset, value);
    }

    @Override
    public void setInt(Object object, int value) {
        UNSAFE.putInt(Objects.requireNonNull(object), fieldOffset, value);
    }

    @Override
    public void setLong(Object object, long value) {
        UNSAFE.putLong(Objects.requireNonNull(object), fieldOffset, value);
    }

    @Override
    public void setFloat(Object object, float value) {
        UNSAFE.putFloat(Objects.requireNonNull(object), fieldOffset, value);
    }

    @Override
    public void setDouble(Object object, double value) {
        UNSAFE.putDouble(Objects.requireNonNull(object), fieldOffset, value);
    }

    @Override
    public void setBoolean(Object object, boolean value) {
        UNSAFE.putBoolean(Objects.requireNonNull(object), fieldOffset, value);
    }
}
