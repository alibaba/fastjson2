package com.alibaba.fastjson2.function;

@FunctionalInterface
public interface ToShortFunction<T> {
    short applyAsShort(T value);
}
