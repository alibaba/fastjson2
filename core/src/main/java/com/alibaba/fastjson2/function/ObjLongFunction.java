package com.alibaba.fastjson2.function;

public interface ObjLongFunction<T,R> {
    R apply(T t, long value);
}
