package com.alibaba.fastjson2.adapter.jackson.databind.deser.std;

import com.alibaba.fastjson2.adapter.jackson.databind.JavaType;
import com.alibaba.fastjson2.adapter.jackson.databind.JsonDeserializer;

public abstract class StdDeserializer<T>
        extends JsonDeserializer<T> {
    protected final Class<?> valueClass;
    protected final JavaType valueType;

    protected StdDeserializer(Class<?> vc) {
        valueClass = vc;
        valueType = null;
    }

    protected StdDeserializer(JavaType valueType) {
        this.valueType = valueType;
        this.valueClass = valueType.getRawClass();
    }
}
