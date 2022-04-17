package com.alibaba.fastjson2.function;

public interface ObjIntFunction<T,R> {
    R apply(T t, int value);
}
