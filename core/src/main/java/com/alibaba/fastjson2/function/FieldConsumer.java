package com.alibaba.fastjson2.function;

/**
 * Represents an operation that accepts an object, a field index, and a field value,
 * and returns no result. This is used for setting field values by index in fastjson2's
 * internal serialization mechanism.
 *
 * <p>This is a functional interface whose functional method is {@link #accept(Object, int, Object)}.
 *
 * @param <T> the type of the object whose field is being set
 * @see java.util.function.BiConsumer
 * @since 2.0.0
 */
@FunctionalInterface
public interface FieldConsumer<T> {
    /**
     * Performs this operation on the given arguments.
     *
     * @param object the object whose field will be set
     * @param fieldIndex the index of the field to set
     * @param fieldValue the value to set for the field
     */
    void accept(T object, int fieldIndex, Object fieldValue);
}
