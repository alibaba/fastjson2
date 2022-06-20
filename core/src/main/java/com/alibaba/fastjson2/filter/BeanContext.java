package com.alibaba.fastjson2.filter;

import com.alibaba.fastjson2.codec.FieldInfo;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

public class BeanContext {
    private final Class beanClass;

    private final Method method;

    private final Field field;

    private final String name;

    private final String label;

    private final Class fieldClass;
    private final Type fieldType;

    private final long features;

    private final String format;

    public BeanContext(
            Class beanClass,
            Method method,
            Field field,
            String name,
            String label,
            Class fieldClass,
            Type fieldType,
            long features,
            String format) {
        this.beanClass = beanClass;
        this.method = method;
        this.field = field;
        this.name = name;
        this.label = label;
        this.fieldClass = fieldClass;
        this.fieldType = fieldType;
        this.features = features;
        this.format = format;
    }

    public Class<?> getBeanClass() {
        return beanClass;
    }

    public Method getMethod() {
        return method;
    }

    public Field getField() {
        return field;
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }

    public Class<?> getFieldClass() {
        return fieldClass;
    }

    public Type getFieldType() {
        return fieldType;
    }

    public long getFeatures() {
        return features;
    }

    public boolean isJsonDirect() {
        return (features & FieldInfo.RAW_VALUE_MASK) != 0;
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        T annotatition = null;
        if (method != null) {
            annotatition = method.getAnnotation(annotationClass);
        }

        if (annotatition == null && field != null) {
            annotatition = field.getAnnotation(annotationClass);
        }

        return annotatition;
    }

    public String getFormat() {
        return format;
    }
}
