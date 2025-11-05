package com.alibaba.fastjson2.modules;

import com.alibaba.fastjson2.codec.BeanInfo;
import com.alibaba.fastjson2.codec.FieldInfo;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * Processor interface for extracting metadata from annotations on classes, fields, methods,
 * and parameters during object reading (deserialization). Implementations can process custom
 * annotations and populate {@link BeanInfo} and {@link FieldInfo} objects with configuration.
 *
 * <p>This interface is used by {@link ObjectReaderModule} implementations to provide custom
 * annotation processing logic.
 *
 * @see ObjectReaderModule
 * @see BeanInfo
 * @see FieldInfo
 * @since 2.0.0
 */
public interface ObjectReaderAnnotationProcessor {
    /**
     * Extracts bean-level information from annotations on the given class.
     *
     * @param beanInfo the bean information object to populate
     * @param objectClass the class to extract annotation information from
     */
    default void getBeanInfo(BeanInfo beanInfo, Class<?> objectClass) {
    }

    /**
     * Extracts field-level information from annotations on the given field.
     *
     * @param fieldInfo the field information object to populate
     * @param objectClass the class containing the field
     * @param field the field to extract annotation information from
     */
    default void getFieldInfo(FieldInfo fieldInfo, Class objectClass, Field field) {
    }

    /**
     * Extracts field-level information from annotations on a constructor parameter.
     *
     * @param fieldInfo the field information object to populate
     * @param objectClass the class containing the constructor
     * @param constructor the constructor containing the parameter
     * @param paramIndex the index of the parameter
     * @param parameter the parameter to extract annotation information from
     */
    default void getFieldInfo(FieldInfo fieldInfo, Class objectClass, Constructor constructor, int paramIndex, Parameter parameter) {
    }

    /**
     * Extracts field-level information from annotations on a method parameter.
     *
     * @param fieldInfo the field information object to populate
     * @param objectClass the class containing the method
     * @param method the method containing the parameter
     * @param paramIndex the index of the parameter
     * @param parameter the parameter to extract annotation information from
     */
    default void getFieldInfo(FieldInfo fieldInfo, Class objectClass, Method method, int paramIndex, Parameter parameter) {
    }

    /**
     * Extracts field-level information from annotations on a method (typically a getter method).
     *
     * @param fieldInfo the field information object to populate
     * @param objectClass the class containing the method
     * @param method the method to extract annotation information from
     */
    default void getFieldInfo(FieldInfo fieldInfo, Class objectClass, Method method) {
    }
}
