package com.alibaba.fastjson2.modules;

import com.alibaba.fastjson2.codec.BeanInfo;
import com.alibaba.fastjson2.codec.FieldInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Processor interface for extracting metadata from annotations on classes, fields, and methods
 * during object writing (serialization). Implementations can process custom annotations and
 * populate {@link BeanInfo} and {@link FieldInfo} objects with configuration.
 *
 * <p>This interface is used by {@link ObjectWriterModule} implementations to provide custom
 * annotation processing logic.
 *
 * @see ObjectWriterModule
 * @see BeanInfo
 * @see FieldInfo
 * @since 2.0.0
 */
public interface ObjectWriterAnnotationProcessor {
    /**
     * Extracts bean-level information from annotations on the given class.
     *
     * @param beanInfo the bean information object to populate
     * @param objectClass the class to extract annotation information from
     */
    default void getBeanInfo(BeanInfo beanInfo, Class objectClass) {
    }

    /**
     * Extracts field-level information from annotations on the given field.
     *
     * @param beanInfo the bean information object (provides context)
     * @param fieldInfo the field information object to populate
     * @param objectType the class containing the field
     * @param field the field to extract annotation information from
     */
    default void getFieldInfo(BeanInfo beanInfo, FieldInfo fieldInfo, Class objectType, Field field) {
    }

    /**
     * Extracts field-level information from annotations on a method (typically a getter method).
     *
     * @param beanInfo the bean information object (provides context)
     * @param fieldInfo the field information object to populate
     * @param objectType the class containing the method
     * @param method the method to extract annotation information from
     */
    default void getFieldInfo(BeanInfo beanInfo, FieldInfo fieldInfo, Class objectType, Method method) {
    }
}
