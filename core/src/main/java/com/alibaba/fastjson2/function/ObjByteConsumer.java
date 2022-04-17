package com.alibaba.fastjson2.function;

public interface ObjByteConsumer<T> {
    void accept(T t, byte value);
}
