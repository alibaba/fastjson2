package com.alibaba.fastjson2.diff.function;

import java.util.Objects;

@FunctionalInterface
public interface Function<T, R> {
    static <T> java.util.function.Function<T, T> identity() {
        return t -> t;
    }

    R apply(T t);

    default <V> java.util.function.Function<V, R> compose(java.util.function.Function<? super V, ? extends T> before) {
        Objects.requireNonNull(before);
        return (V v) -> apply(before.apply(v));
    }

    default <V> java.util.function.Function<T, V> andThen(java.util.function.Function<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (T t) -> after.apply(apply(t));
    }
}
