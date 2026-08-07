package com.alibaba.fastjson2.function;

@FunctionalInterface
public interface ToByteFunction<T> {
    byte applyAsByte(T value);
}
