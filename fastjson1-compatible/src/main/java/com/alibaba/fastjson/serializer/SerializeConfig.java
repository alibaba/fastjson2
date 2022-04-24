package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson.PropertyNamingStrategy;

public class SerializeConfig {

    public static SerializeConfig global = new SerializeConfig();

    public static SerializeConfig getGlobalInstance() {
        return global;
    }

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
