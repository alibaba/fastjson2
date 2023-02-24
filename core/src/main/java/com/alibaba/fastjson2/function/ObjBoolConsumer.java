package com.alibaba.fastjson2.function;

@FunctionalInterface
public interface ObjBoolConsumer<T> {
    void accept(T t, boolean value);
}
