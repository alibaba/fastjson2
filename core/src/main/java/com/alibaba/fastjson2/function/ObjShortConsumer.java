package com.alibaba.fastjson2.function;

@FunctionalInterface
public interface ObjShortConsumer<T> {
    void accept(T t, short value);
}
