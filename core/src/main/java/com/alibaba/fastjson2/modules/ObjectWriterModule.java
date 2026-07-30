package com.alibaba.fastjson2.modules;

import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;

import java.lang.reflect.Type;

public interface ObjectWriterModule {
    default void init(ObjectWriterProvider provider) {
    }

    default ObjectWriter getObjectWriter(Type objectType, Class objectClass) {
        return null;
    }

    default ObjectWriterAnnotationProcessor getAnnotationProcessor() {
        return null;
    }

    default ObjectWriterProvider getProvider() {
        return null;
    }
}
