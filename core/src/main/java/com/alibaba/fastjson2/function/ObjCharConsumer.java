package com.alibaba.fastjson2.function;

@FunctionalInterface
public interface ObjCharConsumer<T> {
    void accept(T t, char value);
}
