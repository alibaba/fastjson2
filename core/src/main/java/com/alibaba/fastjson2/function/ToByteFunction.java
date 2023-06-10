package com.alibaba.fastjson2.function;

public interface ToByteFunction<T> {
    byte applyAsByte(T value);
}
