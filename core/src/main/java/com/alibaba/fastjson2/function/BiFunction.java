package com.alibaba.fastjson2.function;

public interface BiFunction<T, U, R> {
    R apply(T t, U u);
}
