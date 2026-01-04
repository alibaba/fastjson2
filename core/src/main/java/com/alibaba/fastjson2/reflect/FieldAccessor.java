package com.alibaba.fastjson2.reflect;

import com.alibaba.fastjson2.JSONException;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

/**
 * Abstract base class for field-based property accessors.
 * Provides common functionality for accessing object fields using reflection.
 * Handles field metadata and determines if the field supports setting based
 * on whether the field is declared as final.
 * <p>
 * Implements PropertyAccessor interface using direct field access through
 * reflection, providing efficient property access for serialization and
 * deserialization operations.
 * </p>
 */
public abstract class FieldAccessor
        implements PropertyAccessor {
    /** The underlying Field object being accessed */
    protected final Field field;
    /** The name of the field */
    protected final String fieldName;
    /** The runtime class of the field */
    protected final Class<?> propertyClass;
    /** The generic type of the field */
    protected final Type propertyType;
    /** Flag indicating whether the field supports setting values */
    protected final boolean supportSet;

    /**
     * Constructs a FieldAccessor for the given field.
     *
     * @param field the field to be accessed
     */
    protected FieldAccessor(Field field) {
        this.field = field;
        this.propertyClass = field.getType();
        this.propertyType = field.getGenericType();
        this.fieldName = field.getName();
        // A field can be set if it's not declared as final
        supportSet = (field.getModifiers() & Modifier.FINAL) == 0;
    }

    /**
     * Returns the underlying Field object.
     *
     * @return the Field object
     */
    public final Field field() {
        return field;
    }

    @Override
    public final Class<?> propertyClass() {
        return propertyClass;
    }

    @Override
    public final Type propertyType() {
        return propertyType;
    }

    @Override
    public final String name() {
        return fieldName;
    }

    @Override
    public final boolean supportGet() {
        return true;
    }

    @Override
    public final boolean supportSet() {
        return supportSet;
    }

    @Override
    public String toString() {
        return field.toString();
    }

    /**
     * Creates a JSON exception for getter errors.
     *
     * @param e the exception that occurred during getting
     * @return a JSONException with details about the getter error
     */
    final JSONException errorForGet(Throwable e) {
        return new JSONException(field.toString().concat(" get error"), e);
    }

    /**
     * Creates a JSON exception for setter errors.
     *
     * @param e the exception that occurred during setting
     * @return a JSONException with details about the setter error
     */
    final JSONException errorForSet(Throwable e) {
        return new JSONException(field.toString().concat(" set error"), e);
    }
}
