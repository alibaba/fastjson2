package com.alibaba.fastjson2.adapter.jackson.databind.util;

public abstract class StdConverter<I, O>
        implements Converter<I, O> {
    public abstract O convert(I value);
}
