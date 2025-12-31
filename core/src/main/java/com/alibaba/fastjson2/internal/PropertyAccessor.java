package com.alibaba.fastjson2.internal;

import java.lang.reflect.Type;

public interface PropertyAccessor {
    String name();
    Class<?> propertyClass();
    Type propertyType();

    boolean supportGet();
    boolean supportSet();

    // Getter methods
    Object getObject(Object object);
    byte getByte(Object object);
    char getChar(Object object);
    short getShort(Object object);
    int getInt(Object object);
    long getLong(Object object);
    float getFloat(Object object);
    double getDouble(Object object);
    boolean getBoolean(Object object);

    // Setter methods
    void setObject(Object object, Object value);
    void setByte(Object object, byte value);
    void setShort(Object object, short value);
    void setChar(Object object, char value);
    void setInt(Object object, int value);
    void setLong(Object object, long value);
    void setFloat(Object object, float value);
    void setDouble(Object object, double value);
    void setBoolean(Object object, boolean value);
}
