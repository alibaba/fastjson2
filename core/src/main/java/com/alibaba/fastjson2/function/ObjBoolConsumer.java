package com.alibaba.fastjson2.function;

/**
 * Represents an operation that accepts an object-valued and a {@code boolean}-valued argument,
 * and returns no result. This is the {@code (reference, boolean)} specialization of {@link java.util.function.BiConsumer}.
 * Unlike most other functional interfaces, {@code ObjBoolConsumer} is expected to operate via side-effects.
 *
 * <p>This is a functional interface whose functional method is {@link #accept(Object, boolean)}.
 *
 * @param <T> the type of the object argument to the operation
 * @see java.util.function.ObjIntConsumer
 * @see java.util.function.ObjLongConsumer
 * @see java.util.function.ObjDoubleConsumer
 * @since 2.0.0
 */
@FunctionalInterface
public interface ObjBoolConsumer<T> {
    /**
     * Performs this operation on the given arguments.
     *
     * @param t the first input argument
     * @param value the second input argument
     */
    void accept(T t, boolean value);
}
