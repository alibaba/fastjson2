package com.alibaba.fastjson2.function;

@FunctionalInterface
public interface ToCharFunction<T> {
    char applyAsChar(T value);
}
