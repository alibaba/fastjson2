package com.alibaba.fastjson2.modules;

import com.alibaba.fastjson2.codec.BeanInfo;
import com.alibaba.fastjson2.codec.FieldInfo;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public interface ObjectReaderAnnotationProcessor {
    default void getBeanInfo(BeanInfo beanInfo, Class<?> objectClass) {
    }

    default void getFieldInfo(FieldInfo fieldInfo, Class objectClass, Field field) {
    }

    default void getFieldInfo(FieldInfo fieldInfo, Class objectClass, Constructor constructor, int paramIndex, Parameter parameter) {
    }

    default void getFieldInfo(FieldInfo fieldInfo, Class objectClass, Method method, int paramIndex, Parameter parameter) {
    }

    default void getFieldInfo(FieldInfo fieldInfo, Class objectClass, Method method) {
    }
}
