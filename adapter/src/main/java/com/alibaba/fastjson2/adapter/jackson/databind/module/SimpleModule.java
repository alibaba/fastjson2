package com.alibaba.fastjson2.adapter.jackson.databind.module;

import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.adapter.jackson.core.Version;
import com.alibaba.fastjson2.adapter.jackson.databind.JsonDeserializer;
import com.alibaba.fastjson2.adapter.jackson.databind.JsonSerializer;
import com.alibaba.fastjson2.adapter.jackson.databind.jsontype.NamedType;
import com.alibaba.fastjson2.modules.ObjectReaderModule;
import com.alibaba.fastjson2.modules.ObjectWriterModule;

import java.util.concurrent.atomic.AtomicInteger;

public class SimpleModule
        extends com.alibaba.fastjson2.adapter.jackson.databind.Module {
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

    public SimpleModule(String name) {
        this.name = name;
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

    public SimpleModule addSerializer(JsonSerializer<?> ser) {
        Class<?> handledType = ser.handledType();
        if (handledType == null) {
            throw new JSONException("not support null handledType");
        }

        if (serializers == null) {
            serializers = new SimpleSerializers();
        }
        serializers.addSerializer(handledType, ser);
        return this;
    }

    public <T> SimpleModule addDeserializer(Class<T> type, JsonDeserializer<? extends T> deser) {
        if (deserializers == null) {
            deserializers = new SimpleDeserializers();
        }
        deserializers.addDeserializer(type, deser);
        return this;
    }

    public SimpleModule setMixInAnnotation(Class<?> targetType, Class<?> mixinClass) {
        throw new JSONException("TODO");
    }

    public ObjectWriterModule getWriterModule() {
        return serializers;
    }

    public ObjectReaderModule getReaderModule() {
        return deserializers;
    }

    public SimpleModule registerSubtypes(NamedType... subtypes) {
        throw new JSONException("TODO");
    }
}
