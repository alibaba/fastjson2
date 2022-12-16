package com.alibaba.fastjson2.adapter.jackson.databind.module;

import com.alibaba.fastjson2.adapter.jackson.databind.JsonSerializer;
import com.alibaba.fastjson2.modules.ObjectWriterModule;
import com.alibaba.fastjson2.writer.ObjectWriter;

import java.lang.reflect.Type;
import java.util.HashMap;

public class SimpleSerializers
        implements ObjectWriterModule {
    protected HashMap<Type, JsonSerializer<?>> classMappings;
    protected HashMap<Type, JsonSerializer<?>> interfaceMappings;

    protected boolean hasEnumSerializer;

    public void addSerializer(Class<?> cls, JsonSerializer<?> ser) {
        // Interface or class type?
        if (cls.isInterface()) {
            if (interfaceMappings == null) {
                interfaceMappings = new HashMap<>();
            }
            interfaceMappings.put(cls, ser);
        } else { // nope, class:
            if (classMappings == null) {
                classMappings = new HashMap<>();
            }
            classMappings.put(cls, ser);
            if (cls == Enum.class) {
                hasEnumSerializer = true;
            }
        }
    }

    public ObjectWriter getObjectWriter(Type objectType, Class objectClass) {
        return classMappings.get(objectType);
    }
}
