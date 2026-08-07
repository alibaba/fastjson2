package com.alibaba.fastjson2.function;

import java.util.function.Function;

public final class FieldSupplierFunction<T>
        implements Function<T, Object> {
    public final FieldSupplier<T> supplier;
    public final int fieldIndex;

    public FieldSupplierFunction(FieldSupplier<T> supplier, int fieldIndex) {
        this.supplier = supplier;
        this.fieldIndex = fieldIndex;
    }

    @Override
    public Object apply(T object) {
        return supplier.get(object, fieldIndex);
    }
}
