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
            return getBoolean(object);
        }

        @Override
        default byte getByte(Object object) {
            return toByte(getBoolean(object));
        }

        @Override
        default char getChar(Object object) {
            return toChar(getBoolean(object));
        }

        @Override
        default short getShort(Object object) {
            return toShort(getBoolean(object));
        }

        @Override
        default int getInt(Object object) {
            return toInt(getBoolean(object));
        }

        @Override
        default long getLong(Object object) {
            return toLong(getBoolean(object));
        }

        @Override
        default float getFloat(Object object) {
            return toFloat(getBoolean(object));
        }

        @Override
        default double getDouble(Object object) {
            return toDouble(getBoolean(object));
        }

        @Override
        default void setObject(Object object, Object value) {
            setBoolean(object, toBoolean(value));
        }

        @Override
        default void setByte(Object object, byte value) {
            setBoolean(object, toBoolean(value));
        }

        @Override
        default void setChar(Object object, char value) {
            setBoolean(object, toBoolean(value));
        }

        @Override
        default void setShort(Object object, short value) {
            setBoolean(object, toBoolean(value));
        }

        @Override
        default void setInt(Object object, int value) {
            setBoolean(object, toBoolean(value));
        }

        @Override
        default void setLong(Object object, long value) {
            setBoolean(object, toBoolean(value));
        }

        @Override
        default void setFloat(Object object, float value) {
            setBoolean(object, toBoolean(value));
        }

        @Override
        default void setDouble(Object object, double value) {
            setBoolean(object, toBoolean(value));
        }

        @Override
        default String getString(Object object) {
            return Cast.toString(getBoolean(object));
        }

        @Override
        default BigInteger getBigInteger(Object object) {
            return toBigInteger(getBoolean(object));
        }

        @Override
        default BigDecimal getBigDecimal(Object object) {
            return toBigDecimal(getBoolean(object));
        }

        @Override
        default void setString(Object object, String value) {
            setBoolean(object, toBoolean(value));
        }

        @Override
        default void setBigInteger(Object object, BigInteger value) {
            setBoolean(object, toBoolean(value));
        }

        @Override
        default void setBigDecimal(Object object, BigDecimal value) {
            setBoolean(object, toBoolean(value));
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

        @Override
        default String getString(Object object) {
            return Cast.toString(getByte(object));
        }

        @Override
        default BigInteger getBigInteger(Object object) {
            return toBigInteger(getByte(object));
        }

        @Override
        default BigDecimal getBigDecimal(Object object) {
            return toBigDecimal(getByte(object));
        }

        @Override
        default void setString(Object object, String value) {
            setByte(object, toByte(value));
        }

        @Override
        default void setBigInteger(Object object, BigInteger value) {
            setByte(object, toByte(value));
        }

        @Override
        default void setBigDecimal(Object object, BigDecimal value) {
            setByte(object, toByte(value));
        }
    }

    /**
     * Interface for property accessors that handle short-typed properties.
     * Provides methods to get and set short values, with conversions to
     * other types as needed.
     */
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

        @Override
        default String getString(Object object) {
            return Cast.toString(getShort(object));
        }

        @Override
        default BigInteger getBigInteger(Object object) {
            return toBigInteger(getShort(object));
        }

        @Override
        default BigDecimal getBigDecimal(Object object) {
            return toBigDecimal(getShort(object));
        }

        @Override
        default void setString(Object object, String value) {
            setShort(object, toShort(value));
        }

        @Override
        default void setBigInteger(Object object, BigInteger value) {
            setShort(object, toShort(value));
        }

        @Override
        default void setBigDecimal(Object object, BigDecimal value) {
            setShort(object, toShort(value));
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

        @Override
        default String getString(Object object) {
            return Cast.toString(getInt(object));
        }

        @Override
        default BigInteger getBigInteger(Object object) {
            return toBigInteger(getInt(object));
        }

        @Override
        default BigDecimal getBigDecimal(Object object) {
            return toBigDecimal(getInt(object));
        }

        @Override
        default void setString(Object object, String value) {
            setInt(object, toInt(value));
        }

        @Override
        default void setBigInteger(Object object, BigInteger value) {
            setInt(object, toInt(value));
        }

        @Override
        default void setBigDecimal(Object object, BigDecimal value) {
            setInt(object, toInt(value));
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

        @Override
        default String getString(Object object) {
            return Cast.toString(getLong(object));
        }

        @Override
        default BigInteger getBigInteger(Object object) {
            return toBigInteger(getLong(object));
        }

        @Override
        default BigDecimal getBigDecimal(Object object) {
            return toBigDecimal(getLong(object));
        }

        @Override
        default void setString(Object object, String value) {
            setLong(object, toLong(value));
        }

        @Override
        default void setBigInteger(Object object, BigInteger value) {
            setLong(object, toLong(value));
        }

        @Override
        default void setBigDecimal(Object object, BigDecimal value) {
            setLong(object, toLong(value));
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

        @Override
        default String getString(Object object) {
            return Cast.toString(getFloat(object));
        }

        @Override
        default BigInteger getBigInteger(Object object) {
            return toBigInteger(getFloat(object));
        }

        @Override
        default BigDecimal getBigDecimal(Object object) {
            return toBigDecimal(getFloat(object));
        }

        @Override
        default void setString(Object object, String value) {
            setFloat(object, toFloat(value));
        }

        @Override
        default void setBigInteger(Object object, BigInteger value) {
            setFloat(object, toFloat(value));
        }

        @Override
        default void setBigDecimal(Object object, BigDecimal value) {
            setFloat(object, toFloat(value));
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

        @Override
        default String getString(Object object) {
            return Cast.toString(getDouble(object));
        }

        @Override
        default BigInteger getBigInteger(Object object) {
            return toBigInteger(getDouble(object));
        }

        @Override
        default BigDecimal getBigDecimal(Object object) {
            return toBigDecimal(getDouble(object));
        }

        @Override
        default void setString(Object object, String value) {
            setDouble(object, toDouble(value));
        }

        @Override
        default void setBigInteger(Object object, BigInteger value) {
            setDouble(object, toDouble(value));
        }

        @Override
        default void setBigDecimal(Object object, BigDecimal value) {
            setDouble(object, toDouble(value));
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

        @Override
        default String getString(Object object) {
            return Cast.toString(getChar(object));
        }

        @Override
        default BigInteger getBigInteger(Object object) {
            return toBigInteger(getChar(object));
        }

        @Override
        default BigDecimal getBigDecimal(Object object) {
            return toBigDecimal(getChar(object));
        }

        @Override
        default void setString(Object object, String value) {
            setChar(object, toChar(value));
        }

        @Override
        default void setBigInteger(Object object, BigInteger value) {
            setChar(object, toChar(value));
        }

        @Override
        default void setBigDecimal(Object object, BigDecimal value) {
            setChar(object, toChar(value));
        }
    }

    /**
     * Interface for property accessors that handle object-typed properties.
     * Provides methods to get and set Object values, with conversions to
     * other types as needed. This is the base interface for non-primitive types.
     */
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
        default byte getByte(Object object) {
            return toByte(getString(object));
        }

        @Override
        default char getChar(Object object) {
            return toChar(getString(object));
        }

        @Override
        default short getShort(Object object) {
            return toShort(getString(object));
        }

        @Override
        default int getInt(Object object) {
            return toInt(getString(object));
        }

        @Override
        default long getLong(Object object) {
            return toLong(getString(object));
        }

        @Override
        default float getFloat(Object object) {
            return toFloat(getString(object));
        }

        @Override
        default double getDouble(Object object) {
            return toDouble(getString(object));
        }

        @Override
        default boolean getBoolean(Object object) {
            return toBoolean(getString(object));
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
        default byte getByte(Object object) {
            return toByte(getBigInteger(object));
        }

        @Override
        default short getShort(Object object) {
            return toShort(getBigInteger(object));
        }

        @Override
        default int getInt(Object object) {
            return toInt(getBigInteger(object));
        }

        @Override
        default long getLong(Object object) {
            return toLong(getBigInteger(object));
        }

        @Override
        default float getFloat(Object object) {
            return toFloat(getBigInteger(object));
        }

        @Override
        default double getDouble(Object object) {
            return toDouble(getBigInteger(object));
        }

        @Override
        default boolean getBoolean(Object object) {
            return toBoolean(getBigInteger(object));
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
        default byte getByte(Object object) {
            return toByte(getBigDecimal(object));
        }

        @Override
        default short getShort(Object object) {
            return toShort(getBigDecimal(object));
        }

        @Override
        default int getInt(Object object) {
            return toInt(getBigDecimal(object));
        }

        @Override
        default long getLong(Object object) {
            return toLong(getBigDecimal(object));
        }

        @Override
        default float getFloat(Object object) {
            return toFloat(getBigDecimal(object));
        }

        @Override
        default double getDouble(Object object) {
            return toDouble(getBigDecimal(object));
        }

        @Override
        default boolean getBoolean(Object object) {
            return toBoolean(getBigDecimal(object));
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
        public boolean getBoolean(Object object) {
            try {
                return field.getBoolean(object);
            } catch (IllegalAccessException e) {
                throw errorForGet(e);
            }
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

    /**
     * Field accessor implementation for byte-typed properties using reflection.
     * Provides efficient getter and setter operations for byte fields via reflection.
     */
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

    /**
     * Field accessor implementation for short-typed properties using reflection.
     * Provides efficient getter and setter operations for short fields via reflection.
     */
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

    /**
     * Field accessor implementation for int-typed properties using reflection.
     * Provides efficient getter and setter operations for int fields via reflection.
     */
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

    /**
     * Field accessor implementation for long-typed properties using reflection.
     * Provides efficient getter and setter operations for long fields via reflection.
     */
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

    /**
     * Field accessor implementation for float-typed properties using reflection.
     * Provides efficient getter and setter operations for float fields via reflection.
     */
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

    /**
     * Field accessor implementation for double-typed properties using reflection.
     * Provides efficient getter and setter operations for double fields via reflection.
     */
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

    /**
     * Field accessor implementation for char-typed properties using reflection.
     * Provides efficient getter and setter operations for char fields via reflection.
     */
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

    /**
     * Method accessor implementation for byte-typed properties using method invocation.
     * Provides efficient getter and setter operations for byte properties via method calls.
     */
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

    /**
     * Method accessor implementation for short-typed properties using method invocation.
     * Provides efficient getter and setter operations for short properties via method calls.
     */
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

    /**
     * Method accessor implementation for int-typed properties using method invocation.
     * Provides efficient getter and setter operations for int properties via method calls.
     */
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

    /**
     * Method accessor implementation for long-typed properties using method invocation.
     * Provides efficient getter and setter operations for long properties via method calls.
     */
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

    /**
     * Method accessor implementation for float-typed properties using method invocation.
     * Provides efficient getter and setter operations for float properties via method calls.
     */
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

    /**
     * Method accessor implementation for double-typed properties using method invocation.
     * Provides efficient getter and setter operations for double properties via method calls.
     */
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

    /**
     * Method accessor implementation for boolean-typed properties using method invocation.
     * Provides efficient getter and setter operations for boolean properties via method calls.
     */
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
        public byte getByte(Object object) {
            return getterFunc.applyAsByte((T) object);
        }

        @Override
        public void setByte(Object object, byte value) {
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
        public short getShort(Object object) {
            return getterFunc.applyAsShort((T) object);
        }

        @Override
        public void setShort(Object object, short value) {
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
        public int getInt(Object object) {
            return getterFunc.applyAsInt((T) object);
        }

        @Override
        public void setInt(Object object, int value) {
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
        public long getLong(Object object) {
            return getterFunc.applyAsLong((T) object);
        }

        @Override
        public void setLong(Object object, long value) {
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
        public float getFloat(Object object) {
            return getterFunc.applyAsFloat((T) object);
        }

        @Override
        public void setFloat(Object object, float value) {
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
        public double getDouble(Object object) {
            return getterFunc.applyAsDouble((T) object);
        }

        @Override
        public void setDouble(Object object, double value) {
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
        public boolean getBoolean(Object object) {
            return getterFunc.test((T) object);
        }

        @Override
        public void setBoolean(Object object, boolean value) {
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
        public char getChar(Object object) {
            return getterFunc.applyAsChar((T) object);
        }

        @Override
        public void setChar(Object object, char value) {
            setterFunc.accept((T) object, value);
        }
    }
}
