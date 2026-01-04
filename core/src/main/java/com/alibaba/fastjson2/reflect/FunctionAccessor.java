package com.alibaba.fastjson2.reflect;

import com.alibaba.fastjson2.JSONException;

import java.lang.reflect.Type;

/**
 * Abstract base class for function-based property accessors.
 * Provides common functionality for accessing object properties using getter and setter functions.
 * This allows for accessing properties through functional interfaces rather than direct field access
 * or method invocation, providing flexibility in how properties are accessed.
 * <p>
 * Implements PropertyAccessor interface using functional interfaces for property access,
 * providing efficient property access for serialization and deserialization operations.
 * </p>
 *
 * @param <T> the type of the object containing the property
 */
public abstract class FunctionAccessor<T>
        implements PropertyAccessor {
    /** The name of the property being accessed */
    protected final String name;
    /** The generic type of the property */
    protected final Type propertyType;
    /** The runtime class of the property */
    protected final Class<?> propertyClass;
    /** The getter function for accessing the property value */
    protected final Object getter;
    /** The setter function for setting the property value */
    protected final Object setter;

    /**
     * Constructs a FunctionAccessor with the specified parameters.
     *
     * @param name the name of the property
     * @param propertyType the generic type of the property
     * @param propertyClass the runtime class of the property
     * @param getter the getter function for accessing the property value
     * @param setter the setter function for setting the property value
     */
    public FunctionAccessor(String name, Type propertyType, Class<?> propertyClass, Object getter, Object setter) {
        this.name = name;
        this.propertyType = propertyType;
        this.propertyClass = propertyClass;
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    public final String name() {
        return name;
    }

    /**
     * Checks if the property supports getting values.
     * A property supports getting if a getter function is provided.
     *
     * @return true if the getter function is not null, false otherwise
     */
    public final boolean supportGet() {
        return getter != null;
    }

    /**
     * Checks if the property supports setting values.
     * A property supports setting if a setter function is provided.
     *
     * @return true if the setter function is not null, false otherwise
     */
    public final boolean supportSet() {
        return setter != null;
    }

    public final Class<?> propertyClass() {
        return propertyClass;
    }

    public final Type propertyType() {
        return propertyType;
    }

    /**
     * Creates a JSON exception for getter errors.
     *
     * @param e the exception that occurred during getting
     * @return a JSONException with details about the getter error
     */
    final JSONException errorForGet(Throwable e) {
        return new JSONException(name.concat(" get error"), e);
    }

    /**
     * Creates a JSON exception for setter errors.
     *
     * @param e the exception that occurred during setting
     * @return a JSONException with details about the setter error
     */
    final JSONException errorForSet(Throwable e) {
        return new JSONException(name.concat(" set error"), e);
    }
}
