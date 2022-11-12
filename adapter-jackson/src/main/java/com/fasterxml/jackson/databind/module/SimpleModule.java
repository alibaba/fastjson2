package com.fasterxml.jackson.databind.module;

import com.alibaba.fastjson2.modules.ObjectReaderModule;
import com.alibaba.fastjson2.modules.ObjectWriterModule;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;

import java.util.concurrent.atomic.AtomicInteger;

public class SimpleModule
        extends com.fasterxml.jackson.databind.Module {
    private static final AtomicInteger MODULE_ID_SEQ = new AtomicInteger(1);

    protected SimpleSerializers serializers;
    protected SimpleDeserializers deserializers;

    protected final String name;
    protected final Version version;
    protected final boolean hasExplicitName;

    public SimpleModule() {
        name = (getClass() == SimpleModule.class)
                ? "SimpleModule-" + MODULE_ID_SEQ.getAndIncrement()
                : getClass().getName();
        version = Version.unknownVersion();
        // 07-Jun-2021, tatu: [databind#3110] Not passed explicitly so...
        hasExplicitName = false;
    }

    public SimpleModule(String name, Version version) {
        this.name = name;
        this.version = version;
        hasExplicitName = true;
    }

    public <T> SimpleModule addSerializer(Class<? extends T> type, JsonSerializer<T> ser) {
        if (serializers == null) {
            serializers = new SimpleSerializers();
        }
        serializers.addSerializer(type, ser);
        return this;
    }

    public <T> SimpleModule addDeserializer(Class<T> type, JsonDeserializer<? extends T> deser) {
        if (deserializers == null) {
            deserializers = new SimpleDeserializers();
        }
        deserializers.addDeserializer(type, deser);
        return this;
    }

    public ObjectWriterModule getWriterModule() {
        return serializers;
    }

    public ObjectReaderModule getReaderModule() {
        return deserializers;
    }
}
