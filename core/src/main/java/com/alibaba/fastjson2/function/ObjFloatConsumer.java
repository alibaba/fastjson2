package com.alibaba.fastjson2.function;

@FunctionalInterface
public interface ObjFloatConsumer<T> {
    void accept(T t, float value);
}
