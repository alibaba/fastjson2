package com.alibaba.fastjson2.modules;

import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;

import java.lang.reflect.Type;

public interface ObjectReaderModule {
    default void init(ObjectReaderProvider provider) {
    }

    default ObjectReaderProvider getProvider() {
        return null;
    }

    default ObjectReaderAnnotationProcessor getAnnotationProcessor() {
        return null;
    }

    default ObjectReader getObjectReader(ObjectReaderProvider provider, Type type) {
        return getObjectReader(type);
    }

    default ObjectReader getObjectReader(Type type) {
        return null;
    }
}
