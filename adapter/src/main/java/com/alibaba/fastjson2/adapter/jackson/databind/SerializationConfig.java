package com.alibaba.fastjson2.adapter.jackson.databind;

import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;

import static com.alibaba.fastjson2.JSONWriter.Feature.WriteByteArrayAsBase64;

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
        return this;
    }

    public SerializationConfig without(MapperFeature f) {
        // TODO
        return this;
    }

    public SerializationConfig with(SerializationFeature f) {
        serFeatures |= f.getMask();
        return this;
    }

    public SerializationConfig without(SerializationFeature f) {
        serFeatures &= ~f.getMask();
        return this;
    }

    protected JSONWriter.Context createWriterContext() {
        JSONWriter.Context context = JSONFactory.createWriteContext(writerProvider, WriteByteArrayAsBase64);

        if ((serFeatures & SerializationFeature.INDENT_OUTPUT.getMask()) != 0) {
            context.config(JSONWriter.Feature.PrettyFormat);
        }

        return context;
    }
}
