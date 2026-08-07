package com.alibaba.fastjson2.function;

import java.util.function.BiConsumer;

public final class FieldBiConsumer<T>
        implements BiConsumer<T, Object> {
    public final int fieldIndex;
    public final FieldConsumer<T> consumer;

    public FieldBiConsumer(int fieldIndex, FieldConsumer<T> consumer) {
        this.fieldIndex = fieldIndex;
        this.consumer = consumer;
    }

    @Override
    public void accept(T object, Object fieldValue) {
        consumer.accept(object, fieldIndex, fieldValue);
    }
}
