package com.fasterxml.jackson.databind.module;

import com.alibaba.fastjson2.modules.ObjectReaderModule;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.fasterxml.jackson.databind.JsonDeserializer;

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
        return classMappings.get(type);
    }
}
