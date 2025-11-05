package com.alibaba.fastjson2.function;

/**
 * Represents a supplier of {@code short}-valued results. This is the
 * {@code short}-producing primitive specialization of {@link java.util.function.Supplier}.
 *
 * <p>There is no requirement that a distinct result be returned each
 * time the supplier is invoked.
 *
 * <p>This is a functional interface whose functional method is {@link #getAsShort()}.
 *
 * @see java.util.function.Supplier
 * @see java.util.function.IntSupplier
 * @see java.util.function.LongSupplier
 * @see java.util.function.DoubleSupplier
 * @since 2.0.0
 */
@FunctionalInterface
public interface ShortSupplier {
    /**
     * Gets a result.
     *
     * @return a short value
     */
    short getAsShort();
}
