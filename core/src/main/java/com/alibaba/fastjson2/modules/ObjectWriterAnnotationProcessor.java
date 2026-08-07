package com.alibaba.fastjson2.modules;

import com.alibaba.fastjson2.codec.BeanInfo;
import com.alibaba.fastjson2.codec.FieldInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public interface ObjectWriterAnnotationProcessor {
    default void getBeanInfo(BeanInfo beanInfo, Class objectClass) {
    }

    default void getFieldInfo(BeanInfo beanInfo, FieldInfo fieldInfo, Class objectType, Field field) {
    }

    default void getFieldInfo(BeanInfo beanInfo, FieldInfo fieldInfo, Class objectType, Method method) {
    }
}
