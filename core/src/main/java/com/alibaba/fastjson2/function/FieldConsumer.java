package com.alibaba.fastjson2.function;

@FunctionalInterface
public interface FieldConsumer<T> {
    void accept(T object, int fieldIndex, Object fieldValue);
}
