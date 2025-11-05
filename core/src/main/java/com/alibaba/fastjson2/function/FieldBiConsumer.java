package com.alibaba.fastjson2.function;

import java.util.function.BiConsumer;

/**
 * A {@link BiConsumer} implementation that adapts a {@link FieldConsumer} by binding
 * a specific field index. This allows using the field index-aware consumer in contexts
 * that expect a standard BiConsumer interface.
 *
 * <p>This class is used internally by fastjson2's serialization mechanism to efficiently
 * set field values by pre-bound field indices.
 *
 * @param <T> the type of the object whose field is being set
 * @see FieldConsumer
 * @since 2.0.0
 */
public final class FieldBiConsumer<T>
        implements BiConsumer<T, Object> {
    /**
     * The field index to use when accepting values
     */
    public final int fieldIndex;

    /**
     * The underlying field consumer that performs the actual operation
     */
    public final FieldConsumer<T> consumer;

    /**
     * Constructs a FieldBiConsumer with the specified field index and consumer.
     *
     * @param fieldIndex the index of the field to be set
     * @param consumer the consumer that will set the field value
     */
    public FieldBiConsumer(int fieldIndex, FieldConsumer<T> consumer) {
        this.fieldIndex = fieldIndex;
        this.consumer = consumer;
    }

    /**
     * Performs this operation on the given arguments by delegating to the
     * underlying {@link FieldConsumer} with the pre-bound field index.
     *
     * @param object the object whose field will be set
     * @param fieldValue the value to set for the field
     */
    @Override
    public void accept(T object, Object fieldValue) {
        consumer.accept(object, fieldIndex, fieldValue);
    }
}
