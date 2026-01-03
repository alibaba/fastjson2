package com.alibaba.fastjson2.internal;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.function.*;
import com.alibaba.fastjson2.util.BeanUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.*;

import static com.alibaba.fastjson2.internal.Cast.*;

/**
 * A factory class for creating property accessors that provide efficient
 * getter and setter operations for object properties using reflection,
 * method handles, or functional interfaces.
 *
 * <p>This factory supports different types of property access mechanisms:
 * <ul>
 *   <li>Field-based access - Direct field access using reflection</li>
 *   <li>Method-based access - Getter/setter method invocation</li>
 *   <li>Functional access - Using functional interfaces for custom access logic</li>
 * </ul>
 *
 * <p>The factory creates specialized accessor implementations for different
 * data types (primitives, String, BigInteger, BigDecimal) to optimize
 * performance and avoid boxing/unboxing overhead where possible.
 *
 * @since 2.0
 */
@SuppressWarnings("ALL")
public class PropertyAccessorFactory {
    /**
     * Creates a property accessor for the specified field.
     * This method analyzes the field type and returns an appropriate
     * accessor implementation optimized for that type.
     *
     * @param field the field to create an accessor for
     * @return a PropertyAccessor instance for the specified field
     */
    public PropertyAccessor create(Field field) {
        return createInternal(field);
    }

