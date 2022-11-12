package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;

public abstract class StdDeserializer<T>
        extends JsonDeserializer<T> {
    protected final Class<?> valueClass;
    protected final JavaType valueType;

    protected StdDeserializer(Class<?> vc) {
        valueClass = vc;
        valueType = null;
    }
}
