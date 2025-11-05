package com.alibaba.fastjson2.function;

/**
 * Represents a function that produces a short-valued result from an input argument.
 * This is the {@code short}-producing primitive specialization for {@link java.util.function.Function}.
 *
 * <p>This is a functional interface whose functional method is {@link #applyAsShort(Object)}.
 *
 * @param <T> the type of the input to the function
 * @see java.util.function.ToIntFunction
 * @see java.util.function.ToLongFunction
 * @see java.util.function.ToDoubleFunction
 * @since 2.0.0
 */
@FunctionalInterface
public interface ToShortFunction<T> {
    /**
     * Applies this function to the given argument.
     *
     * @param value the function argument
     * @return the function result as a short
     */
    short applyAsShort(T value);
}
