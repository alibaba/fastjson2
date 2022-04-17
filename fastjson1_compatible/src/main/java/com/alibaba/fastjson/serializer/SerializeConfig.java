package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson.PropertyNamingStrategy;

import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentMap;

public class SerializeConfig {
    public PropertyNamingStrategy propertyNamingStrategy;
//    private final ConcurrentMap<Type, ObjectSerializer> serializers;

    public boolean put(Object type, Object value) {
//        return put((Type)type, (ObjectSerializer)value);
        throw new UnsupportedOperationException();
    }
//
//    public boolean put(Type type, ObjectSerializer value) {
//        return this.serializers.put(type, value);
//    }

    public void setAsmEnable(boolean value) {
        // skip
    }
}
