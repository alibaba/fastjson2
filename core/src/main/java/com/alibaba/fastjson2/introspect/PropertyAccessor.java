package com.alibaba.fastjson2.introspect;

import java.lang.reflect.Type;

/**
 * Interface for accessing object properties generically.
 * Provides unified methods for getting and setting properties of objects,
 * supporting both primitive types and objects through various accessor methods.
 * Used internally by fastjson2 for efficient property access during serialization
 * and deserialization operations.
 */
public interface PropertyAccessor {
    /**
     * Returns the name of the property being accessed.
     *
     * @return the property name
     */
    String name();

    /**
     * Returns the runtime class of the property.
     *
     * @return the property class
     */
    Class<?> propertyClass();

    /**
     * Returns the generic type of the property.
     *
     * @return the property type
     */
    Type propertyType();

    /**
     * Checks if the property supports getting values.
     *
     * @return true if the property can be read, false otherwise
     */
    boolean supportGet();

    /**
     * Checks if the property supports setting values.
     *
     * @return true if the property can be written, false otherwise
     */
    boolean supportSet();

    // Getter methods

    /**
     * Gets the property value as an Object.
     *
     * @param object the object to get the property from
     * @return the property value as Object
     */
    Object getObject(Object object);

    /**
     * Gets the property value as a byte.
     *
     * @param object the object to get the property from
     * @return the property value as byte
     */
    byte getByteValue(Object object);

    /**
     * Gets the property value as a char.
     *
     * @param object the object to get the property from
     * @return the property value as char
     */
    char getCharValue(Object object);

    /**
     * Gets the property value as a short.
     *
     * @param object the object to get the property from
     * @return the property value as short
     */
    short getShortValue(Object object);

    /**
     * Gets the property value as an int.
     *
     * @param object the object to get the property from
     * @return the property value as int
     */
    int getIntValue(Object object);

    /**
     * Gets the property value as a long.
     *
     * @param object the object to get the property from
     * @return the property value as long
     */
    long getLongValue(Object object);

    /**
     * Gets the property value as a float.
     *
     * @param object the object to get the property from
     * @return the property value as float
     */
    float getFloatValue(Object object);

    /**
     * Gets the property value as a double.
     *
     * @param object the object to get the property from
     * @return the property value as double
     */
    double getDoubleValue(Object object);

    /**
     * Gets the property value as a boolean.
     *
     * @param object the object to get the property from
     * @return the property value as boolean
     */
    boolean getBooleanValue(Object object);

    // Setter methods

    /**
     * Sets the property value from an Object.
     *
     * @param object the object to set the property on
     * @param value the value to set
     */
    void setObject(Object object, Object value);

    /**
     * Sets the property value from a byte.
     *
     * @param object the object to set the property on
     * @param value the byte value to set
     */
    void setByteValue(Object object, byte value);

    /**
     * Sets the property value from a char.
     *
     * @param object the object to set the property on
     * @param value the char value to set
     */
    void setCharValue(Object object, char value);

    /**
     * Sets the property value from a short.
     *
     * @param object the object to set the property on
     * @param value the short value to set
     */
    void setShortValue(Object object, short value);

    /**
     * Sets the property value from an int.
     *
     * @param object the object to set the property on
     * @param value the int value to set
     */
    void setIntValue(Object object, int value);

    /**
     * Sets the property value from a long.
     *
     * @param object the object to set the property on
     * @param value the long value to set
     */
    void setLongValue(Object object, long value);

    /**
     * Sets the property value from a float.
     *
     * @param object the object to set the property on
     * @param value the float value to set
     */
    void setFloatValue(Object object, float value);

    /**
     * Sets the property value from a double.
     *
     * @param object the object to set the property on
     * @param value the double value to set
     */
    void setDoubleValue(Object object, double value);

    /**
     * Sets the property value from a boolean.
     *
     * @param object the object to set the property on
     * @param value the boolean value to set
     */
    void setBooleanValue(Object object, boolean value);
}
