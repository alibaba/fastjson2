package com.alibaba.fastjson2.function;

public interface Function<T, R> {
    R apply(T t);
}
