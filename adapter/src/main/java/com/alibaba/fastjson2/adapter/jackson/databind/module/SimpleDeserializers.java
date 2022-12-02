package com.alibaba.fastjson2.adapter.jackson.databind.module;

import com.alibaba.fastjson2.adapter.jackson.databind.JsonDeserializer;
import com.alibaba.fastjson2.modules.ObjectReaderModule;
import com.alibaba.fastjson2.reader.ObjectReader;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;

public class SimpleDeserializers
        implements ObjectReaderModule {
    protected HashMap<Type, JsonDeserializer<?>> classMappings;
    protected boolean hasEnumDeserializer;

    public <T> void addDeserializer(Class<T> forClass, JsonDeserializer<? extends T> deser) {
        if (classMappings == null) {
            classMappings = new HashMap<>();
        }
        classMappings.put(forClass, deser);
        if (forClass == Enum.class) {
            hasEnumDeserializer = true;
        }
    }

    public ObjectReader getObjectReader(Type type) {
        JsonDeserializer<?> deserializer = classMappings.get(type);
        if (deserializer == null) {
            if (type instanceof ParameterizedType) {
                Type rawType = ((ParameterizedType) type).getRawType();
                deserializer = classMappings.get(rawType);
            }
        }
        return deserializer;
    }
}
