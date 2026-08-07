package com.alibaba.fastjson2.function;

@FunctionalInterface
public interface ObjByteConsumer<T> {
    void accept(T t, byte value);
}
