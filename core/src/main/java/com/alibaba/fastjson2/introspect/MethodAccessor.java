package com.alibaba.fastjson2.introspect;

import com.alibaba.fastjson2.JSONException;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * Abstract base class for method-based property accessors.
 * Provides common functionality for accessing object properties using getter and setter methods.
 * This allows for accessing properties through standard getter/setter method pairs,
 * providing flexibility in how properties are accessed compared to direct field access.
 * <p>
 * Implements PropertyAccessor interface using method invocation for property access,
 * providing efficient property access for serialization and deserialization operations.
 * </p>
 */
public abstract class MethodAccessor implements PropertyAccessor {
    /** The name of the property being accessed */
    protected final String name;
    /** The generic type of the property */
    protected final Type propertyType;
    /** The runtime class of the property */
    protected final Class<?> propertyClass;
    /** The getter method for accessing the property value */
    protected final Method getter;
    /** The setter method for setting the property value */
    protected final Method setter;

    /**
     * Constructs a MethodAccessor with the specified parameters.
     *
     * @param name the name of the property
     * @param propertyType the generic type of the property
     * @param propertyClass the runtime class of the property
     * @param getter the getter method for accessing the property value
     * @param setter the setter method for setting the property value
     */
    public MethodAccessor(String name, Type propertyType, Class<?> propertyClass, Method getter, Method setter) {
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
     * A property supports getting if a getter method is provided.
     *
     * @return true if the getter method is not null, false otherwise
     */
    public final boolean supportGet() {
        return getter != null;
    }

    /**
     * Checks if the property supports setting values.
     * A property supports setting if a setter method is provided.
     *
     * @return true if the setter method is not null, false otherwise
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
        return new JSONException((getter == null ? name : getter.toString()).concat(" get error"), e);
    }

    /**
     * Creates a JSON exception for setter errors.
     *
     * @param e the exception that occurred during setting
     * @return a JSONException with details about the setter error
     */
    final JSONException errorForSet(Throwable e) {
        return new JSONException((setter == null ? name : setter.toString()).concat(" set error"), e);
    }
}
