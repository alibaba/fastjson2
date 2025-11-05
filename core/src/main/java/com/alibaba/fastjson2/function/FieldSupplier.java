package com.alibaba.fastjson2.function;

/**
 * Represents a supplier of field values from an object based on field index.
 * This is used for retrieving field values by index in fastjson2's internal
 * serialization mechanism.
 *
 * <p>This is a functional interface whose functional method is {@link #get(Object, int)}.
 *
 * @param <T> the type of the object from which the field value is retrieved
 * @see java.util.function.Supplier
 * @see java.util.function.Function
 * @since 2.0.0
 */
@FunctionalInterface
public interface FieldSupplier<T> {
    /**
     * Gets the value of the field at the specified index from the given object.
     *
     * @param object the object from which to retrieve the field value
     * @param fieldIndex the index of the field to retrieve
     * @return the value of the field, or null if not present
     */
    Object get(T object, int fieldIndex);
}