    /**
     * Internal method to create a field-based property accessor
     * based on the field's type. Returns a specialized accessor
     * implementation depending on the field type (e.g., primitive,
     * String, BigInteger, etc.).
     *
     * @param field the field to create an accessor for
     * @return a specialized FieldAccessor instance for the field type
     */
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
        if (field.getType() == String.class) {
            return new FieldAccessorReflectString(field);
        }
        if (field.getType() == BigInteger.class) {
            return new FieldAccessorReflectBigInteger(field);
        }
        if (field.getType() == BigDecimal.class) {
            return new FieldAccessorReflectBigDecimal(field);
        }
        return new FieldAccessorReflectObject(field);
    }

    /**
     * Interface for property accessors that handle boolean-typed properties.
     * Provides methods to get and set boolean values, with conversions to
     * other types as needed.
     */
    protected interface PropertyAccessorBoolean extends PropertyAccessor {
        @Override
        default Object getObject(Object object) {
            return getBooleanValue(object);
        }

        @Override
        default byte getByteValue(Object object) {
            return toByteValue(getBooleanValue(object));
        }

        @Override
        default char getCharValue(Object object) {
            return toCharValue(getBooleanValue(object));
        }

        @Override
        default short getShortValue(Object object) {
            return toShortValue(getBooleanValue(object));
        }

        @Override
        default int getIntValue(Object object) {
            return toIntValue(getBooleanValue(object));
        }

        @Override
        default long getLongValue(Object object) {
            return toLongValue(getBooleanValue(object));
        }

        @Override
        default float getFloatValue(Object object) {
            return toFloatValue(getBooleanValue(object));
        }

        @Override
        default double getDoubleValue(Object object) {
            return toDoubleValue(getBooleanValue(object));
        }

        @Override
        default void setObject(Object object, Object value) {
            setBooleanValue(object, toBooleanValue(value));
        }

        @Override
        default void setByteValue(Object object, byte value) {
            setBooleanValue(object, toBooleanValue(value));
        }

        @Override
        default void setCharValue(Object object, char value) {
            setBooleanValue(object, toBooleanValue(value));
        }

        @Override
        default void setShortValue(Object object, short value) {
            setBooleanValue(object, toBooleanValue(value));
        }

        @Override
        default void setIntValue(Object object, int value) {
            setBooleanValue(object, toBooleanValue(value));
        }

        @Override
        default void setLongValue(Object object, long value) {
            setBooleanValue(object, toBooleanValue(value));
        }

        @Override
        default void setFloatValue(Object object, float value) {
            setBooleanValue(object, toBooleanValue(value));
        }

        @Override
        default void setDoubleValue(Object object, double value) {
            setBooleanValue(object, toBooleanValue(value));
        }

        @Override
        default String getString(Object object) {
            return Cast.toString(getBooleanValue(object));
        }

        @Override
        default BigInteger getBigInteger(Object object) {
            return toBigInteger(getBooleanValue(object));
        }

        @Override
        default BigDecimal getBigDecimal(Object object) {
            return toBigDecimal(getBooleanValue(object));
        }

        @Override
        default void setString(Object object, String value) {
            setBooleanValue(object, toBooleanValue(value));
        }

        @Override
        default void setBigInteger(Object object, BigInteger value) {
            setBooleanValue(object, toBooleanValue(value));
        }

        @Override
        default void setBigDecimal(Object object, BigDecimal value) {
            setBooleanValue(object, toBooleanValue(value));
        }
    }

    /**
     * Interface for property accessors that handle byte-typed properties.
     * Provides methods to get and set byte values, with conversions to
     * other types as needed.
     */
    protected interface PropertyAccessorByte extends PropertyAccessor {
        @Override
        default Object getObject(Object object) {
            return getByteValue(object);
        }

        @Override
        default char getCharValue(Object object) {
            return toCharValue(getByteValue(object));
        }

        @Override
        default short getShortValue(Object object) {
            return toShortValue(getByteValue(object));
        }

        @Override
        default int getIntValue(Object object) {
            return toIntValue(getByteValue(object));
        }

        @Override
        default long getLongValue(Object object) {
            return toLongValue(getByteValue(object));
        }

        @Override
        default float getFloatValue(Object object) {
            return toFloatValue(getByteValue(object));
        }

        @Override
        default double getDoubleValue(Object object) {
            return toDoubleValue(getByteValue(object));
        }

        @Override
        default boolean getBooleanValue(Object object) {
            return toBooleanValue(getByteValue(object));
        }

        @Override
        default void setObject(Object object, Object value) {
            setByteValue(object, toByteValue(value));
        }

        @Override
        default void setCharValue(Object object, char value) {
            setByteValue(object, (byte) value);
        }

        @Override
        default void setShortValue(Object object, short value) {
            setByteValue(object, (byte) value);
        }

        @Override
        default void setIntValue(Object object, int value) {
            setByteValue(object, (byte) value);
        }

        @Override
        default void setLongValue(Object object, long value) {
            setByteValue(object, (byte) value);
        }

        @Override
        default void setFloatValue(Object object, float value) {
            setByteValue(object, (byte) value);
        }

        @Override
        default void setDoubleValue(Object object, double value) {
            setByteValue(object, (byte) value);
        }

        @Override
        default void setBooleanValue(Object object, boolean value) {
            setByteValue(object, toByteValue(value));
        }

        @Override
        default String getString(Object object) {
            return Cast.toString(getByteValue(object));
        }

        @Override
        default BigInteger getBigInteger(Object object) {
            return toBigInteger(getByteValue(object));
        }

        @Override
        default BigDecimal getBigDecimal(Object object) {
            return toBigDecimal(getByteValue(object));
        }

        @Override
        default void setString(Object object, String value) {
            setByteValue(object, toByteValue(value));
        }

        @Override
        default void setBigInteger(Object object, BigInteger value) {
            setByteValue(object, toByteValue(value));
        }

        @Override
        default void setBigDecimal(Object object, BigDecimal value) {
            setByteValue(object, toByteValue(value));
        }
    }

    /**
     * Interface for property accessors that handle short-typed properties.
     * Provides methods to get and set short values, with conversions to
     * other types as needed.
     */
    protected interface PropertyAccessorShort extends PropertyAccessor {
        default Object getObject(Object object) {
            return getShortValue(object);
        }

        default byte getByteValue(Object object) {
            return toByteValue(getShortValue(object));
        }

        default char getCharValue(Object object) {
            return toCharValue(getShortValue(object));
        }

        default int getIntValue(Object object) {
            return toIntValue(getShortValue(object));
        }

        default long getLongValue(Object object) {
            return toLongValue(getShortValue(object));
        }

        default float getFloatValue(Object object) {
            return toFloatValue(getShortValue(object));
        }

        default double getDoubleValue(Object object) {
            return toDoubleValue(getShortValue(object));
        }

        default boolean getBooleanValue(Object object) {
            return toBooleanValue(getShortValue(object));
        }

        default void setObject(Object object, Object value) {
            setShortValue(object, toShortValue(value));
        }

        default void setByteValue(Object object, byte value) {
            setShortValue(object, value);
        }

        default void setCharValue(Object object, char value) {
            setShortValue(object, (short) value);
        }

        default void setIntValue(Object object, int value) {
            setShortValue(object, (short) value);
        }

        default void setLongValue(Object object, long value) {
            setShortValue(object, (short) value);
        }

        default void setFloatValue(Object object, float value) {
            setShortValue(object, (short) value);
        }

        default void setDoubleValue(Object object, double value) {
            setShortValue(object, (short) value);
        }

        default void setBooleanValue(Object object, boolean value) {
            setShortValue(object, toShortValue(value));
        }

        @Override
        default String getString(Object object) {
            return Cast.toString(getShortValue(object));
        }

        @Override
        default BigInteger getBigInteger(Object object) {
            return toBigInteger(getShortValue(object));
        }

        @Override
        default BigDecimal getBigDecimal(Object object) {
            return toBigDecimal(getShortValue(object));
        }

        @Override
        default void setString(Object object, String value) {
            setShortValue(object, toShortValue(value));
        }

        @Override
        default void setBigInteger(Object object, BigInteger value) {
            setShortValue(object, toShortValue(value));
        }

        @Override
        default void setBigDecimal(Object object, BigDecimal value) {
            setShortValue(object, toShortValue(value));
        }
    }

    /**
     * Interface for property accessors that handle int-typed properties.
     * Provides methods to get and set int values, with conversions to
     * other types as needed.
     */
    protected interface PropertyAccessorInt extends PropertyAccessor {
        @Override
        default Object getObject(Object object) {
            return getIntValue(object);
        }

        @Override
        default byte getByteValue(Object object) {
            return toByteValue(getIntValue(object));
        }

        @Override
        default char getCharValue(Object object) {
            return toCharValue(getIntValue(object));
        }

        @Override
        default short getShortValue(Object object) {
            return toShortValue(getIntValue(object));
        }

        @Override
        default long getLongValue(Object object) {
            return toLongValue(getIntValue(object));
        }

        @Override
        default float getFloatValue(Object object) {
            return toFloatValue(getIntValue(object));
        }

        @Override
        default double getDoubleValue(Object object) {
            return toDoubleValue(getIntValue(object));
        }

        @Override
        default boolean getBooleanValue(Object object) {
            return toBooleanValue(getIntValue(object));
        }

        @Override
        default void setObject(Object object, Object value) {
            setIntValue(object, toIntValue(value));
        }

        @Override
        default void setByteValue(Object object, byte value) {
            setIntValue(object, value);
        }

        @Override
        default void setShortValue(Object object, short value) {
            setIntValue(object, value);
        }

        @Override
        default void setCharValue(Object object, char value) {
            setIntValue(object, value);
        }

        @Override
        default void setLongValue(Object object, long value) {
            setIntValue(object, (int) value);
        }

        @Override
        default void setFloatValue(Object object, float value) {
            setIntValue(object, (int) value);
        }

        @Override
        default void setDoubleValue(Object object, double value) {
            setIntValue(object, (int) value);
        }

        @Override
        default void setBooleanValue(Object object, boolean value) {
            setIntValue(object, toIntValue(value));
        }

        @Override
        default String getString(Object object) {
            return Cast.toString(getIntValue(object));
        }

        @Override
        default BigInteger getBigInteger(Object object) {
            return toBigInteger(getIntValue(object));
        }

        @Override
        default BigDecimal getBigDecimal(Object object) {
            return toBigDecimal(getIntValue(object));
        }

        @Override
        default void setString(Object object, String value) {
            setIntValue(object, toIntValue(value));
        }

        @Override
        default void setBigInteger(Object object, BigInteger value) {
            setIntValue(object, toIntValue(value));
        }

        @Override
        default void setBigDecimal(Object object, BigDecimal value) {
            setIntValue(object, toIntValue(value));
        }
    }

    /**
     * Interface for property accessors that handle long-typed properties.
     * Provides methods to get and set long values, with conversions to
     * other types as needed.
     */
    protected interface PropertyAccessorLong extends PropertyAccessor {
        @Override
        default Object getObject(Object object) {
            return getLongValue(object);
        }

        @Override
        default byte getByteValue(Object object) {
            return toByteValue(getLongValue(object));
        }

        @Override
        default char getCharValue(Object object) {
            return toCharValue(getLongValue(object));
        }

        @Override
        default short getShortValue(Object object) {
            return toShortValue(getLongValue(object));
        }

        @Override
        default int getIntValue(Object object) {
            return toIntValue(getLongValue(object));
        }

        @Override
        default float getFloatValue(Object object) {
            return toFloatValue(getLongValue(object));
        }

        @Override
        default double getDoubleValue(Object object) {
            return toDoubleValue(getLongValue(object));
        }

        @Override
        default boolean getBooleanValue(Object object) {
            return toBooleanValue(getLongValue(object));
        }

        @Override
        default void setObject(Object object, Object value) {
            setLongValue(object, toLongValue(value));
        }

        @Override
        default void setByteValue(Object object, byte value) {
            setLongValue(object, value);
        }

        @Override
        default void setCharValue(Object object, char value) {
            setLongValue(object, value);
        }

        @Override
        default void setShortValue(Object object, short value) {
            setLongValue(object, value);
        }

        @Override
        default void setIntValue(Object object, int value) {
            setLongValue(object, value);
        }

        @Override
        default void setFloatValue(Object object, float value) {
            setLongValue(object, (long) value);
        }

        @Override
        default void setDoubleValue(Object object, double value) {
            setLongValue(object, (long) value);
        }

        @Override
        default void setBooleanValue(Object object, boolean value) {
            setLongValue(object, toLongValue(value));
        }

        @Override
        default String getString(Object object) {
            return Cast.toString(getLongValue(object));
        }

        @Override
        default BigInteger getBigInteger(Object object) {
            return toBigInteger(getLongValue(object));
        }

        @Override
        default BigDecimal getBigDecimal(Object object) {
            return toBigDecimal(getLongValue(object));
        }

        @Override
        default void setString(Object object, String value) {
            setLongValue(object, toLongValue(value));
        }

        @Override
        default void setBigInteger(Object object, BigInteger value) {
            setLongValue(object, toLongValue(value));
        }

        @Override
        default void setBigDecimal(Object object, BigDecimal value) {
            setLongValue(object, toLongValue(value));
        }
    }

    /**
     * Interface for property accessors that handle float-typed properties.
     * Provides methods to get and set float values, with conversions to
     * other types as needed.
     */
    interface PropertyAccessorFloat extends PropertyAccessor {
        @Override
        default Object getObject(Object object) {
            return getFloatValue(object);
        }

        @Override
        default byte getByteValue(Object object) {
            return toByteValue(getFloatValue(object));
        }

        @Override
        default char getCharValue(Object object) {
            return toCharValue(getFloatValue(object));
        }

        @Override
        default short getShortValue(Object object) {
            return toShortValue(getFloatValue(object));
        }

        @Override
        default int getIntValue(Object object) {
            return toIntValue(getFloatValue(object));
        }

        @Override
        default long getLongValue(Object object) {
            return toLongValue(getFloatValue(object));
        }

        @Override
        default double getDoubleValue(Object object) {
            return toDoubleValue(getFloatValue(object));
        }

        @Override
        default boolean getBooleanValue(Object object) {
            return toBooleanValue(getFloatValue(object));
        }

        @Override
        default void setObject(Object object, Object value) {
            setFloatValue(object, toFloatValue(value));
        }

        @Override
        default void setByteValue(Object object, byte value) {
            setFloatValue(object, value);
        }

        @Override
        default void setCharValue(Object object, char value) {
            setFloatValue(object, value);
        }

        @Override
        default void setShortValue(Object object, short value) {
            setFloatValue(object, value);
        }

        @Override
        default void setIntValue(Object object, int value) {
            setFloatValue(object, value);
        }

        @Override
        default void setLongValue(Object object, long value) {
            setFloatValue(object, value);
        }

        @Override
        default void setDoubleValue(Object object, double value) {
            setFloatValue(object, (float) value);
        }

        @Override
        default void setBooleanValue(Object object, boolean value) {
            setFloatValue(object, toFloatValue(value));
        }

        @Override
        default String getString(Object object) {
            return Cast.toString(getFloatValue(object));
        }

        @Override
        default BigInteger getBigInteger(Object object) {
            return toBigInteger(getFloatValue(object));
        }

        @Override
        default BigDecimal getBigDecimal(Object object) {
            return toBigDecimal(getFloatValue(object));
        }

        @Override
        default void setString(Object object, String value) {
            setFloatValue(object, toFloatValue(value));
        }

        @Override
        default void setBigInteger(Object object, BigInteger value) {
            setFloatValue(object, toFloatValue(value));
        }

        @Override
        default void setBigDecimal(Object object, BigDecimal value) {
            setFloatValue(object, toFloatValue(value));
        }
    }

    /**
     * Interface for property accessors that handle double-typed properties.
     * Provides methods to get and set double values, with conversions to
     * other types as needed.
     */
    protected interface PropertyAccessorDouble extends PropertyAccessor {
        @Override
        default Object getObject(Object object) {
            return getDoubleValue(object);
        }

        @Override
        default byte getByteValue(Object object) {
            return toByteValue(getDoubleValue(object));
        }

        @Override
        default char getCharValue(Object object) {
            return toCharValue(getDoubleValue(object));
        }

        @Override
        default short getShortValue(Object object) {
            return toShortValue(getDoubleValue(object));
        }

        @Override
        default int getIntValue(Object object) {
            return toIntValue(getDoubleValue(object));
        }

        @Override
        default long getLongValue(Object object) {
            return toLongValue(getDoubleValue(object));
        }

        @Override
        default float getFloatValue(Object object) {
            return toFloatValue(getDoubleValue(object));
        }

        @Override
        default boolean getBooleanValue(Object object) {
            return toBooleanValue(getDoubleValue(object));
        }

        @Override
        default void setObject(Object object, Object value) {
            setDoubleValue(object, toDoubleValue(value));
        }

        @Override
        default void setByteValue(Object object, byte value) {
            setDoubleValue(object, toDoubleValue(value));
        }

        @Override
        default void setCharValue(Object object, char value) {
            setDoubleValue(object, toDoubleValue(value));
        }

        @Override
        default void setShortValue(Object object, short value) {
            setDoubleValue(object, toDoubleValue(value));
        }

        @Override
        default void setIntValue(Object object, int value) {
            setDoubleValue(object, toDoubleValue(value));
        }

        @Override
        default void setLongValue(Object object, long value) {
            setDoubleValue(object, toDoubleValue(value));
        }

        @Override
        default void setFloatValue(Object object, float value) {
            setDoubleValue(object, value);
        }

        @Override
        default void setBooleanValue(Object object, boolean value) {
            setDoubleValue(object, toDoubleValue(value));
        }

        @Override
        default String getString(Object object) {
            return Cast.toString(getDoubleValue(object));
        }

        @Override
        default BigInteger getBigInteger(Object object) {
            return toBigInteger(getDoubleValue(object));
        }

        @Override
        default BigDecimal getBigDecimal(Object object) {
            return toBigDecimal(getDoubleValue(object));
        }

        @Override
        default void setString(Object object, String value) {
            setDoubleValue(object, toDoubleValue(value));
        }

        @Override
        default void setBigInteger(Object object, BigInteger value) {
            setDoubleValue(object, toDoubleValue(value));
        }

        @Override
        default void setBigDecimal(Object object, BigDecimal value) {
            setDoubleValue(object, toDoubleValue(value));
        }
    }

    /**
     * Interface for property accessors that handle char-typed properties.
     * Provides methods to get and set char values, with conversions to
     * other types as needed.
     */
    protected interface PropertyAccessorChar extends PropertyAccessor {
        @Override
        default Object getObject(Object object) {
            return getCharValue(object);
        }

        @Override
        default byte getByteValue(Object object) {
            return toByteValue(getCharValue(object));
        }

        @Override
        default short getShortValue(Object object) {
            return toShortValue(getCharValue(object));
        }

        @Override
        default int getIntValue(Object object) {
            return toIntValue(getCharValue(object));
        }

        @Override
        default long getLongValue(Object object) {
            return toLongValue(getCharValue(object));
        }

        @Override
        default float getFloatValue(Object object) {
            return toFloatValue(getCharValue(object));
        }

        @Override
        default double getDoubleValue(Object object) {
            return toDoubleValue(getCharValue(object));
        }

        @Override
        default boolean getBooleanValue(Object object) {
            return toBooleanValue(getCharValue(object));
        }

        @Override
        default void setObject(Object object, Object value) {
            setCharValue(object, toCharValue(value));
        }

        @Override
        default void setByteValue(Object object, byte value) {
            setCharValue(object, toCharValue(value));
        }

        @Override
        default void setShortValue(Object object, short value) {
            setCharValue(object, toCharValue(value));
        }

        @Override
        default void setIntValue(Object object, int value) {
            setCharValue(object, toCharValue(value));
        }

        @Override
        default void setLongValue(Object object, long value) {
            setCharValue(object, toCharValue(value));
        }

        @Override
        default void setFloatValue(Object object, float value) {
            setCharValue(object, toCharValue(value));
        }

        @Override
        default void setDoubleValue(Object object, double value) {
            setCharValue(object, toCharValue(value));
        }

        @Override
        default void setBooleanValue(Object object, boolean value) {
            setCharValue(object, toCharValue(value));
        }

        @Override
        default String getString(Object object) {
            return Cast.toString(getCharValue(object));
        }

        @Override
        default BigInteger getBigInteger(Object object) {
            return toBigInteger(getCharValue(object));
        }

        @Override
        default BigDecimal getBigDecimal(Object object) {
            return toBigDecimal(getCharValue(object));
        }

        @Override
        default void setString(Object object, String value) {
            setCharValue(object, toCharValue(value));
        }

        @Override
        default void setBigInteger(Object object, BigInteger value) {
            setCharValue(object, toCharValue(value));
        }

        @Override
        default void setBigDecimal(Object object, BigDecimal value) {
            setCharValue(object, toCharValue(value));
        }
    }

    /**
     * Interface for property accessors that handle object-typed properties.
     * Provides methods to get and set Object values, with conversions to
     * other types as needed. This is the base interface for non-primitive types.
     */
    protected interface PropertyAccessorObject extends PropertyAccessor {
        @Override
        default byte getByteValue(Object object) {
            return toByteValue(getObject(object));
        }

        @Override
        default char getCharValue(Object object) {
            return toCharValue(getObject(object));
        }

        @Override
        default short getShortValue(Object object) {
            return toShortValue(getObject(object));
        }

        @Override
        default int getIntValue(Object object) {
            return toIntValue(getObject(object));
        }

        @Override
        default long getLongValue(Object object) {
            return toLongValue(getObject(object));
        }

        @Override
        default float getFloatValue(Object object) {
            return toFloatValue(getObject(object));
        }

        @Override
        default double getDoubleValue(Object object) {
            return toDoubleValue(getObject(object));
        }

        @Override
        default boolean getBooleanValue(Object object) {
            return toBooleanValue(getObject(object));
        }

        @Override
        default String getString(Object object) {
            return Cast.toString(getObject(object));
        }

        @Override
        default BigInteger getBigInteger(Object object) {
            return toBigInteger(getObject(object));
        }

        @Override
        default BigDecimal getBigDecimal(Object object) {
            return toBigDecimal(getObject(object));
        }

        @Override
        default void setByteValue(Object object, byte value) {
            setObject(object, value);
        }

        @Override
        default void setShortValue(Object object, short value) {
            setObject(object, value);
        }

        @Override
        default void setCharValue(Object object, char value) {
            setObject(object, value);
        }

        @Override
        default void setIntValue(Object object, int value) {
            setObject(object, value);
        }

        @Override
        default void setLongValue(Object object, long value) {
            setObject(object, value);
        }

        @Override
        default void setFloatValue(Object object, float value) {
            setObject(object, value);
        }

        @Override
        default void setDoubleValue(Object object, double value) {
            setObject(object, value);
        }

        @Override
        default void setBooleanValue(Object object, boolean value) {
            setObject(object, value);
        }

        @Override
        default void setString(Object object, String value) {
            setObject(object, value);
        }

        @Override
        default void setBigInteger(Object object, BigInteger value) {
            setObject(object, value);
        }

        @Override
        default void setBigDecimal(Object object, BigDecimal value) {
            setObject(object, value);
        }
    }

    /**
     * Interface for property accessors that handle String-typed properties.
     * Provides methods to get and set String values, with conversions to
     * other types as needed.
     */
    protected interface PropertyAccessorString extends PropertyAccessorObject {
        @Override
        default byte getByteValue(Object object) {
            return toByteValue(getString(object));
        }

        @Override
        default char getCharValue(Object object) {
            return toCharValue(getString(object));
        }

        @Override
        default short getShortValue(Object object) {
            return toShortValue(getString(object));
        }

        @Override
        default int getIntValue(Object object) {
            return toIntValue(getString(object));
        }

        @Override
        default long getLongValue(Object object) {
            return toLongValue(getString(object));
        }

        @Override
        default float getFloatValue(Object object) {
            return toFloatValue(getString(object));
        }

        @Override
        default double getDoubleValue(Object object) {
            return toDoubleValue(getString(object));
        }

        @Override
        default boolean getBooleanValue(Object object) {
            return toBooleanValue(getString(object));
        }

        @Override
        default Object getObject(Object object) {
            return getString(object);
        }

        @Override
        default void setObject(Object object, Object value) {
            setString(object, Cast.toString(value));
        }

        @Override
        default BigInteger getBigInteger(Object object) {
            return toBigInteger(getString(object));
        }

        @Override
        default void setBigInteger(Object object, BigInteger value) {
            setString(object, Cast.toString(value));
        }

        @Override
        default BigDecimal getBigDecimal(Object object) {
            return toBigDecimal(getString(object));
        }

        @Override
        default void setBigDecimal(Object object, BigDecimal value) {
            setString(object, Cast.toString(value));
        }
    }

    /**
     * Interface for property accessors that handle BigInteger-typed properties.
     * Provides methods to get and set BigInteger values, with conversions to
     * other types as needed.
     */
    protected interface PropertyAccessorBigInteger extends PropertyAccessorObject {
        @Override
        default byte getByteValue(Object object) {
            return toByteValue(getBigInteger(object));
        }

        @Override
        default short getShortValue(Object object) {
            return toShortValue(getBigInteger(object));
        }

        @Override
        default int getIntValue(Object object) {
            return toIntValue(getBigInteger(object));
        }

        @Override
        default long getLongValue(Object object) {
            return toLongValue(getBigInteger(object));
        }

        @Override
        default float getFloatValue(Object object) {
            return toFloatValue(getBigInteger(object));
        }

        @Override
        default double getDoubleValue(Object object) {
            return toDoubleValue(getBigInteger(object));
        }

        @Override
        default boolean getBooleanValue(Object object) {
            return toBooleanValue(getBigInteger(object));
        }

        @Override
        default Object getObject(Object object) {
            return getBigInteger(object);
        }

        @Override
        default void setObject(Object object, Object value) {
            setBigInteger(object, toBigInteger(value));
        }

        @Override
        default String getString(Object object) {
            return Cast.toString(getBigInteger(object));
        }

        @Override
        default void setString(Object object, String value) {
            setBigInteger(object, toBigInteger(value));
        }

        @Override
        default BigDecimal getBigDecimal(Object object) {
            return toBigDecimal(getBigInteger(object));
        }

        @Override
        default void setBigDecimal(Object object, BigDecimal value) {
            setBigInteger(object, toBigInteger(value));
        }
    }

    /**
     * Interface for property accessors that handle BigDecimal-typed properties.
     * Provides methods to get and set BigDecimal values, with conversions to
     * other types as needed.
     */
    protected interface PropertyAccessorBigDecimal extends PropertyAccessorObject {
        @Override
        default byte getByteValue(Object object) {
            return toByteValue(getBigDecimal(object));
        }

        @Override
        default short getShortValue(Object object) {
            return toShortValue(getBigDecimal(object));
        }

        @Override
        default int getIntValue(Object object) {
            return toIntValue(getBigDecimal(object));
        }

        @Override
        default long getLongValue(Object object) {
            return toLongValue(getBigDecimal(object));
        }

        @Override
        default float getFloatValue(Object object) {
            return toFloatValue(getBigDecimal(object));
        }

        @Override
        default double getDoubleValue(Object object) {
            return toDoubleValue(getBigDecimal(object));
        }

        @Override
        default boolean getBooleanValue(Object object) {
            return toBooleanValue(getBigDecimal(object));
        }

        @Override
        default BigInteger getBigInteger(Object object) {
            return toBigInteger(getBigDecimal(object));
        }

        @Override
        default void setBigInteger(Object object, BigInteger value) {
            setBigDecimal(object, toBigDecimal(value));
        }

        @Override
        default Object getObject(Object object) {
            return getBigDecimal(object);
        }

        @Override
        default void setObject(Object object, Object value) {
            setBigDecimal(object, toBigDecimal(value));
        }

        @Override
        default String getString(Object object) {
            return Cast.toString(getBigDecimal(object));
        }

        @Override
        default void setString(Object object, String value) {
            setBigDecimal(object, toBigDecimal(value));
        }
    }

    /**
     * Abstract base class for field-based property accessors that use
     * reflection to access field values. Provides common functionality
     * for accessing fields via reflection, including error handling.
     */
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

    /**
     * Field accessor implementation for boolean-typed properties using reflection.
     * Provides efficient getter and setter operations for boolean fields via reflection.
     */
    static final class FieldAccessorReflectBoolean extends FieldAccessorReflect implements PropertyAccessorBoolean {
        public FieldAccessorReflectBoolean(Field field) {
            super(field);
        }

        @Override
        public boolean getBooleanValue(Object object) {
            try {
                return field.getBoolean(object);
            } catch (IllegalAccessException e) {
                throw errorForGet(e);
            }
        }

        @Override
        public void setBooleanValue(Object object, boolean value) {
            try {
                field.setBoolean(object, value);
            } catch (IllegalAccessException e) {
                throw errorForSet(e);
            }
        }
    }

    /**
     * Field accessor implementation for byte-typed properties using reflection.
     * Provides efficient getter and setter operations for byte fields via reflection.
     */
    static final class FieldAccessorReflectByte extends FieldAccessorReflect implements PropertyAccessorByte {
        public FieldAccessorReflectByte(Field field) {
            super(field);
        }

        @Override
        public byte getByteValue(Object object) {
            try {
                return field.getByte(object);
            } catch (IllegalAccessException e) {
                throw errorForGet(e);
            }
        }

        @Override
        public void setByteValue(Object object, byte value) {
            try {
                field.setByte(object, value);
            } catch (IllegalAccessException e) {
                throw errorForSet(e);
            }
        }
    }

    /**
     * Field accessor implementation for short-typed properties using reflection.
     * Provides efficient getter and setter operations for short fields via reflection.
     */
    static final class FieldAccessorReflectShort extends FieldAccessorReflect implements PropertyAccessorShort {
        public FieldAccessorReflectShort(Field field) {
            super(field);
        }

        @Override
        public short getShortValue(Object object) {
            try {
                return field.getShort(object);
            } catch (IllegalAccessException e) {
                throw errorForGet(e);
            }
        }

        @Override
        public void setShortValue(Object object, short value) {
            try {
                field.setShort(object, value);
            } catch (IllegalAccessException e) {
                throw errorForSet(e);
            }
        }
    }

    /**
     * Field accessor implementation for int-typed properties using reflection.
     * Provides efficient getter and setter operations for int fields via reflection.
     */
    static final class FieldAccessorReflectInt extends FieldAccessorReflect implements PropertyAccessorInt {
        public FieldAccessorReflectInt(Field field) {
            super(field);
        }

        @Override
        public int getIntValue(Object object) {
            try {
                return field.getInt(object);
            } catch (IllegalAccessException e) {
                throw errorForGet(e);
            }
        }

        @Override
        public void setIntValue(Object object, int value) {
            try {
                field.setInt(object, value);
            } catch (IllegalAccessException e) {
                throw errorForSet(e);
            }
        }
    }

    /**
     * Field accessor implementation for long-typed properties using reflection.
     * Provides efficient getter and setter operations for long fields via reflection.
     */
    static final class FieldAccessorReflectLong extends FieldAccessorReflect implements PropertyAccessorLong {
        public FieldAccessorReflectLong(Field field) {
            super(field);
        }

        @Override
        public long getLongValue(Object object) {
            try {
                return field.getLong(object);
            } catch (IllegalAccessException e) {
                throw errorForGet(e);
            }
        }

        @Override
        public void setLongValue(Object object, long value) {
            try {
                field.setLong(object, value);
            } catch (IllegalAccessException e) {
                throw errorForSet(e);
            }
        }
    }

    /**
     * Field accessor implementation for float-typed properties using reflection.
     * Provides efficient getter and setter operations for float fields via reflection.
     */
    static final class FieldAccessorReflectFloat extends FieldAccessorReflect implements PropertyAccessorFloat {
        public FieldAccessorReflectFloat(Field field) {
            super(field);
        }

        @Override
        public float getFloatValue(Object object) {
            try {
                return field.getFloat(object);
            } catch (IllegalAccessException e) {
                throw errorForGet(e);
            }
        }

        @Override
        public void setFloatValue(Object object, float value) {
            try {
                field.setFloat(object, value);
            } catch (IllegalAccessException e) {
                throw errorForSet(e);
            }
        }
    }

    /**
     * Field accessor implementation for double-typed properties using reflection.
     * Provides efficient getter and setter operations for double fields via reflection.
     */
    static final class FieldAccessorReflectDouble extends FieldAccessorReflect implements PropertyAccessorDouble {
        public FieldAccessorReflectDouble(Field field) {
            super(field);
        }

        @Override
        public double getDoubleValue(Object object) {
            try {
                return field.getDouble(object);
            } catch (IllegalAccessException e) {
                throw errorForGet(e);
            }
        }

        @Override
        public void setDoubleValue(Object object, double value) {
            try {
                field.setDouble(object, value);
            } catch (IllegalAccessException e) {
                throw errorForSet(e);
            }
        }
    }

    /**
     * Field accessor implementation for char-typed properties using reflection.
     * Provides efficient getter and setter operations for char fields via reflection.
     */
    static final class FieldAccessorReflectChar extends FieldAccessorReflect implements PropertyAccessorChar {
        public FieldAccessorReflectChar(Field field) {
            super(field);
        }

        @Override
        public char getCharValue(Object object) {
            try {
                return field.getChar(object);
            } catch (IllegalAccessException e) {
                throw errorForGet(e);
            }
        }

        @Override
        public void setCharValue(Object object, char value) {
            try {
                field.setChar(object, value);
            } catch (IllegalAccessException e) {
                throw errorForSet(e);
            }
        }
    }

    /**
     * Field accessor implementation for Object-typed properties using reflection.
     * Provides efficient getter and setter operations for Object fields via reflection.
     */
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

    /**
     * Field accessor implementation for String-typed properties using reflection.
     * Provides efficient getter and setter operations for String fields via reflection.
     */
    static final class FieldAccessorReflectString extends FieldAccessorReflect implements PropertyAccessorString {
        public FieldAccessorReflectString(Field field) {
            super(field);
        }

        @Override
        public String getString(Object object) {
            try {
                return (String) field.get(object);
            } catch (IllegalAccessException e) {
                throw errorForGet(e);
            }
        }

        @Override
        public void setString(Object object, String value) {
            try {
                field.set(object, value);
            } catch (IllegalAccessException e) {
                throw errorForSet(e);
            }
        }
    }

    /**
     * Field accessor implementation for BigInteger-typed properties using reflection.
     * Provides efficient getter and setter operations for BigInteger fields via reflection.
     */
    static final class FieldAccessorReflectBigInteger extends FieldAccessorReflect implements PropertyAccessorBigInteger {
        public FieldAccessorReflectBigInteger(Field field) {
            super(field);
        }

        @Override
        public BigInteger getBigInteger(Object object) {
            try {
                return (BigInteger) field.get(object);
            } catch (IllegalAccessException e) {
                throw errorForGet(e);
            }
        }

        @Override
        public void setBigInteger(Object object, BigInteger value) {
            try {
                field.set(object, value);
            } catch (IllegalAccessException e) {
                throw errorForSet(e);
            }
        }
    }

    /**
     * Field accessor implementation for BigDecimal-typed properties using reflection.
     * Provides efficient getter and setter operations for BigDecimal fields via reflection.
     */
    static final class FieldAccessorReflectBigDecimal extends FieldAccessorReflect implements PropertyAccessorBigDecimal {
        public FieldAccessorReflectBigDecimal(Field field) {
            super(field);
        }

        @Override
        public BigDecimal getBigDecimal(Object object) {
            try {
                return (BigDecimal) field.get(object);
            } catch (IllegalAccessException e) {
                throw errorForGet(e);
            }
        }

        @Override
        public void setBigDecimal(Object object, BigDecimal value) {
            try {
                field.set(object, value);
            } catch (IllegalAccessException e) {
                throw errorForGet(e);
            }
        }
    }

    /**
     * Creates a property accessor for the specified method.
     * This method determines if the method is a getter (no parameters) or setter (one parameter)
     * and creates an appropriate accessor that uses method invocation for property access.
     *
     * @param method the getter or setter method to create an accessor for
     * @return a PropertyAccessor instance for the specified method
     */
    public PropertyAccessor create(Method method) {
        String methodName = method.getName();
        int parameterCount = method.getParameterCount();
        if (parameterCount == 0) {
            return create(BeanUtils.getterName(methodName, null), null, null, method, null);
        } else {
            return create(BeanUtils.setterName(methodName, null), null, null, null, method);
        }
    }

    /**
     * Creates a property accessor using getter and/or setter methods.
     * Validates that the getter has no parameters and the setter has one parameter.
     * The property type is inferred from the getter's return type or setter's parameter type.
     *
     * @param name the property name
     * @param propertyClass the class of the property value
     * @param propertyType the generic type of the property value
     * @param getter the getter method (optional, may be null)
     * @param setter the setter method (optional, may be null)
     * @return a PropertyAccessor instance for the specified getter/setter methods
     * @throws JSONException if the getter or setter method signatures are invalid
     */
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
        if (propertyClass == String.class) {
            return new MethodAccessorString(name, propertyType, propertyClass, getter, setter);
        }
        if (propertyClass == BigInteger.class) {
            return new MethodAccessorBigInteger(name, propertyType, propertyClass, getter, setter);
        }
        if (propertyClass == BigDecimal.class) {
            return new MethodAccessorBigDecimal(name, propertyType, propertyClass, getter, setter);
        }

        return new MethodAccessorObject(name, propertyType, propertyClass, getter, setter);
    }

    /**
     * Creates a property accessor using functional interfaces for byte property access.
     * Uses ToByteFunction for getting the property value and ObjByteConsumer for setting it.
     *
     * @param name the property name
     * @param getterFunc function to get the byte property value from the object
     * @param setterFunc consumer to set the byte property value on the object
     * @param <T> the type of the object containing the property
     * @return a PropertyAccessor instance using functional interfaces
     */
    public <T> PropertyAccessor create(String name, ToByteFunction<T> getterFunc, ObjByteConsumer<T> setterFunc) {
        return new FunctionAccessorByte(name, getterFunc, setterFunc);
    }

    /**
     * Creates a property accessor using functional interfaces for short property access.
     * Uses ToShortFunction for getting the property value and ObjShortConsumer for setting it.
     *
     * @param name the property name
     * @param getterFunc function to get the short property value from the object
     * @param setterFunc consumer to set the short property value on the object
     * @param <T> the type of the object containing the property
     * @return a PropertyAccessor instance using functional interfaces
     */
    public <T> PropertyAccessor create(String name, ToShortFunction<T> getterFunc, ObjShortConsumer<T> setterFunc) {
        return new FunctionAccessorShort(name, getterFunc, setterFunc);
    }

    /**
     * Creates a property accessor using functional interfaces for int property access.
     * Uses ToIntFunction for getting the property value and ObjIntConsumer for setting it.
     *
     * @param name the property name
     * @param getterFunc function to get the int property value from the object
     * @param setterFunc consumer to set the int property value on the object
     * @param <T> the type of the object containing the property
     * @return a PropertyAccessor instance using functional interfaces
     */
    public <T> PropertyAccessor create(String name, ToIntFunction<T> getterFunc, ObjIntConsumer<T> setterFunc) {
        return new FunctionAccessorInt(name, getterFunc, setterFunc);
    }

    /**
     * Creates a property accessor using functional interfaces for long property access.
     * Uses ToLongFunction for getting the property value and ObjLongConsumer for setting it.
     *
     * @param name the property name
     * @param getterFunc function to get the long property value from the object
     * @param setterFunc consumer to set the long property value on the object
     * @param <T> the type of the object containing the property
     * @return a PropertyAccessor instance using functional interfaces
     */
    public <T> PropertyAccessor create(String name, ToLongFunction<T> getterFunc, ObjLongConsumer<T> setterFunc) {
        return new FunctionAccessorLong(name, getterFunc, setterFunc);
    }

    /**
     * Creates a property accessor using functional interfaces for float property access.
     * Uses ToFloatFunction for getting the property value and ObjFloatConsumer for setting it.
     *
     * @param name the property name
     * @param getterFunc function to get the float property value from the object
     * @param setterFunc consumer to set the float property value on the object
     * @param <T> the type of the object containing the property
     * @return a PropertyAccessor instance using functional interfaces
     */
    public <T> PropertyAccessor create(String name, ToFloatFunction<T> getterFunc, ObjFloatConsumer<T> setterFunc) {
        return new FunctionAccessorFloat(name, getterFunc, setterFunc);
    }

    /**
     * Creates a property accessor using functional interfaces for double property access.
     * Uses ToDoubleFunction for getting the property value and ObjDoubleConsumer for setting it.
     *
     * @param name the property name
     * @param getterFunc function to get the double property value from the object
     * @param setterFunc consumer to set the double property value on the object
     * @param <T> the type of the object containing the property
     * @return a PropertyAccessor instance using functional interfaces
     */
    public <T> PropertyAccessor create(String name, ToDoubleFunction<T> getterFunc, ObjDoubleConsumer<T> setterFunc) {
        return new FunctionAccessorDouble<>(name, getterFunc, setterFunc);
    }

    /**
     * Creates a property accessor using functional interfaces for boolean property access.
     * Uses Predicate for getting the property value and ObjBoolConsumer for setting it.
     *
     * @param name the property name
     * @param getterFunc function to get the boolean property value from the object
     * @param setterFunc consumer to set the boolean property value on the object
     * @param <T> the type of the object containing the property
     * @return a PropertyAccessor instance using functional interfaces
     */
    public <T> PropertyAccessor create(String name, Predicate<T> getterFunc, ObjBoolConsumer<T> setterFunc) {
        return new FunctionAccessorBoolean<>(name, getterFunc, setterFunc);
    }

    /**
     * Creates a property accessor using functional interfaces for char property access.
     * Uses ToCharFunction for getting the property value and ObjCharConsumer for setting it.
     *
     * @param name the property name
     * @param getterFunc function to get the char property value from the object
     * @param setterFunc consumer to set the char property value on the object
     * @param <T> the type of the object containing the property
     * @return a PropertyAccessor instance using functional interfaces
     */
    public <T> PropertyAccessor create(String name, ToCharFunction<T> getterFunc, ObjCharConsumer<T> setterFunc) {
        return new FunctionAccessorChar(name, getterFunc, setterFunc);
    }

    /**
     * Creates a property accessor using functional interfaces for generic property access.
     * Uses Function for getting the property value and BiConsumer for setting it.
     * Supports String, BigInteger, and BigDecimal types with specialized implementations.
     *
     * @param name the property name
     * @param propertyClass the class of the property value
     * @param propertyType the generic type of the property value
     * @param getterFunc function to get the property value from the object
     * @param setterFunc consumer to set the property value on the object
     * @param <T> the type of the object containing the property
     * @param <V> the type of the property value
     * @return a PropertyAccessor instance using functional interfaces
     */
    public <T, V> PropertyAccessor create(String name, Class<?> propertyClass, Type propertyType,
                                      Function<T, V> getterFunc,
                                      BiConsumer<T, V> setterFunc) {
        if (propertyClass == String.class) {
            return new FunctionAccessorString<T>(name, (Function<T, String>) getterFunc, (BiConsumer<T, String>) setterFunc);
        }
        if (propertyClass == BigInteger.class) {
            return new FunctionAccessorBigInteger<T>(name, (Function<T, BigInteger>) getterFunc, (BiConsumer<T, BigInteger>) setterFunc);
        }
        if (propertyClass == BigDecimal.class) {
            return new FunctionAccessorBigDecimal<T>(name, (Function<T, BigDecimal>) getterFunc, (BiConsumer<T, BigDecimal>) setterFunc);
        }
        return new FunctionAccessorObject<T, V>(name, propertyType, propertyClass, getterFunc, setterFunc);
    }

    /**
     * Method accessor implementation for char-typed properties using method invocation.
     * Provides efficient getter and setter operations for char properties via method calls.
     */
    static final class MethodAccessorChar extends MethodAccessor implements PropertyAccessorChar {
        public MethodAccessorChar(String name, Type propertyType, Class<?> propertyClass, Method getter, Method setter) {
            super(name, propertyType, propertyClass, getter, setter);
        }

        @Override
        public char getCharValue(Object object) {
            try {
                return (char) getter.invoke(object);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw errorForGet(e);
            }
        }

        @Override
        public void setCharValue(Object object, char value) {
            try {
                setter.invoke(object, value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw errorForSet(e);
            }
        }
    }

    /**
     * Method accessor implementation for byte-typed properties using method invocation.
     * Provides efficient getter and setter operations for byte properties via method calls.
     */
    static final class MethodAccessorByte extends MethodAccessor implements PropertyAccessorByte {
        public MethodAccessorByte(String name, Type propertyType, Class<?> propertyClass, Method getter, Method setter) {
            super(name, propertyType, propertyClass, getter, setter);
        }

        @Override
        public byte getByteValue(Object object) {
            try {
                return (byte) getter.invoke(object);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw errorForGet(e);
            }
        }

        @Override
        public void setByteValue(Object object, byte value) {
            try {
                setter.invoke(object, value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw errorForSet(e);
            }
        }
    }

    /**
     * Method accessor implementation for short-typed properties using method invocation.
     * Provides efficient getter and setter operations for short properties via method calls.
     */
    static final class MethodAccessorShort extends MethodAccessor implements PropertyAccessorShort {
        public MethodAccessorShort(String name, Type propertyType, Class<?> propertyClass, Method getter, Method setter) {
            super(name, propertyType, propertyClass, getter, setter);
        }

        @Override
        public short getShortValue(Object object) {
            try {
                return (short) getter.invoke(object);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw errorForGet(e);
            }
        }

        @Override
        public void setShortValue(Object object, short value) {
            try {
                setter.invoke(object, value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw errorForSet(e);
            }
        }
    }

    /**
     * Method accessor implementation for int-typed properties using method invocation.
     * Provides efficient getter and setter operations for int properties via method calls.
     */
    static final class MethodAccessorInt extends MethodAccessor implements PropertyAccessorInt {
        public MethodAccessorInt(String name, Type propertyType, Class<?> propertyClass, Method getter, Method setter) {
            super(name, propertyType, propertyClass, getter, setter);
        }

        @Override
        public int getIntValue(Object object) {
            try {
                return (int) getter.invoke(object);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw errorForGet(e);
            }
        }

        @Override
        public void setIntValue(Object object, int value) {
            try {
                setter.invoke(object, value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw errorForSet(e);
            }
        }
    }

    /**
     * Method accessor implementation for long-typed properties using method invocation.
     * Provides efficient getter and setter operations for long properties via method calls.
     */
    static final class MethodAccessorLong extends MethodAccessor implements PropertyAccessorLong {
        public MethodAccessorLong(String name, Type propertyType, Class<?> propertyClass, Method getter, Method setter) {
            super(name, propertyType, propertyClass, getter, setter);
        }

        @Override
        public long getLongValue(Object object) {
            try {
                return (long) getter.invoke(object);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw errorForGet(e);
            }
        }

        @Override
        public void setLongValue(Object object, long value) {
            try {
                setter.invoke(object, value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw errorForSet(e);
            }
        }
    }

    /**
     * Method accessor implementation for float-typed properties using method invocation.
     * Provides efficient getter and setter operations for float properties via method calls.
     */
    static final class MethodAccessorFloat extends MethodAccessor implements PropertyAccessorFloat {
        public MethodAccessorFloat(String name, Type propertyType, Class<?> propertyClass, Method getter, Method setter) {
            super(name, propertyType, propertyClass, getter, setter);
        }

        @Override
        public float getFloatValue(Object object) {
            try {
                return (float) getter.invoke(object);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw errorForGet(e);
            }
        }

        @Override
        public void setFloatValue(Object object, float value) {
            try {
                setter.invoke(object, value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw errorForSet(e);
            }
        }
    }

    /**
     * Method accessor implementation for double-typed properties using method invocation.
     * Provides efficient getter and setter operations for double properties via method calls.
     */
    static final class MethodAccessorDouble extends MethodAccessor implements PropertyAccessorDouble {
        public MethodAccessorDouble(String name, Type propertyType, Class<?> propertyClass, Method getter, Method setter) {
            super(name, propertyType, propertyClass, getter, setter);
        }

        @Override
        public double getDoubleValue(Object object) {
            try {
                return (double) getter.invoke(object);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw errorForGet(e);
            }
        }

        @Override
        public void setDoubleValue(Object object, double value) {
            try {
                setter.invoke(object, value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw errorForSet(e);
            }
        }
    }

    /**
     * Method accessor implementation for boolean-typed properties using method invocation.
     * Provides efficient getter and setter operations for boolean properties via method calls.
     */
    static final class MethodAccessorBoolean extends MethodAccessor implements PropertyAccessorBoolean {
        public MethodAccessorBoolean(String name, Type propertyType, Class<?> propertyClass, Method getter, Method setter) {
            super(name, propertyType, propertyClass, getter, setter);
        }

        @Override
        public boolean getBooleanValue(Object object) {
            try {
                return (boolean) getter.invoke(object);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw errorForGet(e);
            }
        }

        @Override
        public void setBooleanValue(Object object, boolean value) {
            try {
                setter.invoke(object, value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw errorForSet(e);
            }
        }
    }

    /**
     * Method accessor implementation for String-typed properties using method invocation.
     * Provides efficient getter and setter operations for String properties via method calls.
     */
    static final class MethodAccessorString extends MethodAccessor implements PropertyAccessorString {
        public MethodAccessorString(String name, Type propertyType, Class<?> propertyClass, Method getter, Method setter) {
            super(name, propertyType, propertyClass, getter, setter);
        }
        @Override
        public String getString(Object object) {
            try {
                return (String) getter.invoke(object);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw errorForGet(e);
            }
        }
        @Override
        public void setString(Object object, String value) {
            try {
                setter.invoke(object, value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw errorForSet(e);
            }
        }
    }

    /**
     * Method accessor implementation for BigInteger-typed properties using method invocation.
     * Provides efficient getter and setter operations for BigInteger properties via method calls.
     */
    static final class MethodAccessorBigInteger extends MethodAccessor implements PropertyAccessorBigInteger {
        public MethodAccessorBigInteger(String name, Type propertyType, Class<?> propertyClass, Method getter, Method setter) {
            super(name, propertyType, propertyClass, getter, setter);
        }
        @Override
        public BigInteger getBigInteger(Object object) {
            try {
                return (BigInteger) getter.invoke(object);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw errorForGet(e);
            }
        }
        @Override
        public void setBigInteger(Object object, BigInteger value) {
            try {
                setter.invoke(object, value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw errorForSet(e);
            }
        }
    }

    /**
     * Method accessor implementation for BigDecimal-typed properties using method invocation.
     * Provides efficient getter and setter operations for BigDecimal properties via method calls.
     */
    static final class MethodAccessorBigDecimal extends MethodAccessor implements PropertyAccessorBigDecimal {
        public MethodAccessorBigDecimal(String name, Type propertyType, Class<?> propertyClass, Method getter, Method setter) {
            super(name, propertyType, propertyClass, getter, setter);
        }
        @Override
        public BigDecimal getBigDecimal(Object object) {
            try {
                return (BigDecimal) getter.invoke(object);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw errorForGet(e);
            }
        }
        @Override
        public void setBigDecimal(Object object, BigDecimal value) {
            try {
                setter.invoke(object, value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw errorForSet(e);
            }
        }
    }

    /**
     * Method accessor implementation for Object-typed properties using method invocation.
     * Provides efficient getter and setter operations for Object properties via method calls.
     */
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

    /**
     * Function accessor implementation for byte-typed properties using functional interfaces.
     * Provides efficient getter and setter operations for byte properties via functional interfaces.
     *
     * @param <T> the type of the object containing the property
     */
    static final class FunctionAccessorByte<T> extends FunctionAccessor<T> implements PropertyAccessorByte {
        private final ToByteFunction<T> getterFunc;
        private final ObjByteConsumer<T> setterFunc;

        public FunctionAccessorByte(String name, ToByteFunction<T> getterFunc, ObjByteConsumer<T> setterFunc) {
            super(name, byte.class, byte.class, getterFunc, setterFunc);
            this.getterFunc = getterFunc;
            this.setterFunc = setterFunc;
        }

        @Override
        public byte getByteValue(Object object) {
            return getterFunc.applyAsByte((T) object);
        }

        @Override
        public void setByteValue(Object object, byte value) {
            setterFunc.accept((T) object, value);
        }
    }

    /**
     * Function accessor implementation for short-typed properties using functional interfaces.
     * Provides efficient getter and setter operations for short properties via functional interfaces.
     *
     * @param <T> the type of the object containing the property
     */
    static final class FunctionAccessorShort<T> extends FunctionAccessor<T> implements PropertyAccessorShort {
        private final ToShortFunction<T> getterFunc;
        private final ObjShortConsumer<T> setterFunc;

        public FunctionAccessorShort(String name, ToShortFunction<T> getterFunc, ObjShortConsumer<T> setterFunc) {
            super(name, short.class, short.class, getterFunc, setterFunc);
            this.getterFunc = getterFunc;
            this.setterFunc = setterFunc;
        }

        @Override
        public short getShortValue(Object object) {
            return getterFunc.applyAsShort((T) object);
        }

        @Override
        public void setShortValue(Object object, short value) {
            setterFunc.accept((T) object, value);
        }
    }

    /**
     * Function accessor implementation for int-typed properties using functional interfaces.
     * Provides efficient getter and setter operations for int properties via functional interfaces.
     *
     * @param <T> the type of the object containing the property
     */
    static final class FunctionAccessorInt<T> extends FunctionAccessor<T> implements PropertyAccessorInt {
        private final ToIntFunction<T> getterFunc;
        private final ObjIntConsumer<T> setterFunc;

        public FunctionAccessorInt(String name, ToIntFunction<T> getterFunc, ObjIntConsumer<T> setterFunc) {
            super(name, int.class, int.class, getterFunc, setterFunc);
            this.getterFunc = getterFunc;
            this.setterFunc = setterFunc;
        }

        @Override
        public int getIntValue(Object object) {
            return getterFunc.applyAsInt((T) object);
        }

        @Override
        public void setIntValue(Object object, int value) {
            setterFunc.accept((T) object, value);
        }
    }

    /**
     * Function accessor implementation for long-typed properties using functional interfaces.
     * Provides efficient getter and setter operations for long properties via functional interfaces.
     *
     * @param <T> the type of the object containing the property
     */
    static final class FunctionAccessorLong<T> extends FunctionAccessor<T> implements PropertyAccessorLong {
        private final ToLongFunction<T> getterFunc;
        private final ObjLongConsumer<T> setterFunc;

        public FunctionAccessorLong(String name, ToLongFunction<T> getterFunc, ObjLongConsumer<T> setterFunc) {
            super(name, long.class, long.class, getterFunc, setterFunc);
            this.getterFunc = getterFunc;
            this.setterFunc = setterFunc;
        }

        @Override
        public long getLongValue(Object object) {
            return getterFunc.applyAsLong((T) object);
        }

        @Override
        public void setLongValue(Object object, long value) {
            setterFunc.accept((T) object, value);
        }
    }

    /**
     * Function accessor implementation for float-typed properties using functional interfaces.
     * Provides efficient getter and setter operations for float properties via functional interfaces.
     *
     * @param <T> the type of the object containing the property
     */
    static final class FunctionAccessorFloat<T> extends FunctionAccessor<T> implements PropertyAccessorFloat {
        private final ToFloatFunction<T> getterFunc;
        private final ObjFloatConsumer<T> setterFunc;

        public FunctionAccessorFloat(String name, ToFloatFunction<T> getterFunc, ObjFloatConsumer<T> setterFunc) {
            super(name, float.class, float.class, getterFunc, setterFunc);
            this.getterFunc = getterFunc;
            this.setterFunc = setterFunc;
        }

        @Override
        public float getFloatValue(Object object) {
            return getterFunc.applyAsFloat((T) object);
        }

        @Override
        public void setFloatValue(Object object, float value) {
            setterFunc.accept((T) object, value);
        }
    }

    /**
     * Function accessor implementation for double-typed properties using functional interfaces.
     * Provides efficient getter and setter operations for double properties via functional interfaces.
     *
     * @param <T> the type of the object containing the property
     */
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
        public double getDoubleValue(Object object) {
            return getterFunc.applyAsDouble((T) object);
        }

        @Override
        public void setDoubleValue(Object object, double value) {
            setterFunc.accept((T) object, value);
        }
    }

    /**
     * Function accessor implementation for boolean-typed properties using functional interfaces.
     * Provides efficient getter and setter operations for boolean properties via functional interfaces.
     *
     * @param <T> the type of the object containing the property
     */
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
        public boolean getBooleanValue(Object object) {
            return getterFunc.test((T) object);
        }

        @Override
        public void setBooleanValue(Object object, boolean value) {
            setterFunc.accept((T) object, value);
        }
    }

    /**
     * Function accessor implementation for Object-typed properties using functional interfaces.
     * Provides efficient getter and setter operations for Object properties via functional interfaces.
     *
     * @param <T> the type of the object containing the property
     * @param <V> the type of the property value
     */
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
            try {
                setterFunc.accept((T) object, (V) value);
            } catch (Exception e) {
                throw errorForSet(e);
            }
        }
    }

    /**
     * Function accessor implementation for String-typed properties using functional interfaces.
     * Provides efficient getter and setter operations for String properties via functional interfaces.
     *
     * @param <T> the type of the object containing the property
     */
    static final class FunctionAccessorString<T> extends FunctionAccessor<T> implements PropertyAccessorString {
        private final Function<T, String> getterFunc;
        private final BiConsumer<T, String> setterFunc;
        public FunctionAccessorString(String name, Function<T, String> getterFunc, BiConsumer<T, String> setterFunc) {
            super(name, String.class, String.class, getterFunc, setterFunc);
            this.getterFunc = getterFunc;
            this.setterFunc = setterFunc;
        }
        @Override
        public String getString(Object object) {
            return getterFunc.apply((T) object);
        }
        @Override
        public void setString(Object object, String value) {
            setterFunc.accept((T) object, value);
        }
    }

    /**
     * Function accessor implementation for BigInteger-typed properties using functional interfaces.
     * Provides efficient getter and setter operations for BigInteger properties via functional interfaces.
     *
     * @param <T> the type of the object containing the property
     */
    static final class FunctionAccessorBigInteger<T> extends FunctionAccessor<T> implements PropertyAccessorBigInteger {
        private final Function<T, BigInteger> getterFunc;
        private final BiConsumer<T, BigInteger> setterFunc;
        public FunctionAccessorBigInteger(String name, Function<T, BigInteger> getterFunc, BiConsumer<T, BigInteger> setterFunc) {
            super(name, BigInteger.class, BigInteger.class, getterFunc, setterFunc);
            this.getterFunc = getterFunc;
            this.setterFunc = setterFunc;
        }
        @Override
        public BigInteger getBigInteger(Object object) {
            return getterFunc.apply((T) object);
        }
        @Override
        public void setBigInteger(Object object, BigInteger value) {
            setterFunc.accept((T) object, value);
        }
    }

    /**
     * Function accessor implementation for BigDecimal-typed properties using functional interfaces.
     * Provides efficient getter and setter operations for BigDecimal properties via functional interfaces.
     *
     * @param <T> the type of the object containing the property
     */
    static final class FunctionAccessorBigDecimal<T> extends FunctionAccessor<T> implements PropertyAccessorBigDecimal {
        private final Function<T, BigDecimal> getterFunc;
        private final BiConsumer<T, BigDecimal> setterFunc;
        public FunctionAccessorBigDecimal(String name, Function<T, BigDecimal> getterFunc, BiConsumer<T, BigDecimal> setterFunc) {
            super(name, BigDecimal.class, BigDecimal.class, getterFunc, setterFunc);
            this.getterFunc = getterFunc;
            this.setterFunc = setterFunc;
        }
        @Override
        public BigDecimal getBigDecimal(Object object) {
            return getterFunc.apply((T) object);
        }
        @Override
        public void setBigDecimal(Object object, BigDecimal value) {
            setterFunc.accept((T) object, value);
        }
    }

    /**
     * Function accessor implementation for char-typed properties using functional interfaces.
     * Provides efficient getter and setter operations for char properties via functional interfaces.
     *
     * @param <T> the type of the object containing the property
     */
    static final class FunctionAccessorChar<T> extends FunctionAccessor<T> implements PropertyAccessorChar {
        private final ToCharFunction<T> getterFunc;
        private final ObjCharConsumer<T> setterFunc;
        public FunctionAccessorChar(String name, ToCharFunction<T> getterFunc, ObjCharConsumer<T> setterFunc) {
            super(name, char.class, char.class, getterFunc, setterFunc);
            this.getterFunc = getterFunc;
            this.setterFunc = setterFunc;
        }

        @Override
        public char getCharValue(Object object) {
            return getterFunc.applyAsChar((T) object);
        }

        @Override
        public void setCharValue(Object object, char value) {
            setterFunc.accept((T) object, value);
        }
    }
}
