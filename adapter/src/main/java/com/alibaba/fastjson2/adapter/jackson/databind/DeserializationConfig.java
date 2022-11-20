package com.alibaba.fastjson2.adapter.jackson.databind;

import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;

import static com.alibaba.fastjson2.adapter.jackson.databind.DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES;

public class DeserializationConfig {
    private int features;
    final ObjectReaderProvider readerProvider;

    public DeserializationConfig() {
        this.readerProvider = new ObjectReaderProvider();
    }

    public DeserializationConfig with(DeserializationFeature f) {
        features |= f.getMask();
        return this;
    }

    public DeserializationConfig without(DeserializationFeature feature) {
        features &= ~feature.getMask();
        return this;
    }

    JSONReader.Context createReaderContext() {
        JSONReader.Context context = JSONFactory.createReadContext(readerProvider);
        configTo(context);
        return context;
    }

    public void configTo(JSONReader.Context context) {
        if ((features & FAIL_ON_NULL_FOR_PRIMITIVES.getMask()) != 0) {
            context.config(JSONReader.Feature.ErrorOnNullForPrimitives);
        }
    }
}
