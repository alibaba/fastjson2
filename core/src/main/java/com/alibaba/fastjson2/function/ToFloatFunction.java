package com.alibaba.fastjson2.function;

@FunctionalInterface
public interface ToFloatFunction<T> {
    float applyAsFloat(T value);
}
