package com.alibaba.fastjson2.modules;

import com.alibaba.fastjson2.codec.BeanInfo;
import com.alibaba.fastjson2.codec.FieldInfo;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;

import java.lang.reflect.Field;
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

    default void getBeanInfo(BeanInfo beanInfo, Class<?> objectClass) {
        ObjectReaderAnnotationProcessor annotationProcessor = getAnnotationProcessor();
        if (annotationProcessor != null) {
            annotationProcessor.getBeanInfo(beanInfo, objectClass);
        }
    }

    default void getFieldInfo(FieldInfo fieldInfo, Class objectClass, Field field) {
        ObjectReaderAnnotationProcessor annotationProcessor = getAnnotationProcessor();
        if (annotationProcessor != null) {
            annotationProcessor.getFieldInfo(fieldInfo, objectClass, field);
        }
    }

    default ObjectReader getObjectReader(ObjectReaderProvider provider, Type type) {
        return getObjectReader(type);
    }

    default ObjectReader getObjectReader(Type type) {
        return null;
    }
}
