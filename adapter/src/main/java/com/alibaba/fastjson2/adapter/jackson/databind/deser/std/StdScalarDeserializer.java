package com.alibaba.fastjson2.adapter.jackson.databind.deser.std;

public abstract class StdScalarDeserializer<T>
        extends StdDeserializer<T> {
    protected StdScalarDeserializer(Class<?> vc) {
        super(vc);
    }
}
