package com.alibaba.fastjson2.adapter.jackson.databind;

import java.lang.reflect.Type;

public class DeserializationContext {
    final Type fieldType;
    final Object fieldName;
    final long features;

    public DeserializationContext(Type fieldType, Object fieldName, long features) {
        this.fieldType = fieldType;
        this.fieldName = fieldName;
        this.features = features;
    }
}
