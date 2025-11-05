package com.alibaba.fastjson2.function;

import java.util.function.Function;

/**
 * A {@link Function} implementation that adapts a {@link FieldSupplier} by binding
 * a specific field index. This allows using the field index-aware supplier in contexts
 * that expect a standard Function interface.
 *
 * <p>This class is used internally by fastjson2's serialization mechanism to efficiently
 * retrieve field values by pre-bound field indices.
 *
 * @param <T> the type of the object from which the field value is retrieved
 * @see FieldSupplier
 * @since 2.0.0
 */
public final class FieldSupplierFunction<T>
        implements Function<T, Object> {
    /**
     * The underlying field supplier that retrieves field values
     */
    public final FieldSupplier<T> supplier;

    /**
     * The field index to use when retrieving values
     */
    public final int fieldIndex;

    /**
     * Constructs a FieldSupplierFunction with the specified supplier and field index.
     *
     * @param supplier the supplier that will retrieve the field value
     * @param fieldIndex the index of the field to be retrieved
     */
    public FieldSupplierFunction(FieldSupplier<T> supplier, int fieldIndex) {
        this.supplier = supplier;
        this.fieldIndex = fieldIndex;
    }

    /**
     * Applies this function to the given object by delegating to the
     * underlying {@link FieldSupplier} with the pre-bound field index.
     *
     * @param object the object from which to retrieve the field value
     * @return the value of the field at the bound index
     */
    @Override
    public Object apply(T object) {
        return supplier.get(object, fieldIndex);
    }
}
