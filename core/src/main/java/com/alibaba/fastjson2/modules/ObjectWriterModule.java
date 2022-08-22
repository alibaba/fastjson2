package com.alibaba.fastjson2.modules;

import com.alibaba.fastjson2.writer.FieldWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;

import java.lang.reflect.Type;
import java.util.List;

public interface ObjectWriterModule {
    default void init(ObjectWriterProvider provider) {
    }

    default ObjectWriter getObjectWriter(Type objectType, Class objectClass) {
        return null;
    }

    default boolean createFieldWriters(
            ObjectWriterCreator creator,
            Class objectType,
            List<FieldWriter> fieldWriters) {
        return false;
    }

    default ObjectWriterAnnotationProcessor getAnnotationProcessor() {
        return null;
    }

    default ObjectWriterProvider getProvider() {
        return null;
    }
}
