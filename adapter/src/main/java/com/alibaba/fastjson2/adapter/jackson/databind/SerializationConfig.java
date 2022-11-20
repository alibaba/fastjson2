package com.alibaba.fastjson2.adapter.jackson.databind;

import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;

public class SerializationConfig {
    private long serFeatures;
    protected int generatorFeatures;
    final ObjectWriterProvider writerProvider;

    public SerializationConfig() {
        this(JSONFactory.getDefaultObjectWriterProvider());
    }

    public SerializationConfig(ObjectWriterProvider writerProvider) {
        this.writerProvider = writerProvider;
    }

    public final boolean isEnabled(SerializationFeature f) {
        return (serFeatures & f.getMask()) != 0;
    }

    public SerializationConfig with(MapperFeature f) {
        // TODO
        return this;
    }

    public SerializationConfig without(MapperFeature f) {
        // TODO
        return this;
    }

    public SerializationConfig with(SerializationFeature f) {
        // TODO
        return this;
    }

    public SerializationConfig without(SerializationFeature f) {
        // TODO
        return this;
    }
}
