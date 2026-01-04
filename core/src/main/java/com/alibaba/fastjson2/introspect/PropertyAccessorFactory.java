package com.alibaba.fastjson2.introspect;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.function.*;
import com.alibaba.fastjson2.internal.Cast;
import com.alibaba.fastjson2.util.BeanUtils;

import java.lang.reflect.*;
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
     * Creates a Supplier that can instantiate objects using the given constructor.
     * This method provides a way to create object instances via constructor reflection
     * in a functional programming style.
     *
     * @param constructor the constructor to use for object instantiation
     * @return a Supplier that creates new instances using the provided constructor
     */
    public Supplier createSupplier(Constructor constructor) {
        return new ConstructorSupplier(constructor);
    }

    /**
     * Creates a Function that can instantiate objects using the given constructor.
     * @param constructor the constructor to use for object instantiation
     * @return a Function that creates new instances using the provided constructor
     */
    public Function createFunction(Constructor constructor) {
        return new ConstructorFunction(constructor);
    }

    /**
     * Creates a Function that can instantiate objects using the given constructor.
     * @param constructor the constructor to use for object instantiation
     * @return a Function that creates new instances using the provided constructor
     */
    public IntFunction createIntFunction(Constructor constructor) {
        return new ConstructorIntFunction(constructor);
    }

    /**
     * Creates a Function that can instantiate objects using the given constructor.
     * @param constructor the constructor to use for object instantiation
     * @return a Function that creates new instances using the provided constructor
     */
    public LongFunction createLongFunction(Constructor constructor) {
        return new ConstructorLongFunction(constructor);
    }

    /**
     * Creates a Function that can instantiate objects using the given constructor.
     * @param constructor the constructor to use for object instantiation
     * @return a Function that creates new instances using the provided constructor
     */
    public DoubleFunction createDoubleFunction(Constructor constructor) {
        return new ConstructorDoubleFunction(constructor);
    }

    /**
     * Base class for Constructor-based Function implementations.
     * Handles constructor accessibility and instantiation errors.
     */
    abstract static class ConstructorFunctionBase {
        protected final Constructor constructor;

        public ConstructorFunctionBase(Constructor constructor) {
            this.constructor = constructor;
            setAccessible();
        }

        /**
         * Makes the constructor accessible, handling any security exceptions
         * that might occur during the process.
         */
        protected void setAccessible() {
            try {
                constructor.setAccessible(true);
            } catch (Exception e) {
                throw new JSONException(e.getMessage(), e);
            }
        }

        /**
         * Creates a specific JSON exception for constructor accessibility errors.
         *
         * @param e the original exception that occurred
         * @return a JSONException with detailed error information
         */
        protected JSONException errorOnSetAccessible(Exception e) {
            return new JSONException(constructor.toString().concat(" setAccessible error"), e);
        }

        /**
         * Creates a specific JSON exception for constructor instantiation errors.
         *
         * @param e the original exception that occurred
         * @return a JSONException with detailed error information
         */
        protected JSONException errorOnNewInstance(Exception e) {
            return new JSONException(constructor.toString().concat(" newInstance error"), e);
        }
    }

    /**
     * A Supplier implementation that uses reflection to create new instances
     * of a class via its constructor. This class handles constructor accessibility
     * and instantiation errors appropriately.
     */
    static final class ConstructorSupplier extends ConstructorFunctionBase implements Supplier {
        public ConstructorSupplier(Constructor constructor) {
            super(constructor);
        }

        /**
         * Creates a new instance of the class using the constructor.
         *
         * @return a new instance of the class
         * @throws JSONException if instantiation fails
         */
        @Override
        public Object get() {
            try {
                return constructor.newInstance();
            } catch (Exception e) {
                throw errorOnNewInstance(e);
            }
        }
    }

    static final class ConstructorFunction extends ConstructorFunctionBase implements Function {
        /**
         * Creates a ConstructorSupplier for the given constructor.
         * Automatically makes the constructor accessible.
         *
         * @param constructor the constructor to use for instantiation
         */
        public ConstructorFunction(Constructor constructor) {
            super(constructor);
        }

        @Override
        public Object apply(Object arg) {
            try {
                return constructor.newInstance(arg);
            } catch (Exception e) {
                throw errorOnNewInstance(e);
            }
        }
    }

    /**
     * A Supplier implementation that uses reflection to create new instances
     * of a class via its constructor. This class handles constructor accessibility
     * and instantiation errors appropriately.
     */
    static final class ConstructorIntFunction extends ConstructorFunctionBase implements IntFunction {
        /**
         * Creates a ConstructorSupplier for the given constructor.
         * Automatically makes the constructor accessible.
         *
         * @param constructor the constructor to use for instantiation
         */
        public ConstructorIntFunction(Constructor constructor) {
            super(constructor);
        }

        @Override
        public Object apply(int arg) {
            try {
                return constructor.newInstance(arg);
            } catch (Exception e) {
                throw errorOnNewInstance(e);
            }
        }
    }

    /**
     * A Supplier implementation that uses reflection to create new instances
     * of a class via its constructor. This class handles constructor accessibility
     * and instantiation errors appropriately.
     */
    static final class ConstructorLongFunction extends ConstructorFunctionBase implements LongFunction {
        /**
         * Creates a ConstructorSupplier for the given constructor.
         * Automatically makes the constructor accessible.
         *
         * @param constructor the constructor to use for instantiation
         */
        public ConstructorLongFunction(Constructor constructor) {
            super(constructor);
        }

        @Override
        public Object apply(long arg) {
            try {
                return constructor.newInstance(arg);
            } catch (Exception e) {
                throw errorOnNewInstance(e);
            }
        }
    }

    /**
     * A Supplier implementation that uses reflection to create new instances
     * of a class via its constructor. This class handles constructor accessibility
     * and instantiation errors appropriately.
     */
    static final class ConstructorDoubleFunction extends ConstructorFunctionBase implements DoubleFunction {
        public ConstructorDoubleFunction(Constructor constructor) {
            super(constructor);
        }

        @Override
        public Object apply(double arg) {
            try {
                return constructor.newInstance(arg);
            } catch (Exception e) {
                throw errorOnNewInstance(e);
            }
        }
    }

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
            return new FieldAccessorReflectByteValue(field);
        }
        if (field.getType() == short.class) {
            return new FieldAccessorReflectShortValue(field);
        }
        if (field.getType() == int.class) {
            return new FieldAccessorReflectIntValue(field);
        }
        if (field.getType() == long.class) {
            return new FieldAccessorReflectLongValue(field);
        }
        if (field.getType() == float.class) {
            return new FieldAccessorReflectFloatValue(field);
        }
        if (field.getType() == double.class) {
            return new FieldAccessorReflectDoubleValue(field);
        }
        if (field.getType() == boolean.class) {
            return new FieldAccessorReflectBooleanValue(field);
        }
        if (field.getType() == char.class) {
            return new FieldAccessorReflectCharValue(field);
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
        if (field.getType() == Boolean.class) {
            return new FieldAccessorReflectBoolean(field);
        }
        if (field.getType() == Byte.class) {
            return new FieldAccessorReflectByte(field);
        }
        if (field.getType() == Character.class) {
            return new FieldAccessorReflectCharacter(field);
        }
        if (field.getType() == Short.class) {
            return new FieldAccessorReflectShort(field);
        }
        if (field.getType() == Integer.class) {
            return new FieldAccessorReflectInteger(field);
        }
        if (field.getType() == Long.class) {
            return new FieldAccessorReflectLong(field);
        }
        if (field.getType() == Float.class) {
            return new FieldAccessorReflectFloat(field);
        }
        if (field.getType() == Double.class) {
            return new FieldAccessorReflectDouble(field);
        }
        if (field.getType() == Number.class) {
            return new FieldAccessorReflectNumber(field);
        }
        return new FieldAccessorReflectObject(field);
    }

    /**
     * Interface for property accessors that handle boolean-typed properties.
     * Provides methods to get and set boolean values, with conversions to
     * other types as needed.
     */
    protected interface PropertyAccessorBooleanValue
            extends PropertyAccessor {
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
    }

    /**
     * Interface for property accessors that handle byte-typed properties.
     * Provides methods to get and set byte values, with conversions to
     * other types as needed.
     */
    protected interface PropertyAccessorByteValue
            extends PropertyAccessor {
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
    }

    /**
     * Interface for property accessors that handle short-typed properties.
     * Provides methods to get and set short values, with conversions to
     * other types as needed.
     */
    protected interface PropertyAccessorShortValue
            extends PropertyAccessor {
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
    }

    /**
     * Interface for property accessors that handle int-typed properties.
     * Provides methods to get and set int values, with conversions to
     * other types as needed.
     */
    protected interface PropertyAccessorIntValue
            extends PropertyAccessor {
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
    }

    /**
     * Interface for property accessors that handle long-typed properties.
     * Provides methods to get and set long values, with conversions to
     * other types as needed.
     */
    protected interface PropertyAccessorLongValue
            extends PropertyAccessor {
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
    }

    /**
     * Interface for property accessors that handle float-typed properties.
     * Provides methods to get and set float values, with conversions to
     * other types as needed.
     */
    interface PropertyAccessorFloatValue
            extends PropertyAccessor {
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
    }

    /**
     * Interface for property accessors that handle double-typed properties.
     * Provides methods to get and set double values, with conversions to
     * other types as needed.
     */
    protected interface PropertyAccessorDoubleValue
            extends PropertyAccessor {
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
    }

    /**
     * Interface for property accessors that handle char-typed properties.
     * Provides methods to get and set char values, with conversions to
     * other types as needed.
     */
    protected interface PropertyAccessorCharValue
            extends PropertyAccessor {
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

        /**
         * Gets the property value as a String.
         *
         * @param object the object to get the property from
         * @return the property value as String
         */
        String getString(Object object);

        /**
         * Sets the property value from a String.
         *
         * @param object the object to set the property on
         * @param value the String value to set
         */
        void setString(Object object, String value);
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

        /**
         * Gets the property value as a BigInteger.
         *
         * @param object the object to get the property from
         * @return the property value as BigInteger
         */
        BigInteger getBigInteger(Object object);

        /**
         * Sets the property value from a BigInteger.
         *
         * @param object the object to set the property on
         * @param value the BigInteger value to set
         */
        void setBigInteger(Object object, BigInteger value);
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
        default Object getObject(Object object) {
            return getBigDecimal(object);
        }

        @Override
        default void setObject(Object object, Object value) {
            setBigDecimal(object, toBigDecimal(value));
        }

        /**
         * Gets the property value as a BigDecimal.
         *
         * @param object the object to get the property from
         * @return the property value as BigDecimal
         */
        BigDecimal getBigDecimal(Object object);

        /**
         * Sets the property value from a BigDecimal.
         *
         * @param object the object to set the property on
         * @param value the BigDecimal value to set
         */
        void setBigDecimal(Object object, BigDecimal value);
    }

    protected interface PropertyAccessorBoolean extends PropertyAccessorObject {
        @Override
        default byte getByteValue(Object object) {
            return toByteValue(getBoolean(object));
        }

        @Override
        default char getCharValue(Object object) {
            return toCharValue(getBoolean(object));
        }

        @Override
        default short getShortValue(Object object) {
            return toShortValue(getBoolean(object));
        }

        @Override
        default int getIntValue(Object object) {
            return toIntValue(getBoolean(object));
        }

        @Override
        default long getLongValue(Object object) {
            return toLongValue(getBoolean(object));
        }

        @Override
        default float getFloatValue(Object object) {
            return toFloatValue(getBoolean(object));
        }

        @Override
        default double getDoubleValue(Object object) {
            return toDoubleValue(getBoolean(object));
        }

        @Override
        default boolean getBooleanValue(Object object) {
            return toBooleanValue(getBoolean(object));
        }

        @Override
        default Object getObject(Object object) {
            return getBoolean(object);
        }

        @Override
        default void setObject(Object object, Object value) {
            setBoolean(object, Cast.toBoolean(value));
        }

        void setBoolean(Object object, Boolean value);
        Boolean getBoolean(Object object);
    }

    protected interface PropertyAccessorByte extends PropertyAccessorObject {
        @Override
        default byte getByteValue(Object object) {
            return toByteValue(getByte(object));
        }

        @Override
        default char getCharValue(Object object) {
            return toCharValue(getByte(object));
        }

        @Override
        default short getShortValue(Object object) {
            return toShortValue(getByte(object));
        }

        @Override
        default int getIntValue(Object object) {
            return toIntValue(getByte(object));
        }

        @Override
        default long getLongValue(Object object) {
            return toLongValue(getByte(object));
        }

        @Override
        default float getFloatValue(Object object) {
            return toFloatValue(getByte(object));
        }

        @Override
        default double getDoubleValue(Object object) {
            return toDoubleValue(getByte(object));
        }

        @Override
        default boolean getBooleanValue(Object object) {
            return toBooleanValue(getByte(object));
        }

        @Override
        default Object getObject(Object object) {
            return getByte(object);
        }

        @Override
        default void setByteValue(Object object, byte value) {
            setByte(object, value);
        }

        @Override
        default void setCharValue(Object object, char value) {
            setByte(object, toByte(value));
        }

        @Override
        default void setShortValue(Object object, short value) {
            setByte(object, toByte(value));
        }

        @Override
        default void setIntValue(Object object, int value) {
            setByte(object, toByte(value));
        }

        @Override
        default void setLongValue(Object object, long value) {
            setByte(object, toByte(value));
        }

        @Override
        default void setFloatValue(Object object, float value) {
            setByte(object, toByte(value));
        }

        @Override
        default void setDoubleValue(Object object, double value) {
            setByte(object, toByte(value));
        }

        @Override
        default void setBooleanValue(Object object, boolean value) {
            setByte(object, toByte(value));
        }

        @Override
        default void setObject(Object object, Object value) {
            setByte(object, Cast.toByte(value));
        }

        void setByte(Object object, Byte value);
        Byte getByte(Object object);
    }

    protected interface PropertyAccessorCharacter extends PropertyAccessorObject {
        @Override
        default byte getByteValue(Object object) {
            return toByteValue(getCharacter(object));
        }

        @Override
        default char getCharValue(Object object) {
            return toCharValue(getCharacter(object));
        }

        @Override
        default short getShortValue(Object object) {
            return toShortValue(getCharacter(object));
        }

        @Override
        default int getIntValue(Object object) {
            return toIntValue(getCharacter(object));
        }

        @Override
        default long getLongValue(Object object) {
            return toLongValue(getCharacter(object));
        }

        @Override
        default float getFloatValue(Object object) {
            return toFloatValue(getCharacter(object));
        }

        @Override
        default double getDoubleValue(Object object) {
            return toDoubleValue(getCharacter(object));
        }

        @Override
        default boolean getBooleanValue(Object object) {
            return toBooleanValue(getCharacter(object));
        }

        default void setByteValue(Object object, byte value) {
            setCharacter(object, toCharacter(value));
        }

        default void setCharValue(Object object, char value) {
            setCharacter(object, value);
        }

        default void setShortValue(Object object, short value) {
            setCharacter(object, toCharacter(value));
        }

        default void setIntValue(Object object, int value) {
            setCharacter(object, toCharacter(value));
        }

        default void setLongValue(Object object, long value) {
            setCharacter(object, toCharacter(value));
        }

        default void setFloatValue(Object object, float value) {
            setCharacter(object, toCharacter(value));
        }

        default void setDoubleValue(Object object, double value) {
            setCharacter(object, toCharacter(value));
        }

        default void setBooleanValue(Object object, boolean value) {
            setCharacter(object, toCharacter(value));
        }

        default void setString(Object object, String value) {
            setCharacter(object, Cast.toCharacter(value));
        }

        @Override
        default Object getObject(Object object) {
            return getCharacter(object);
        }

        @Override
        default void setObject(Object object, Object value) {
            setCharacter(object, Cast.toCharacter(value));
        }

        void setCharacter(Object object, Character value);
        Character getCharacter(Object object);
    }

    protected interface PropertyAccessorShort extends PropertyAccessorObject {
        @Override
        default byte getByteValue(Object object) {
            return toByteValue(getShort(object));
        }

        @Override
        default char getCharValue(Object object) {
            return toCharValue(getShort(object));
        }

        @Override
        default short getShortValue(Object object) {
            return toShortValue(getShort(object));
        }

        @Override
        default int getIntValue(Object object) {
            return toIntValue(getShort(object));
        }

        @Override
        default long getLongValue(Object object) {
            return toLongValue(getShort(object));
        }

        @Override
        default float getFloatValue(Object object) {
            return toFloatValue(getShort(object));
        }

        @Override
        default double getDoubleValue(Object object) {
            return toDoubleValue(getShort(object));
        }

        @Override
        default boolean getBooleanValue(Object object) {
            return toBooleanValue(getShort(object));
        }

        @Override
        default Object getObject(Object object) {
            return getShort(object);
        }

        @Override
        default void setObject(Object object, Object value) {
            setShort(object, Cast.toShort(value));
        }

        @Override
        default void setByteValue(Object object, byte value) {
            setShort(object, toShort(value));
        }

        @Override
        default void setCharValue(Object object, char value) {
            setShort(object, toShort(value));
        }

        @Override
        default void setShortValue(Object object, short value) {
            setShort(object, value);
        }

        @Override
        default void setIntValue(Object object, int value) {
            setShort(object, toShort(value));
        }

        @Override
        default void setLongValue(Object object, long value) {
            setShort(object, toShort(value));
        }

        @Override
        default void setFloatValue(Object object, float value) {
            setShort(object, toShort(value));
        }

        @Override
        default void setDoubleValue(Object object, double value) {
            setShort(object, toShort(value));
        }

        @Override
        default void setBooleanValue(Object object, boolean value) {
            setShort(object, toShort(value));
        }

        void setShort(Object object, Short value);
        Short getShort(Object object);
    }

    protected interface PropertyAccessorInteger extends PropertyAccessorObject {
        @Override
        default byte getByteValue(Object object) {
            return toByteValue(getInteger(object));
        }

        @Override
        default char getCharValue(Object object) {
            return toCharValue(getInteger(object));
        }

        @Override
        default short getShortValue(Object object) {
            return toShortValue(getInteger(object));
        }

        @Override
        default int getIntValue(Object object) {
            return toIntValue(getInteger(object));
        }

        @Override
        default long getLongValue(Object object) {
            return toLongValue(getInteger(object));
        }

        @Override
        default float getFloatValue(Object object) {
            return toFloatValue(getInteger(object));
        }

        @Override
        default double getDoubleValue(Object object) {
            return toDoubleValue(getInteger(object));
        }

        @Override
        default boolean getBooleanValue(Object object) {
            return toBooleanValue(getInteger(object));
        }

        @Override
        default Object getObject(Object object) {
            return getInteger(object);
        }

        @Override
        default void setObject(Object object, Object value) {
            setInteger(object, Cast.toInteger(value));
        }

        @Override
        default void setByteValue(Object object, byte value) {
            setInteger(object, toInteger(value));
        }

        @Override
        default void setCharValue(Object object, char value) {
            setInteger(object, toInteger(value));
        }

        @Override
        default void setShortValue(Object object, short value) {
            setInteger(object, toInteger(value));
        }

        @Override
        default void setIntValue(Object object, int value) {
            setInteger(object, value);
        }

        @Override
        default void setLongValue(Object object, long value) {
            setInteger(object, toInteger(value));
        }

        @Override
        default void setFloatValue(Object object, float value) {
            setInteger(object, toInteger(value));
        }

        @Override
        default void setDoubleValue(Object object, double value) {
            setInteger(object, toInteger(value));
        }

        void setInteger(Object object, Integer value);
        Integer getInteger(Object object);
    }

    protected interface PropertyAccessorLong extends PropertyAccessorObject {
        @Override
        default byte getByteValue(Object object) {
            return toByteValue(getLong(object));
        }

        @Override
        default char getCharValue(Object object) {
            return toCharValue(getLong(object));
        }

        @Override
        default short getShortValue(Object object) {
            return toShortValue(getLong(object));
        }

        @Override
        default int getIntValue(Object object) {
            return toIntValue(getLong(object));
        }

        @Override
        default long getLongValue(Object object) {
            return toLongValue(getLong(object));
        }

        @Override
        default float getFloatValue(Object object) {
            return toFloatValue(getLong(object));
        }

        @Override
        default double getDoubleValue(Object object) {
            return toDoubleValue(getLong(object));
        }

        @Override
        default boolean getBooleanValue(Object object) {
            return toBooleanValue(getLong(object));
        }

        @Override
        default Object getObject(Object object) {
            return getLong(object);
        }

        @Override
        default void setObject(Object object, Object value) {
            setLong(object, Cast.toLong(value));
        }

        @Override
        default void setByteValue(Object object, byte value) {
            setLong(object, toLong(value));
        }

        @Override
        default void setCharValue(Object object, char value) {
            setLong(object, toLong(value));
        }

        @Override
        default void setShortValue(Object object, short value) {
            setLong(object, toLong(value));
        }

        @Override
        default void setIntValue(Object object, int value) {
            setLong(object, toLong(value));
        }

        @Override
        default void setLongValue(Object object, long value) {
            setLong(object, value);
        }

        @Override
        default void setFloatValue(Object object, float value) {
            setLong(object, toLong(value));
        }

        @Override
        default void setDoubleValue(Object object, double value) {
            setLong(object, toLong(value));
        }

        @Override
        default void setBooleanValue(Object object, boolean value) {
            setLong(object, toLong(value));
        }

        void setLong(Object object, Long value);
        Long getLong(Object object);
    }

    protected interface PropertyAccessorFloat extends PropertyAccessorObject {
        @Override
        default byte getByteValue(Object object) {
            return toByteValue(getFloat(object));
        }

        @Override
        default char getCharValue(Object object) {
            return toCharValue(getFloat(object));
        }

        @Override
        default short getShortValue(Object object) {
            return toShortValue(getFloat(object));
        }

        @Override
        default int getIntValue(Object object) {
            return toIntValue(getFloat(object));
        }

        @Override
        default long getLongValue(Object object) {
            return toLongValue(getFloat(object));
        }

        @Override
        default float getFloatValue(Object object) {
            return toFloatValue(getFloat(object));
        }

        @Override
        default double getDoubleValue(Object object) {
            return toDoubleValue(getFloat(object));
        }

        @Override
        default boolean getBooleanValue(Object object) {
            return toBooleanValue(getFloat(object));
        }

        @Override
        default Object getObject(Object object) {
            return getFloat(object);
        }

        @Override
        default void setObject(Object object, Object value) {
            setFloat(object, Cast.toFloat(value));
        }

        @Override
        default void setByteValue(Object object, byte value) {
            setFloat(object, toFloat(value));
        }

        @Override
        default void setCharValue(Object object, char value) {
            setFloat(object, toFloat(value));
        }

        @Override
        default void setShortValue(Object object, short value) {
            setFloat(object, toFloat(value));
        }

        @Override
        default void setIntValue(Object object, int value) {
            setFloat(object, toFloat(value));
        }

        @Override
        default void setLongValue(Object object, long value) {
            setFloat(object, toFloat(value));
        }

        @Override
        default void setFloatValue(Object object, float value) {
            setFloat(object, value);
        }

        @Override
        default void setDoubleValue(Object object, double value) {
            setFloat(object, toFloat(value));
        }

        @Override
        default void setBooleanValue(Object object, boolean value) {
            setFloat(object, toFloat(value));
        }

        void setFloat(Object object, Float value);
        Float getFloat(Object object);
    }

    protected interface PropertyAccessorDouble extends PropertyAccessorObject {
        @Override
        default byte getByteValue(Object object) {
            return toByteValue(getDouble(object));
        }

        @Override
        default char getCharValue(Object object) {
            return toCharValue(getDouble(object));
        }

        @Override
        default short getShortValue(Object object) {
            return toShortValue(getDouble(object));
        }

        @Override
        default int getIntValue(Object object) {
            return toIntValue(getDouble(object));
        }

        @Override
        default long getLongValue(Object object) {
            return toLongValue(getDouble(object));
        }

        @Override
        default float getFloatValue(Object object) {
            return toFloatValue(getDouble(object));
        }

        @Override
        default double getDoubleValue(Object object) {
            return toDoubleValue(getDouble(object));
        }

        @Override
        default boolean getBooleanValue(Object object) {
            return toBooleanValue(getDouble(object));
        }

        @Override
        default Object getObject(Object object) {
            return getDouble(object);
        }

        @Override
        default void setObject(Object object, Object value) {
            setDouble(object, Cast.toDouble(value));
        }

        @Override
        default void setByteValue(Object object, byte value) {
            setDouble(object, toDouble(value));
        }

        @Override
        default void setCharValue(Object object, char value) {
            setDouble(object, toDouble(value));
        }

        @Override
        default void setShortValue(Object object, short value) {
            setDouble(object, toDouble(value));
        }

        @Override
        default void setIntValue(Object object, int value) {
            setDouble(object, toDouble(value));
        }

        @Override
        default void setLongValue(Object object, long value) {
            setDouble(object, toDouble(value));
        }

        @Override
        default void setFloatValue(Object object, float value) {
            setDouble(object, toDouble(value));
        }

        @Override
        default void setDoubleValue(Object object, double value) {
            setDouble(object, value);
        }

        @Override
        default void setBooleanValue(Object object, boolean value) {
            setDouble(object, toDouble(value));
        }

        /**
         * Sets the property value from a double.
         *
         * @param object the object to set the property on
         * @param value the double value to set
         */
        void setDouble(Object object, Double value);

        /**
         * Gets the property value as a double.
         *
         * @param object the object to get the property from
         * @return the property value as a double
         */
        Double getDouble(Object object);
    }

    protected interface PropertyAccessorNumber extends PropertyAccessorObject {
        @Override
        default byte getByteValue(Object object) {
            return toByteValue(getNumber(object));
        }

        @Override
        default char getCharValue(Object object) {
            return toCharValue(getNumber(object));
        }

        @Override
        default short getShortValue(Object object) {
            return toShortValue(getNumber(object));
        }

        @Override
        default int getIntValue(Object object) {
            return toIntValue(getNumber(object));
        }

        @Override
        default long getLongValue(Object object) {
            return toLongValue(getNumber(object));
        }

        @Override
        default float getFloatValue(Object object) {
            return toFloatValue(getNumber(object));
        }

        @Override
        default double getDoubleValue(Object object) {
            return toDoubleValue(getNumber(object));
        }

        @Override
        default boolean getBooleanValue(Object object) {
            return toBooleanValue(getNumber(object));
        }

        @Override
        default Object getObject(Object object) {
            return getNumber(object);
        }

        @Override
        default void setObject(Object object, Object value) {
            setNumber(object, toNumber(value));
        }

        @Override
        default void setByteValue(Object object, byte value) {
            setNumber(object, toNumber(value));
        }

        @Override
        default void setCharValue(Object object, char value) {
            setNumber(object, toNumber(value));
        }

        @Override
        default void setShortValue(Object object, short value) {
            setNumber(object, toNumber(value));
        }

        @Override
        default void setIntValue(Object object, int value) {
            setNumber(object, toNumber(value));
        }

        @Override
        default void setLongValue(Object object, long value) {
            setNumber(object, toNumber(value));
        }

        @Override
        default void setFloatValue(Object object, float value) {
            setNumber(object, toNumber(value));
        }

        @Override
        default void setDoubleValue(Object object, double value) {
            setNumber(object, value);
        }

        @Override
        default void setBooleanValue(Object object, boolean value) {
            setNumber(object, toNumber(value));
        }

        void setNumber(Object object, Number value);

        Number getNumber(Object object);
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
    }

    /**
     * Field accessor implementation for boolean-typed properties using reflection.
     * Provides efficient getter and setter operations for boolean fields via reflection.
     */
    static final class FieldAccessorReflectBooleanValue
            extends FieldAccessorReflect implements PropertyAccessorBooleanValue
    {
        public FieldAccessorReflectBooleanValue(Field field) {
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
    static final class FieldAccessorReflectByteValue
            extends FieldAccessorReflect implements PropertyAccessorByteValue
    {
        public FieldAccessorReflectByteValue(Field field) {
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
    static final class FieldAccessorReflectShortValue
            extends FieldAccessorReflect implements PropertyAccessorShortValue
    {
        public FieldAccessorReflectShortValue(Field field) {
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
    static final class FieldAccessorReflectIntValue
            extends FieldAccessorReflect implements PropertyAccessorIntValue
    {
        public FieldAccessorReflectIntValue(Field field) {
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
    static final class FieldAccessorReflectLongValue
            extends FieldAccessorReflect implements PropertyAccessorLongValue
    {
        public FieldAccessorReflectLongValue(Field field) {
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
    static final class FieldAccessorReflectFloatValue
            extends FieldAccessorReflect implements PropertyAccessorFloatValue
    {
        public FieldAccessorReflectFloatValue(Field field) {
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
    static final class FieldAccessorReflectDoubleValue
            extends FieldAccessorReflect implements PropertyAccessorDoubleValue
    {
        public FieldAccessorReflectDoubleValue(Field field) {
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
    static final class FieldAccessorReflectCharValue
            extends FieldAccessorReflect implements PropertyAccessorCharValue
    {
        public FieldAccessorReflectCharValue(Field field) {
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

    static final class FieldAccessorReflectBoolean
            extends FieldAccessorReflect
            implements PropertyAccessorBoolean {
        public FieldAccessorReflectBoolean(Field field) {
            super(field);
        }

        @Override
        public void setBoolean(Object object, Boolean value) {
            try {
                field.set(object, value);
            } catch (IllegalAccessException e) {
                throw errorForSet(e);
            }
        }

        @Override
        public Boolean getBoolean(Object object) {
            try {
                return (Boolean) field.get(object);
            } catch (IllegalAccessException e) {
                throw errorForGet(e);
            }
        }
    }

    static final class FieldAccessorReflectByte
            extends FieldAccessorReflect
            implements PropertyAccessorByte {
        public FieldAccessorReflectByte(Field field) {
            super(field);
        }

        @Override
        public void setByte(Object object, Byte value) {
            try {
                field.set(object, value);
            } catch (IllegalAccessException e) {
                throw errorForSet(e);
            }
        }

        @Override
        public Byte getByte(Object object) {
            try {
                return (Byte) field.get(object);
            } catch (IllegalAccessException e) {
                throw errorForGet(e);
            }
        }
    }

    static final class FieldAccessorReflectCharacter
            extends FieldAccessorReflect
            implements PropertyAccessorCharacter {
        public FieldAccessorReflectCharacter(Field field) {
            super(field);
        }

        @Override
        public void setCharacter(Object object, Character value) {
            try {
                field.set(object, value);
            } catch (IllegalAccessException e) {
                throw errorForSet(e);
            }
        }

        @Override
        public Character getCharacter(Object object) {
            try {
                return (Character) field.get(object);
            } catch (IllegalAccessException e) {
                throw errorForGet(e);
            }
        }
    }

    static final class FieldAccessorReflectShort
            extends FieldAccessorReflect
            implements PropertyAccessorShort {
        public FieldAccessorReflectShort(Field field) {
            super(field);
        }

        @Override
        public void setShort(Object object, Short value) {
            try {
                field.set(object, value);
            } catch (IllegalAccessException e) {
                throw errorForSet(e);
            }
        }

        @Override
        public Short getShort(Object object) {
            try {
                return (Short) field.get(object);
            } catch (IllegalAccessException e) {
                throw errorForGet(e);
            }
        }
    }

    static final class FieldAccessorReflectInteger
            extends FieldAccessorReflect
            implements PropertyAccessorInteger {
        public FieldAccessorReflectInteger(Field field) {
            super(field);
        }

        @Override
        public void setInteger(Object object, Integer value) {
            try {
                field.set(object, value);
            } catch (IllegalAccessException e) {
                throw errorForSet(e);
            }
        }

        @Override
        public Integer getInteger(Object object) {
            try {
                return (Integer) field.get(object);
            } catch (IllegalAccessException e) {
                throw errorForGet(e);
            }
        }
    }

    static final class FieldAccessorReflectLong
            extends FieldAccessorReflect
            implements PropertyAccessorLong {
        public FieldAccessorReflectLong(Field field) {
            super(field);
        }

        @Override
        public void setLong(Object object, Long value) {
            try {
                field.set(object, value);
            } catch (IllegalAccessException e) {
                throw errorForSet(e);
            }
        }

        @Override
        public Long getLong(Object object) {
            try {
                return (Long) field.get(object);
            } catch (IllegalAccessException e) {
                throw errorForGet(e);
            }
        }
    }

    static final class FieldAccessorReflectFloat
            extends FieldAccessorReflect
            implements PropertyAccessorFloat {
        public FieldAccessorReflectFloat(Field field) {
            super(field);
        }

        @Override
        public void setFloat(Object object, Float value) {
            try {
                field.set(object, value);
            } catch (IllegalAccessException e) {
                throw errorForSet(e);
            }
        }

        @Override
        public Float getFloat(Object object) {
            try {
                return (Float) field.get(object);
            } catch (IllegalAccessException e) {
                throw errorForGet(e);
            }
        }
    }

    static final class FieldAccessorReflectDouble
            extends FieldAccessorReflect
            implements PropertyAccessorDouble {
        public FieldAccessorReflectDouble(Field field) {
            super(field);
        }

        @Override
        public void setDouble(Object object, Double value) {
            try {
                field.set(object, value);
            } catch (IllegalAccessException e) {
                throw errorForSet(e);
            }
        }

        @Override
        public Double getDouble(Object object) {
            try {
                return (Double) field.get(object);
            } catch (IllegalAccessException e) {
                throw errorForGet(e);
            }
        }
    }

    /**
     * Field accessor implementation for Number-typed properties using reflection.
     * Provides efficient getter and setter operations for Number fields via reflection.
     */
    static final class FieldAccessorReflectNumber
            extends FieldAccessorReflect
            implements PropertyAccessorNumber {
        public FieldAccessorReflectNumber(Field field) {
            super(field);
        }

        @Override
        public void setNumber(Object object, Number value) {
            try {
                field.set(object, value);
            } catch (IllegalAccessException e) {
                throw errorForSet(e);
            }
        }

        @Override
        public Number getNumber(Object object) {
            try {
                return (Number) field.get(object);
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
            return new MethodAccessorByteValue(name, propertyType, propertyClass, getter, setter);
        }
        if (propertyClass == short.class) {
            return new MethodAccessorShortValue(name, propertyType, propertyClass, getter, setter);
        }
        if (propertyClass == int.class) {
            return new MethodAccessorIntValue(name, propertyType, propertyClass, getter, setter);
        }
        if (propertyClass == long.class) {
            return new MethodAccessorLongValue(name, propertyType, propertyClass, getter, setter);
        }
        if (propertyClass == float.class) {
            return new MethodAccessorFloatValue(name, propertyType, propertyClass, getter, setter);
        }
        if (propertyClass == double.class) {
            return new MethodAccessorDoubleValue(name, propertyType, propertyClass, getter, setter);
        }
        if (propertyClass == char.class) {
            return new MethodAccessorCharValue(name, propertyType, propertyClass, getter, setter);
        }
        if (propertyClass == boolean.class) {
            return new MethodAccessorBooleanValue(name, propertyType, propertyClass, getter, setter);
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
        if (propertyClass == Boolean.class) {
            return new MethodAccessorBoolean(name, propertyType, propertyClass, getter, setter);
        }
        if (propertyClass == Byte.class) {
            return new MethodAccessorByte(name, propertyType, propertyClass, getter, setter);
        }
        if (propertyClass == Character.class) {
            return new MethodAccessorCharacter(name, propertyType, propertyClass, getter, setter);
        }
        if (propertyClass == Short.class) {
            return new MethodAccessorShort(name, propertyType, propertyClass, getter, setter);
        }
        if (propertyClass == Integer.class) {
            return new MethodAccessorInteger(name, propertyType, propertyClass, getter, setter);
        }
        if (propertyClass == Long.class) {
            return new MethodAccessorLong(name, propertyType, propertyClass, getter, setter);
        }
        if (propertyClass == Float.class) {
            return new MethodAccessorFloat(name, propertyType, propertyClass, getter, setter);
        }
        if (propertyClass == Double.class) {
            return new MethodAccessorDouble(name, propertyType, propertyClass, getter, setter);
        }
        if (propertyClass == Number.class) {
            return new MethodAccessorNumber(name, propertyType, propertyClass, getter, setter);
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
        return new FunctionAccessorByteValue(name, getterFunc, setterFunc);
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
        return new FunctionAccessorShortValue(name, getterFunc, setterFunc);
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
        return new FunctionAccessorIntValue(name, getterFunc, setterFunc);
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
        return new FunctionAccessorLongValue(name, getterFunc, setterFunc);
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
        return new FunctionAccessorFloatValue(name, getterFunc, setterFunc);
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
        return new FunctionAccessorDoubleValue<>(name, getterFunc, setterFunc);
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
        return new FunctionAccessorBooleanValue<>(name, getterFunc, setterFunc);
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
        return new FunctionAccessorCharValue(name, getterFunc, setterFunc);
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
        if (propertyClass == Boolean.class) {
            return new FunctionAccessorBoolean<T>(name, (Function<T, Boolean>) getterFunc, (BiConsumer<T, Boolean>) setterFunc);
        }
        if (propertyClass == Byte.class) {
            return new FunctionAccessorByte<T>(name, (Function<T, Byte>) getterFunc, (BiConsumer<T, Byte>) setterFunc);
        }
        if (propertyClass == Character.class) {
            return new FunctionAccessorCharacter<T>(name, (Function<T, Character>) getterFunc, (BiConsumer<T, Character>) setterFunc);
        }
        if (propertyClass == Short.class) {
            return new FunctionAccessorShort<T>(name, (Function<T, Short>) getterFunc, (BiConsumer<T, Short>) setterFunc);
        }
        if (propertyClass == Integer.class) {
            return new FunctionAccessorInteger<T>(name, (Function<T, Integer>) getterFunc, (BiConsumer<T, Integer>) setterFunc);
        }
        if (propertyClass == Long.class) {
            return new FunctionAccessorLong<T>(name, (Function<T, Long>) getterFunc, (BiConsumer<T, Long>) setterFunc);
        }
        if (propertyClass == Float.class) {
            return new FunctionAccessorFloat<T>(name, (Function<T, Float>) getterFunc, (BiConsumer<T, Float>) setterFunc);
        }
        if (propertyClass == Double.class) {
            return new FunctionAccessorDouble<T>(name, (Function<T, Double>) getterFunc, (BiConsumer<T, Double>) setterFunc);
        }
        if (propertyClass == Number.class) {
            return new FunctionAccessorNumber<T>(name, (Function<T, Number>) getterFunc, (BiConsumer<T, Number>) setterFunc);
        }
        return new FunctionAccessorObject<T, V>(name, propertyType, propertyClass, getterFunc, setterFunc);
    }

    /**
     * Method accessor implementation for char-typed properties using method invocation.
     * Provides efficient getter and setter operations for char properties via method calls.
     */
    static final class MethodAccessorCharValue
            extends MethodAccessor implements PropertyAccessorCharValue
    {
        public MethodAccessorCharValue(String name, Type propertyType, Class<?> propertyClass, Method getter, Method setter) {
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
    static final class MethodAccessorByteValue
            extends MethodAccessor implements PropertyAccessorByteValue
    {
        public MethodAccessorByteValue(String name, Type propertyType, Class<?> propertyClass, Method getter, Method setter) {
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
    static final class MethodAccessorShortValue
            extends MethodAccessor implements PropertyAccessorShortValue
    {
        public MethodAccessorShortValue(String name, Type propertyType, Class<?> propertyClass, Method getter, Method setter) {
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
    static final class MethodAccessorIntValue
            extends MethodAccessor implements PropertyAccessorIntValue
    {
        public MethodAccessorIntValue(String name, Type propertyType, Class<?> propertyClass, Method getter, Method setter) {
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
     * Method accessor implementation for Boolean-typed properties using method invocation.
     * Provides efficient getter and setter operations for Boolean properties via method calls.
     */
    static final class MethodAccessorBoolean
            extends MethodAccessor implements PropertyAccessorBoolean
    {
        public MethodAccessorBoolean(String name, Type propertyType, Class<?> propertyClass, Method getter, Method setter) {
            super(name, propertyType, propertyClass, getter, setter);
        }

        @Override
        public Boolean getBoolean(Object object) {
            try {
                return (Boolean) getter.invoke(object);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw errorForGet(e);
            }
        }

        @Override
        public void setBoolean(Object object, Boolean value) {
            try {
                setter.invoke(object, value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw errorForSet(e);
            }
        }
    }

    /**
     * Method accessor implementation for Byte-typed properties using method invocation.
     * Provides efficient getter and setter operations for Byte properties via method calls.
     */
    static final class MethodAccessorByte
            extends MethodAccessor implements PropertyAccessorByte
    {
        public MethodAccessorByte(String name, Type propertyType, Class<?> propertyClass, Method getter, Method setter) {
            super(name, propertyType, propertyClass, getter, setter);
        }

        @Override
        public Byte getByte(Object object) {
            try {
                return (Byte) getter.invoke(object);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw errorForGet(e);
            }
        }

        @Override
        public void setByte(Object object, Byte value) {
            try {
                setter.invoke(object, value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw errorForSet(e);
            }
        }
    }

    /**
     * Method accessor implementation for Character-typed properties using method invocation.
     * Provides efficient getter and setter operations for Character properties via method calls.
     */
    static final class MethodAccessorCharacter
            extends MethodAccessor implements PropertyAccessorCharacter
    {
        public MethodAccessorCharacter(String name, Type propertyType, Class<?> propertyClass, Method getter, Method setter) {
            super(name, propertyType, propertyClass, getter, setter);
        }

        @Override
        public Character getCharacter(Object object) {
            try {
                return (Character) getter.invoke(object);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw errorForGet(e);
            }
        }

        @Override
        public void setCharacter(Object object, Character value) {
            try {
                setter.invoke(object, value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw errorForSet(e);
            }
        }
    }

    /**
     * Method accessor implementation for Short-typed properties using method invocation.
     * Provides efficient getter and setter operations for Short properties via method calls.
     */
    static final class MethodAccessorShort
            extends MethodAccessor implements PropertyAccessorShort
    {
        public MethodAccessorShort(String name, Type propertyType, Class<?> propertyClass, Method getter, Method setter) {
            super(name, propertyType, propertyClass, getter, setter);
        }

        @Override
        public Short getShort(Object object) {
            try {
                return (Short) getter.invoke(object);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw errorForGet(e);
            }
        }

        @Override
        public void setShort(Object object, Short value) {
            try {
                setter.invoke(object, value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw errorForSet(e);
            }
        }
    }

    /**
     * Method accessor implementation for Integer-typed properties using method invocation.
     * Provides efficient getter and setter operations for Integer properties via method calls.
     */
    static final class MethodAccessorInteger
            extends MethodAccessor implements PropertyAccessorInteger
    {
        public MethodAccessorInteger(String name, Type propertyType, Class<?> propertyClass, Method getter, Method setter) {
            super(name, propertyType, propertyClass, getter, setter);
        }

        @Override
        public Integer getInteger(Object object) {
            try {
                return (Integer) getter.invoke(object);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw errorForGet(e);
            }
        }

        @Override
        public void setInteger(Object object, Integer value) {
            try {
                setter.invoke(object, value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw errorForSet(e);
            }
        }
    }

    /**
     * Method accessor implementation for Long-typed properties using method invocation.
     * Provides efficient getter and setter operations for Long properties via method calls.
     */
    static final class MethodAccessorLong
            extends MethodAccessor implements PropertyAccessorLong
    {
        public MethodAccessorLong(String name, Type propertyType, Class<?> propertyClass, Method getter, Method setter) {
            super(name, propertyType, propertyClass, getter, setter);
        }

        @Override
        public Long getLong(Object object) {
            try {
                return (Long) getter.invoke(object);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw errorForGet(e);
            }
        }

        @Override
        public void setLong(Object object, Long value) {
            try {
                setter.invoke(object, value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw errorForSet(e);
            }
        }
    }

    /**
     * Method accessor implementation for Float-typed properties using method invocation.
     * Provides efficient getter and setter operations for Float properties via method calls.
     */
    static final class MethodAccessorFloat
            extends MethodAccessor implements PropertyAccessorFloat
    {
        public MethodAccessorFloat(String name, Type propertyType, Class<?> propertyClass, Method getter, Method setter) {
            super(name, propertyType, propertyClass, getter, setter);
        }

        @Override
        public Float getFloat(Object object) {
            try {
                return (Float) getter.invoke(object);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw errorForGet(e);
            }
        }

        @Override
        public void setFloat(Object object, Float value) {
            try {
                setter.invoke(object, value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw errorForSet(e);
            }
        }
    }

    /**
     * Method accessor implementation for Double-typed properties using method invocation.
     * Provides efficient getter and setter operations for Double properties via method calls.
     */
    static final class MethodAccessorDouble
            extends MethodAccessor implements PropertyAccessorDouble
    {
        public MethodAccessorDouble(String name, Type propertyType, Class<?> propertyClass, Method getter, Method setter) {
            super(name, propertyType, propertyClass, getter, setter);
        }

        @Override
        public Double getDouble(Object object) {
            try {
                return (Double) getter.invoke(object);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw errorForGet(e);
            }
        }

        @Override
        public void setDouble(Object object, Double value) {
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
    static final class MethodAccessorLongValue
            extends MethodAccessor implements PropertyAccessorLongValue
    {
        public MethodAccessorLongValue(String name, Type propertyType, Class<?> propertyClass, Method getter, Method setter) {
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
    static final class MethodAccessorFloatValue
            extends MethodAccessor implements PropertyAccessorFloatValue
    {
        public MethodAccessorFloatValue(String name, Type propertyType, Class<?> propertyClass, Method getter, Method setter) {
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
    static final class MethodAccessorDoubleValue
            extends MethodAccessor implements PropertyAccessorDoubleValue
    {
        public MethodAccessorDoubleValue(String name, Type propertyType, Class<?> propertyClass, Method getter, Method setter) {
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
    static final class MethodAccessorBooleanValue
            extends MethodAccessor implements PropertyAccessorBooleanValue
    {
        public MethodAccessorBooleanValue(String name, Type propertyType, Class<?> propertyClass, Method getter, Method setter) {
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
     * Method accessor implementation for Number-typed properties using method invocation.
     * Provides efficient getter and setter operations for Number properties via method calls.
     */
    static final class MethodAccessorNumber
            extends MethodAccessor implements PropertyAccessorNumber
    {
        public MethodAccessorNumber(String name, Type propertyType, Class<?> propertyClass, Method getter, Method setter) {
            super(name, propertyType, propertyClass, getter, setter);
        }

        @Override
        public Number getNumber(Object object) {
            try {
                return (Number) getter.invoke(object);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw errorForGet(e);
            }
        }

        @Override
        public void setNumber(Object object, Number value) {
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
    static final class FunctionAccessorByteValue<T> extends FunctionAccessor<T> implements PropertyAccessorByteValue
    {
        private final ToByteFunction<T> getterFunc;
        private final ObjByteConsumer<T> setterFunc;

        public FunctionAccessorByteValue(String name, ToByteFunction<T> getterFunc, ObjByteConsumer<T> setterFunc) {
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
    static final class FunctionAccessorShortValue<T> extends FunctionAccessor<T> implements PropertyAccessorShortValue
    {
        private final ToShortFunction<T> getterFunc;
        private final ObjShortConsumer<T> setterFunc;

        public FunctionAccessorShortValue(String name, ToShortFunction<T> getterFunc, ObjShortConsumer<T> setterFunc) {
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
    static final class FunctionAccessorIntValue<T> extends FunctionAccessor<T> implements PropertyAccessorIntValue
    {
        private final ToIntFunction<T> getterFunc;
        private final ObjIntConsumer<T> setterFunc;

        public FunctionAccessorIntValue(String name, ToIntFunction<T> getterFunc, ObjIntConsumer<T> setterFunc) {
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
    static final class FunctionAccessorLongValue<T> extends FunctionAccessor<T> implements PropertyAccessorLongValue
    {
        private final ToLongFunction<T> getterFunc;
        private final ObjLongConsumer<T> setterFunc;

        public FunctionAccessorLongValue(String name, ToLongFunction<T> getterFunc, ObjLongConsumer<T> setterFunc) {
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
    static final class FunctionAccessorFloatValue<T> extends FunctionAccessor<T> implements PropertyAccessorFloatValue
    {
        private final ToFloatFunction<T> getterFunc;
        private final ObjFloatConsumer<T> setterFunc;

        public FunctionAccessorFloatValue(String name, ToFloatFunction<T> getterFunc, ObjFloatConsumer<T> setterFunc) {
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
    static final class FunctionAccessorDoubleValue<T> extends FunctionAccessor implements PropertyAccessorDoubleValue
    {
        private final ToDoubleFunction<T> getterFunc;
        private final ObjDoubleConsumer<T> setterFunc;

        public FunctionAccessorDoubleValue(String name, ToDoubleFunction<T> getterFunc, ObjDoubleConsumer<T> setterFunc) {
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
    static final class FunctionAccessorBooleanValue<T> extends FunctionAccessor<T> implements PropertyAccessorBooleanValue
    {
        private final Predicate<T> getterFunc;
        private final ObjBoolConsumer<T> setterFunc;

        public FunctionAccessorBooleanValue(String name, Predicate<T> getterFunc, ObjBoolConsumer<T> setterFunc) {
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
     * Function accessor implementation for Boolean-typed properties using functional interfaces.
     * Provides efficient getter and setter operations for Boolean properties via functional interfaces.
     *
     * @param <T> the type of the object containing the property
     */
    static final class FunctionAccessorBoolean<T> extends FunctionAccessor<T> implements PropertyAccessorBoolean {
        private final Function<T, Boolean> getterFunc;
        private final BiConsumer<T, Boolean> setterFunc;
        public FunctionAccessorBoolean(String name, Function<T, Boolean> getterFunc, BiConsumer<T, Boolean> setterFunc) {
            super(name, Boolean.class, Boolean.class, getterFunc, setterFunc);
            this.getterFunc = getterFunc;
            this.setterFunc = setterFunc;
        }
        @Override
        public Boolean getBoolean(Object object) {
            return getterFunc.apply((T) object);
        }
        @Override
        public void setBoolean(Object object, Boolean value) {
            setterFunc.accept((T) object, value);
        }
    }

    /**
     * Function accessor implementation for Byte-typed properties using functional interfaces.
     * Provides efficient getter and setter operations for Byte properties via functional interfaces.
     *
     * @param <T> the type of the object containing the property
     */
    static final class FunctionAccessorByte<T> extends FunctionAccessor<T> implements PropertyAccessorByte {
        private final Function<T, Byte> getterFunc;
        private final BiConsumer<T, Byte> setterFunc;
        public FunctionAccessorByte(String name, Function<T, Byte> getterFunc, BiConsumer<T, Byte> setterFunc) {
            super(name, Byte.class, Byte.class, getterFunc, setterFunc);
            this.getterFunc = getterFunc;
            this.setterFunc = setterFunc;
        }
        @Override
        public Byte getByte(Object object) {
            return getterFunc.apply((T) object);
        }
        @Override
        public void setByte(Object object, Byte value) {
            setterFunc.accept((T) object, value);
        }
    }

    /**
     * Function accessor implementation for Character-typed properties using functional interfaces.
     * Provides efficient getter and setter operations for Character properties via functional interfaces.
     *
     * @param <T> the type of the object containing the property
     */
    static final class FunctionAccessorCharacter<T> extends FunctionAccessor<T> implements PropertyAccessorCharacter {
        private final Function<T, Character> getterFunc;
        private final BiConsumer<T, Character> setterFunc;
        public FunctionAccessorCharacter(String name, Function<T, Character> getterFunc, BiConsumer<T, Character> setterFunc) {
            super(name, Character.class, Character.class, getterFunc, setterFunc);
            this.getterFunc = getterFunc;
            this.setterFunc = setterFunc;
        }
        @Override
        public Character getCharacter(Object object) {
            return getterFunc.apply((T) object);
        }
        @Override
        public void setCharacter(Object object, Character value) {
            setterFunc.accept((T) object, value);
        }
    }

    /**
     * Function accessor implementation for Short-typed properties using functional interfaces.
     * Provides efficient getter and setter operations for Short properties via functional interfaces.
     *
     * @param <T> the type of the object containing the property
     */
    static final class FunctionAccessorShort<T> extends FunctionAccessor<T> implements PropertyAccessorShort {
        private final Function<T, Short> getterFunc;
        private final BiConsumer<T, Short> setterFunc;
        public FunctionAccessorShort(String name, Function<T, Short> getterFunc, BiConsumer<T, Short> setterFunc) {
            super(name, Short.class, Short.class, getterFunc, setterFunc);
            this.getterFunc = getterFunc;
            this.setterFunc = setterFunc;
        }
        @Override
        public Short getShort(Object object) {
            return getterFunc.apply((T) object);
        }
        @Override
        public void setShort(Object object, Short value) {
            setterFunc.accept((T) object, value);
        }
    }

    /**
     * Function accessor implementation for Integer-typed properties using functional interfaces.
     * Provides efficient getter and setter operations for Integer properties via functional interfaces.
     *
     * @param <T> the type of the object containing the property
     */
    static final class FunctionAccessorInteger<T> extends FunctionAccessor<T> implements PropertyAccessorInteger {
        private final Function<T, Integer> getterFunc;
        private final BiConsumer<T, Integer> setterFunc;
        public FunctionAccessorInteger(String name, Function<T, Integer> getterFunc, BiConsumer<T, Integer> setterFunc) {
            super(name, Integer.class, Integer.class, getterFunc, setterFunc);
            this.getterFunc = getterFunc;
            this.setterFunc = setterFunc;
        }
        @Override
        public Integer getInteger(Object object) {
            return getterFunc.apply((T) object);
        }
        @Override
        public void setInteger(Object object, Integer value) {
            setterFunc.accept((T) object, value);
        }
    }

    /**
     * Function accessor implementation for Long-typed properties using functional interfaces.
     * Provides efficient getter and setter operations for Long properties via functional interfaces.
     *
     * @param <T> the type of the object containing the property
     */
    static final class FunctionAccessorLong<T> extends FunctionAccessor<T> implements PropertyAccessorLong {
        private final Function<T, Long> getterFunc;
        private final BiConsumer<T, Long> setterFunc;
        public FunctionAccessorLong(String name, Function<T, Long> getterFunc, BiConsumer<T, Long> setterFunc) {
            super(name, Long.class, Long.class, getterFunc, setterFunc);
            this.getterFunc = getterFunc;
            this.setterFunc = setterFunc;
        }
        @Override
        public Long getLong(Object object) {
            return getterFunc.apply((T) object);
        }
        @Override
        public void setLong(Object object, Long value) {
            setterFunc.accept((T) object, value);
        }
    }

    /**
     * Function accessor implementation for Float-typed properties using functional interfaces.
     * Provides efficient getter and setter operations for Float properties via functional interfaces.
     *
     * @param <T> the type of the object containing the property
     */
    static final class FunctionAccessorFloat<T> extends FunctionAccessor<T> implements PropertyAccessorFloat {
        private final Function<T, Float> getterFunc;
        private final BiConsumer<T, Float> setterFunc;
        public FunctionAccessorFloat(String name, Function<T, Float> getterFunc, BiConsumer<T, Float> setterFunc) {
            super(name, Float.class, Float.class, getterFunc, setterFunc);
            this.getterFunc = getterFunc;
            this.setterFunc = setterFunc;
        }
        @Override
        public Float getFloat(Object object) {
            return getterFunc.apply((T) object);
        }
        @Override
        public void setFloat(Object object, Float value) {
            setterFunc.accept((T) object, value);
        }
    }

    /**
     * Function accessor implementation for Double-typed properties using functional interfaces.
     * Provides efficient getter and setter operations for Double properties via functional interfaces.
     *
     * @param <T> the type of the object containing the property
     */
    static final class FunctionAccessorDouble<T> extends FunctionAccessor<T> implements PropertyAccessorDouble {
        private final Function<T, Double> getterFunc;
        private final BiConsumer<T, Double> setterFunc;
        public FunctionAccessorDouble(String name, Function<T, Double> getterFunc, BiConsumer<T, Double> setterFunc) {
            super(name, Double.class, Double.class, getterFunc, setterFunc);
            this.getterFunc = getterFunc;
            this.setterFunc = setterFunc;
        }
        @Override
        public Double getDouble(Object object) {
            return getterFunc.apply((T) object);
        }
        @Override
        public void setDouble(Object object, Double value) {
            setterFunc.accept((T) object, value);
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
     * Function accessor implementation for Number-typed properties using functional interfaces.
     * Provides efficient getter and setter operations for Number properties via functional interfaces.
     *
     * @param <T> the type of the object containing the property
     */
    static final class FunctionAccessorNumber<T> extends FunctionAccessor<T> implements PropertyAccessorNumber {
        private final Function<T, Number> getterFunc;
        private final BiConsumer<T, Number> setterFunc;
        public FunctionAccessorNumber(String name, Function<T, Number> getterFunc, BiConsumer<T, Number> setterFunc) {
            super(name, Number.class, Number.class, getterFunc, setterFunc);
            this.getterFunc = getterFunc;
            this.setterFunc = setterFunc;
        }
        @Override
        public Number getNumber(Object object) {
            return getterFunc.apply((T) object);
        }
        @Override
        public void setNumber(Object object, Number value) {
            setterFunc.accept((T) object, value);
        }
    }

    /**
     * Function accessor implementation for char-typed properties using functional interfaces.
     * Provides efficient getter and setter operations for char properties via functional interfaces.
     *
     * @param <T> the type of the object containing the property
     */
    static final class FunctionAccessorCharValue<T> extends FunctionAccessor<T> implements PropertyAccessorCharValue {
        private final ToCharFunction<T> getterFunc;
        private final ObjCharConsumer<T> setterFunc;
        public FunctionAccessorCharValue(String name, ToCharFunction<T> getterFunc, ObjCharConsumer<T> setterFunc) {
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

    public PropertyAccessor create(PropertyAccessor impl, ObjBoolConsumer getterConsumer, ObjBoolConsumer setterConsumer) {
        return new PropertyAccessorWrapperBooleanValue(impl, getterConsumer, setterConsumer);
    }

    public PropertyAccessor create(PropertyAccessor impl, ObjByteConsumer getterConsumer, ObjByteConsumer setterConsumer) {
        return new PropertyAccessorWrapperByteValue(impl, getterConsumer, setterConsumer);
    }

    public PropertyAccessor create(PropertyAccessor impl, ObjCharConsumer getterConsumer, ObjCharConsumer setterConsumer) {
        return new PropertyAccessorWrapperCharValue(impl, getterConsumer, setterConsumer);
    }

    public PropertyAccessor create(PropertyAccessor impl, ObjShortConsumer getterConsumer, ObjShortConsumer setterConsumer) {
        return new PropertyAccessorWrapperShortValue(impl, getterConsumer, setterConsumer);
    }

    public PropertyAccessor create(PropertyAccessor impl, ObjIntConsumer getterConsumer, ObjIntConsumer setterConsumer) {
        return new PropertyAccessorWrapperIntValue(impl, getterConsumer, setterConsumer);
    }

    public PropertyAccessor create(PropertyAccessor impl, ObjLongConsumer getterConsumer, ObjLongConsumer setterConsumer) {
        return new PropertyAccessorWrapperLongValue(impl, getterConsumer, setterConsumer);
    }

    public PropertyAccessor create(PropertyAccessor impl, ObjFloatConsumer getterConsumer, ObjFloatConsumer setterConsumer) {
        return new PropertyAccessorWrapperFloatValue(impl, getterConsumer, setterConsumer);
    }

    public PropertyAccessor create(PropertyAccessor impl, ObjDoubleConsumer getterConsumer, ObjDoubleConsumer setterConsumer) {
        return new PropertyAccessorWrapperDoubleValue(impl, getterConsumer, setterConsumer);
    }

    public PropertyAccessor create(PropertyAccessor impl, BiConsumer getterConsumer, BiConsumer setterConsumer) {
        Class<?> propertyClass = impl.propertyClass();
        if (propertyClass == Boolean.class) {
            return new PropertyAccessorWrapperBoolean(impl, getterConsumer, setterConsumer);
        }
        if (propertyClass == Byte.class) {
            return new PropertyAccessorWrapperByte(impl, getterConsumer, setterConsumer);
        }
        if (propertyClass == Character.class) {
            return new PropertyAccessorWrapperCharacter(impl, getterConsumer, setterConsumer);
        }
        if (propertyClass == Short.class) {
            return new PropertyAccessorWrapperShort(impl, getterConsumer, setterConsumer);
        }
        if (propertyClass == Integer.class) {
            return new PropertyAccessorWrapperInteger(impl, getterConsumer, setterConsumer);
        }
        if (propertyClass == Long.class) {
            return new PropertyAccessorWrapperLong(impl, getterConsumer, setterConsumer);
        }
        if (propertyClass == Float.class) {
            return new PropertyAccessorWrapperFloat(impl, getterConsumer, setterConsumer);
        }
        if (propertyClass == Double.class) {
            return new PropertyAccessorWrapperDouble(impl, getterConsumer, setterConsumer);
        }
        if (propertyClass == String.class) {
            return new PropertyAccessorWrapperString(impl, getterConsumer, setterConsumer);
        }
        if (propertyClass == BigInteger.class) {
            return new PropertyAccessorWrapperBigInteger(impl, getterConsumer, setterConsumer);
        }
        if (propertyClass == BigDecimal.class) {
            return new PropertyAccessorWrapperBigDecimal(impl, getterConsumer, setterConsumer);
        }
        if (propertyClass == Number.class) {
            return new PropertyAccessorWrapperNumber(impl, getterConsumer, setterConsumer);
        }
        return new PropertyAccessorWrapperObject(impl, getterConsumer, setterConsumer);
    }

    abstract static class PropertyAccessorWrapper implements PropertyAccessor {
        final PropertyAccessor impl;
        public PropertyAccessorWrapper(PropertyAccessor impl) {
            this.impl = impl;
        }

        @Override
        public final String name() {
            return impl.name();
        }

        @Override
        public final Class<?> propertyClass() {
            return impl.propertyClass();
        }

        @Override
        public final Type propertyType() {
            return impl.propertyType();
        }

        @Override
        public final boolean supportGet() {
            return impl.supportGet();
        }

        @Override
        public final boolean supportSet() {
            return impl.supportSet();
        }
    }

    static final class PropertyAccessorWrapperBooleanValue extends PropertyAccessorWrapper implements PropertyAccessorBooleanValue {
        private final ObjBoolConsumer getterConsumer;
        private final ObjBoolConsumer setterConsumer;
        public PropertyAccessorWrapperBooleanValue(PropertyAccessor impl, ObjBoolConsumer getterConsumer, ObjBoolConsumer setterConsumer) {
            super(impl);
            this.getterConsumer = getterConsumer;
            this.setterConsumer = setterConsumer;
        }

        @Override
        public boolean getBooleanValue(Object object) {
            boolean value = impl.getBooleanValue(object);
            if (getterConsumer != null) {
                getterConsumer.accept(object, value);
            }
            return value;
        }

        @Override
        public void setBooleanValue(Object object, boolean value) {
            if (setterConsumer != null) {
                setterConsumer.accept(object, value);
            }
            impl.setBooleanValue(object, value);
        }
    }

    static final class PropertyAccessorWrapperByteValue extends PropertyAccessorWrapper implements PropertyAccessorByteValue {
        private final ObjByteConsumer getterConsumer;
        private final ObjByteConsumer setterConsumer;
        public PropertyAccessorWrapperByteValue(PropertyAccessor impl, ObjByteConsumer getterConsumer, ObjByteConsumer setterConsumer) {
            super(impl);
            this.getterConsumer = getterConsumer;
            this.setterConsumer = setterConsumer;
        }

        @Override
        public byte getByteValue(Object object) {
            byte value = impl.getByteValue(object);
            if (getterConsumer != null) {
                getterConsumer.accept(object, value);
            }
            return value;
        }

        @Override
        public void setByteValue(Object object, byte value) {
            if (setterConsumer != null) {
                setterConsumer.accept(object, value);
            }
            impl.setByteValue(object, value);
        }
    }

    static final class PropertyAccessorWrapperShortValue extends PropertyAccessorWrapper implements PropertyAccessorShortValue {
        private final ObjShortConsumer getterConsumer;
        private final ObjShortConsumer setterConsumer;
        public PropertyAccessorWrapperShortValue(PropertyAccessor impl, ObjShortConsumer getterConsumer, ObjShortConsumer setterConsumer) {
            super(impl);
            this.getterConsumer = getterConsumer;
            this.setterConsumer = setterConsumer;
        }

        @Override
        public short getShortValue(Object object) {
            short value = impl.getShortValue(object);
            if (getterConsumer != null) {
                getterConsumer.accept(object, value);
            }
            return value;
        }

        @Override
        public void setShortValue(Object object, short value) {
            if (setterConsumer != null) {
                setterConsumer.accept(object, value);
            }
            impl.setShortValue(object, value);
        }
    }

    static final class PropertyAccessorWrapperIntValue extends PropertyAccessorWrapper implements PropertyAccessorIntValue {
        private final ObjIntConsumer getterConsumer;
        private final ObjIntConsumer setterConsumer;
        public PropertyAccessorWrapperIntValue(PropertyAccessor impl, ObjIntConsumer getterConsumer, ObjIntConsumer setterConsumer) {
            super(impl);
            this.getterConsumer = getterConsumer;
            this.setterConsumer = setterConsumer;
        }

        @Override
        public int getIntValue(Object object) {
            int value = impl.getIntValue(object);
            if (getterConsumer != null) {
                getterConsumer.accept(object, value);
            }
            return value;
        }

        @Override
        public void setIntValue(Object object, int value) {
            if (setterConsumer != null) {
                setterConsumer.accept(object, value);
            }
            impl.setIntValue(object, value);
        }
    }

    static final class PropertyAccessorWrapperLongValue extends PropertyAccessorWrapper implements PropertyAccessorLongValue {
        private final ObjLongConsumer getterConsumer;
        private final ObjLongConsumer setterConsumer;
        public PropertyAccessorWrapperLongValue(PropertyAccessor impl, ObjLongConsumer getterConsumer, ObjLongConsumer setterConsumer) {
            super(impl);
            this.getterConsumer = getterConsumer;
            this.setterConsumer = setterConsumer;
        }

        @Override
        public long getLongValue(Object object) {
            long value = impl.getLongValue(object);
            if (getterConsumer != null) {
                getterConsumer.accept(object, value);
            }
            return value;
        }

        @Override
        public void setLongValue(Object object, long value) {
            if (setterConsumer != null) {
                setterConsumer.accept(object, value);
            }
            impl.setLongValue(object, value);
        }
    }

    static final class PropertyAccessorWrapperFloatValue extends PropertyAccessorWrapper implements PropertyAccessorFloatValue {
        private final ObjFloatConsumer getterConsumer;
        private final ObjFloatConsumer setterConsumer;
        public PropertyAccessorWrapperFloatValue(PropertyAccessor impl, ObjFloatConsumer getterConsumer, ObjFloatConsumer setterConsumer) {
            super(impl);
            this.getterConsumer = getterConsumer;
            this.setterConsumer = setterConsumer;
        }

        @Override
        public float getFloatValue(Object object) {
            float value = impl.getFloatValue(object);
            if (getterConsumer != null) {
                getterConsumer.accept(object, value);
            }
            return value;
        }

        @Override
        public void setFloatValue(Object object, float value) {
            if (setterConsumer != null) {
                setterConsumer.accept(object, value);
            }
            impl.setFloatValue(object, value);
        }
    }

    static final class PropertyAccessorWrapperDoubleValue extends PropertyAccessorWrapper implements PropertyAccessorDoubleValue {
        private final ObjDoubleConsumer getterConsumer;
        private final ObjDoubleConsumer setterConsumer;
        public PropertyAccessorWrapperDoubleValue(PropertyAccessor impl, ObjDoubleConsumer getterConsumer, ObjDoubleConsumer setterConsumer) {
            super(impl);
            this.getterConsumer = getterConsumer;
            this.setterConsumer = setterConsumer;
        }

        @Override
        public double getDoubleValue(Object object) {
            double value = impl.getDoubleValue(object);
            if (getterConsumer != null) {
                getterConsumer.accept(object, value);
            }
            return value;
        }

        @Override
        public void setDoubleValue(Object object, double value) {
            if (setterConsumer != null) {
                setterConsumer.accept(object, value);
            }
            impl.setDoubleValue(object, value);
        }
    }

    static final class PropertyAccessorWrapperCharValue extends PropertyAccessorWrapper implements PropertyAccessorCharValue {
        private final ObjCharConsumer getterConsumer;
        private final ObjCharConsumer setterConsumer;
        public PropertyAccessorWrapperCharValue(PropertyAccessor impl, ObjCharConsumer getterConsumer, ObjCharConsumer setterConsumer) {
            super(impl);
            this.getterConsumer = getterConsumer;
            this.setterConsumer = setterConsumer;
        }

        @Override
        public char getCharValue(Object object) {
            char value = impl.getCharValue(object);
            if (getterConsumer != null) {
                getterConsumer.accept(object, value);
            }
            return value;
        }

        @Override
        public void setCharValue(Object object, char value) {
            if (setterConsumer != null) {
                setterConsumer.accept(object, value);
            }
            impl.setCharValue(object, value);
        }
    }

    static final class PropertyAccessorWrapperBoolean extends PropertyAccessorWrapper implements PropertyAccessorBoolean {
        private PropertyAccessorBoolean booleanImpl;
        private final BiConsumer getterConsumer;
        private final BiConsumer setterConsumer;
        public PropertyAccessorWrapperBoolean(PropertyAccessor impl, BiConsumer getterConsumer, BiConsumer setterConsumer) {
            super(impl);
            booleanImpl = (PropertyAccessorBoolean) impl;
            this.getterConsumer = getterConsumer;
            this.setterConsumer = setterConsumer;
        }

        @Override
        public void setBoolean(Object object, Boolean value) {
            if (setterConsumer != null) {
                setterConsumer.accept(object, value);
            }
            booleanImpl.setBoolean(object, value);
        }

        @Override
        public Boolean getBoolean(Object object) {
            Boolean value = booleanImpl.getBoolean(object);
            if (getterConsumer != null) {
                getterConsumer.accept(object, value);
            }
            return value;
        }
    }

    static final class PropertyAccessorWrapperByte extends PropertyAccessorWrapper implements PropertyAccessorByte {
        private PropertyAccessorByte byteImpl;
        private final BiConsumer getterConsumer;
        private final BiConsumer setterConsumer;
        public PropertyAccessorWrapperByte(PropertyAccessor impl, BiConsumer getterConsumer, BiConsumer setterConsumer) {
            super(impl);
            byteImpl = (PropertyAccessorByte) impl;
            this.getterConsumer = getterConsumer;
            this.setterConsumer = setterConsumer;
        }

        @Override
        public void setByte(Object object, Byte value) {
            if (setterConsumer != null) {
                setterConsumer.accept(object, value);
            }
            byteImpl.setByte(object, value);
        }

        @Override
        public Byte getByte(Object object) {
            Byte value = byteImpl.getByte(object);
            if (getterConsumer != null) {
                getterConsumer.accept(object, value);
            }
            return value;
        }
    }

    static final class PropertyAccessorWrapperCharacter extends PropertyAccessorWrapper implements PropertyAccessorCharacter {
        private PropertyAccessorCharacter characterImpl;
        private final BiConsumer getterConsumer;
        private final BiConsumer setterConsumer;
        public PropertyAccessorWrapperCharacter(PropertyAccessor impl, BiConsumer getterConsumer, BiConsumer setterConsumer) {
            super(impl);
            characterImpl = (PropertyAccessorCharacter) impl;
            this.getterConsumer = getterConsumer;
            this.setterConsumer = setterConsumer;
        }

        @Override
        public void setCharacter(Object object, Character value) {
            if (setterConsumer != null) {
                setterConsumer.accept(object, value);
            }
            characterImpl.setCharacter(object, value);
        }

        @Override
        public Character getCharacter(Object object) {
            Character value = characterImpl.getCharacter(object);
            if (getterConsumer != null) {
                getterConsumer.accept(object, value);
            }
            return value;
        }
    }

    static final class PropertyAccessorWrapperShort extends PropertyAccessorWrapper implements PropertyAccessorShort {
        private PropertyAccessorShort shortImpl;
        private final BiConsumer getterConsumer;
        private final BiConsumer setterConsumer;
        public PropertyAccessorWrapperShort(PropertyAccessor impl, BiConsumer getterConsumer, BiConsumer setterConsumer) {
            super(impl);
            shortImpl = (PropertyAccessorShort) impl;
            this.getterConsumer = getterConsumer;
            this.setterConsumer = setterConsumer;
        }

        @Override
        public void setShort(Object object, Short value) {
            if (setterConsumer != null) {
                setterConsumer.accept(object, value);
            }
            shortImpl.setShort(object, value);
        }

        @Override
        public Short getShort(Object object) {
            Short value = shortImpl.getShort(object);
            if (getterConsumer != null) {
                getterConsumer.accept(object, value);
            }
            return value;
        }
    }

    static final class PropertyAccessorWrapperInteger extends PropertyAccessorWrapper implements PropertyAccessorInteger {
        private PropertyAccessorInteger integerImpl;
        private final BiConsumer getterConsumer;
        private final BiConsumer setterConsumer;
        public PropertyAccessorWrapperInteger(PropertyAccessor impl, BiConsumer getterConsumer, BiConsumer setterConsumer) {
            super(impl);
            integerImpl = (PropertyAccessorInteger) impl;
            this.getterConsumer = getterConsumer;
            this.setterConsumer = setterConsumer;
        }

        @Override
        public void setInteger(Object object, Integer value) {
            if (setterConsumer != null) {
                setterConsumer.accept(object, value);
            }
            integerImpl.setInteger(object, value);
        }

        @Override
        public Integer getInteger(Object object) {
            Integer value = integerImpl.getInteger(object);
            if (getterConsumer != null) {
                getterConsumer.accept(object, value);
            }
            return value;
        }
    }

    static final class PropertyAccessorWrapperLong extends PropertyAccessorWrapper implements PropertyAccessorLong {
        private PropertyAccessorLong longImpl;
        private final BiConsumer getterConsumer;
        private final BiConsumer setterConsumer;
        public PropertyAccessorWrapperLong(PropertyAccessor impl, BiConsumer getterConsumer, BiConsumer setterConsumer) {
            super(impl);
            longImpl = (PropertyAccessorLong) impl;
            this.getterConsumer = getterConsumer;
            this.setterConsumer = setterConsumer;
        }

        @Override
        public void setLong(Object object, Long value) {
            if (setterConsumer != null) {
                setterConsumer.accept(object, value);
            }
            longImpl.setLong(object, value);
        }

        @Override
        public Long getLong(Object object) {
            Long value = longImpl.getLong(object);
            if (getterConsumer != null) {
                getterConsumer.accept(object, value);
            }
            return value;
        }
    }

    static final class PropertyAccessorWrapperFloat extends PropertyAccessorWrapper implements PropertyAccessorFloat {
        private PropertyAccessorFloat floatImpl;
        private final BiConsumer getterConsumer;
        private final BiConsumer setterConsumer;
        public PropertyAccessorWrapperFloat(PropertyAccessor impl, BiConsumer getterConsumer, BiConsumer setterConsumer) {
            super(impl);
            floatImpl = (PropertyAccessorFloat) impl;
            this.getterConsumer = getterConsumer;
            this.setterConsumer = setterConsumer;
        }

        @Override
        public void setFloat(Object object, Float value) {
            if (setterConsumer != null) {
                setterConsumer.accept(object, value);
            }
            floatImpl.setFloat(object, value);
        }

        @Override
        public Float getFloat(Object object) {
            Float value = floatImpl.getFloat(object);
            if (getterConsumer != null) {
                getterConsumer.accept(object, value);
            }
            return value;
        }
    }

    static final class PropertyAccessorWrapperDouble extends PropertyAccessorWrapper implements PropertyAccessorDouble {
        private PropertyAccessorDouble doubleImpl;
        private final BiConsumer getterConsumer;
        private final BiConsumer setterConsumer;
        public PropertyAccessorWrapperDouble(PropertyAccessor impl, BiConsumer getterConsumer, BiConsumer setterConsumer) {
            super(impl);
            doubleImpl = (PropertyAccessorDouble) impl;
            this.getterConsumer = getterConsumer;
            this.setterConsumer = setterConsumer;
        }

        @Override
        public void setDouble(Object object, Double value) {
            if (setterConsumer != null) {
                setterConsumer.accept(object, value);
            }
            doubleImpl.setDouble(object, value);
        }

        @Override
        public Double getDouble(Object object) {
            Double value = doubleImpl.getDouble(object);
            if (getterConsumer != null) {
                getterConsumer.accept(object, value);
            }
            return value;
        }
    }

    static final class PropertyAccessorWrapperString extends PropertyAccessorWrapper implements PropertyAccessorString {
        private PropertyAccessorString stringImpl;
        private final BiConsumer getterConsumer;
        private final BiConsumer setterConsumer;
        public PropertyAccessorWrapperString(PropertyAccessor impl, BiConsumer getterConsumer, BiConsumer setterConsumer) {
            super(impl);
            stringImpl = (PropertyAccessorString) impl;
            this.getterConsumer = getterConsumer;
            this.setterConsumer = setterConsumer;
        }

        @Override
        public void setString(Object object, String value) {
            if (setterConsumer != null) {
                setterConsumer.accept(object, value);
            }
            stringImpl.setString(object, value);
        }

        @Override
        public String getString(Object object) {
            String value = stringImpl.getString(object);
            if (getterConsumer != null) {
                getterConsumer.accept(object, value);
            }
            return value;
        }
    }

    static final class PropertyAccessorWrapperBigInteger extends PropertyAccessorWrapper implements PropertyAccessorBigInteger {
        private PropertyAccessorBigInteger bigIntegerImpl;
        private final BiConsumer getterConsumer;
        private final BiConsumer setterConsumer;
        public PropertyAccessorWrapperBigInteger(PropertyAccessor impl, BiConsumer getterConsumer, BiConsumer setterConsumer) {
            super(impl);
            bigIntegerImpl = (PropertyAccessorBigInteger) impl;
            this.getterConsumer = getterConsumer;
            this.setterConsumer = setterConsumer;
        }

        @Override
        public void setBigInteger(Object object, BigInteger value) {
            if (setterConsumer != null) {
                setterConsumer.accept(object, value);
            }
            bigIntegerImpl.setBigInteger(object, value);
        }

        @Override
        public BigInteger getBigInteger(Object object) {
            BigInteger value = bigIntegerImpl.getBigInteger(object);
            if (getterConsumer != null) {
                getterConsumer.accept(object, value);
            }
            return value;
        }
    }

    static final class PropertyAccessorWrapperBigDecimal extends PropertyAccessorWrapper implements PropertyAccessorBigDecimal {
        private PropertyAccessorBigDecimal bigDecimalImpl;
        private final BiConsumer getterConsumer;
        private final BiConsumer setterConsumer;
        public PropertyAccessorWrapperBigDecimal(PropertyAccessor impl, BiConsumer getterConsumer, BiConsumer setterConsumer) {
            super(impl);
            bigDecimalImpl = (PropertyAccessorBigDecimal) impl;
            this.getterConsumer = getterConsumer;
            this.setterConsumer = setterConsumer;
        }

        @Override
        public void setBigDecimal(Object object, BigDecimal value) {
            if (setterConsumer != null) {
                setterConsumer.accept(object, value);
            }
            bigDecimalImpl.setBigDecimal(object, value);
        }

        @Override
        public BigDecimal getBigDecimal(Object object) {
            BigDecimal value = bigDecimalImpl.getBigDecimal(object);
            if (getterConsumer != null) {
                getterConsumer.accept(object, value);
            }
            return value;
        }
    }

    static final class PropertyAccessorWrapperNumber extends PropertyAccessorWrapper implements PropertyAccessorNumber {
        private PropertyAccessorNumber numberImpl;
        private final BiConsumer getterConsumer;
        private final BiConsumer setterConsumer;
        public PropertyAccessorWrapperNumber(PropertyAccessor impl, BiConsumer getterConsumer, BiConsumer setterConsumer) {
            super(impl);
            numberImpl = (PropertyAccessorNumber) impl;
            this.getterConsumer = getterConsumer;
            this.setterConsumer = setterConsumer;
        }

        @Override
        public void setNumber(Object object, Number value) {
            if (setterConsumer != null) {
                setterConsumer.accept(object, value);
            }
            numberImpl.setNumber(object, value);
        }

        @Override
        public Number getNumber(Object object) {
            Number value = numberImpl.getNumber(object);
            if (getterConsumer != null) {
                getterConsumer.accept(object, value);
            }
            return value;
        }
    }

    static final class PropertyAccessorWrapperObject extends PropertyAccessorWrapper implements PropertyAccessorObject {
        private PropertyAccessorObject objImpl;
        private final BiConsumer getterConsumer;
        private final BiConsumer setterConsumer;
        public PropertyAccessorWrapperObject(PropertyAccessor impl, BiConsumer getterConsumer, BiConsumer setterConsumer) {
            super(impl);
            objImpl = (PropertyAccessorObject) impl;
            this.getterConsumer = getterConsumer;
            this.setterConsumer = setterConsumer;
        }

        @Override
        public void setObject(Object object, Object value) {
            if (setterConsumer != null) {
                setterConsumer.accept(object, value);
            }
            objImpl.setObject(object, value);
        }

        @Override
        public Object getObject(Object object) {
            Object value = objImpl.getObject(object);
            if (getterConsumer != null) {
                getterConsumer.accept(object, value);
            }
            return value;
        }
    }
}
