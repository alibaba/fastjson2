package com.alibaba.fastjson2.adapter.jackson.databind.util;

public interface Converter<IN, OUT> {
    OUT convert(IN value);

    abstract static class None
            implements Converter<Object, Object> {
    }
}
