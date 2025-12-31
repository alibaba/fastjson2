package com.alibaba.fastjson2.internal;

import com.alibaba.fastjson2.JSONException;

import java.lang.reflect.Field;

public class FieldAccessorReflect extends FieldAccessor {
    public FieldAccessorReflect(Field field) {
        super(field);
        if (getClass() == FieldAccessorReflect.class) {
            try {
                field.setAccessible(true);
            } catch (RuntimeException e) {
                throw errorOnSetAccessible(field, e);
            }
        }
    }

    private static JSONException errorOnSetAccessible(Field field, RuntimeException e) {
        return new JSONException(field.toString() + " setAccessible error", e);
    }

    public Object getObject(Object object) {
        try {
            return field.get(object);
        } catch (IllegalAccessException e) {
            throw errorForGet(e);
        }
    }

    public byte getByte(Object object) {
        try {
            return field.getByte(object);
        } catch (IllegalAccessException e) {
            throw errorForGet(e);
        }
    }

    public char getChar(Object object) {
        try {
            return field.getChar(object);
        } catch (IllegalAccessException e) {
            throw errorForGet(e);
        }
    }

    public short getShort(Object object) {
        try {
            return field.getShort(object);
        } catch (IllegalAccessException e) {
            throw errorForGet(e);
        }
    }

    public int getInt(Object object) {
        try {
            return field.getInt(object);
        } catch (IllegalAccessException e) {
            throw errorForGet(e);
        }
    }

    public long getLong(Object object) {
        try {
            return field.getLong(object);
        } catch (IllegalAccessException e) {
            throw errorForGet(e);
        }
    }

    public float getFloat(Object object) {
        try {
            return field.getFloat(object);
        } catch (IllegalAccessException e) {
            throw errorForGet(e);
        }
    }

    public double getDouble(Object object) {
        try {
            return field.getDouble(object);
        } catch (IllegalAccessException e) {
            throw errorForGet(e);
        }
    }

    public boolean getBoolean(Object object) {
        try {
            return field.getBoolean(object);
        } catch (IllegalAccessException e) {
            throw errorForGet(e);
        }
    }

    public void setObject(Object object, Object value) {
        try {
            field.set(object, value);
        } catch (IllegalAccessException e) {
            throw errorForSet(e);
        }
    }

    public void setByte(Object object, byte value) {
        try {
            field.setByte(object, value);
        } catch (IllegalAccessException e) {
            throw errorForSet(e);
        }
    }

    public void setShort(Object object, short value) {
        try {
            field.setShort(object, value);
        } catch (IllegalAccessException e) {
            throw errorForSet(e);
        }
    }

    public void setChar(Object object, char value) {
        try {
            field.setChar(object, value);
        } catch (IllegalAccessException e) {
            throw errorForSet(e);
        }
    }

    public void setInt(Object object, int value) {
        try {
            field.setInt(object, value);
        } catch (IllegalAccessException e) {
            throw errorForSet(e);
        }
    }

    public void setLong(Object object, long value) {
        try {
            field.setLong(object, value);
        } catch (IllegalAccessException e) {
            throw errorForSet(e);
        }
    }

    public void setFloat(Object object, float value) {
        try {
            field.setFloat(object, value);
        } catch (IllegalAccessException e) {
            throw errorForSet(e);
        }
    }

    public void setDouble(Object object, double value) {
        try {
            field.setDouble(object, value);
        } catch (IllegalAccessException e) {
            throw errorForSet(e);
        }
    }

    public void setBoolean(Object object, boolean value) {
        try {
            field.setBoolean(object, value);
        } catch (IllegalAccessException e) {
            throw errorForSet(e);
        }
    }

    JSONException errorForGet(IllegalAccessException e) {
        return new JSONException(field.toString() + " get error", e);
    }

    JSONException errorForSet(IllegalAccessException e) {
        return new JSONException(field.toString() + " set error", e);
    }
}
