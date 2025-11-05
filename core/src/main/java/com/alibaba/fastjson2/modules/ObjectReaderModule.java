package com.alibaba.fastjson2.modules;

import com.alibaba.fastjson2.codec.BeanInfo;
import com.alibaba.fastjson2.codec.FieldInfo;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

/**
 * Module interface for customizing object reading (deserialization) behavior in fastjson2.
 * Implementations can provide custom {@link ObjectReader} instances, annotation processors,
 * and customize bean/field information extraction.
 *
 * <p>Modules are registered with {@link ObjectReaderProvider} and are invoked during
 * the deserialization process to allow custom handling of specific types or annotations.
 *
 * @see ObjectReaderProvider
 * @see ObjectReaderAnnotationProcessor
 * @since 2.0.0
 */
public interface ObjectReaderModule {
    /**
     * Initializes this module with the given provider. This method is called when
     * the module is registered with an ObjectReaderProvider.
     *
     * @param provider the provider that this module is being registered with
     */
    default void init(ObjectReaderProvider provider) {
    }

    /**
     * Returns the ObjectReaderProvider associated with this module, if any.
     *
     * @return the provider, or null if not applicable
     */
    default ObjectReaderProvider getProvider() {
        return null;
    }

    /**
     * Returns the annotation processor for this module, if any.
     *
     * @return the annotation processor, or null if not applicable
     */
    default ObjectReaderAnnotationProcessor getAnnotationProcessor() {
        return null;
    }

    /**
     * Extracts bean information from the given class using this module's annotation processor.
     *
     * @param beanInfo the bean information object to populate
     * @param objectClass the class to extract information from
     */
    default void getBeanInfo(BeanInfo beanInfo, Class<?> objectClass) {
        ObjectReaderAnnotationProcessor annotationProcessor = getAnnotationProcessor();
        if (annotationProcessor != null) {
            annotationProcessor.getBeanInfo(beanInfo, objectClass);
        }
    }

    /**
     * Extracts field information from the given field using this module's annotation processor.
     *
     * @param fieldInfo the field information object to populate
     * @param objectClass the class containing the field
     * @param field the field to extract information from
     */
    default void getFieldInfo(FieldInfo fieldInfo, Class objectClass, Field field) {
        ObjectReaderAnnotationProcessor annotationProcessor = getAnnotationProcessor();
        if (annotationProcessor != null) {
            annotationProcessor.getFieldInfo(fieldInfo, objectClass, field);
        }
    }

    /**
     * Returns a custom ObjectReader for the specified type, if this module provides one.
     *
     * @param provider the provider requesting the reader
     * @param type the type to create a reader for
     * @return a custom ObjectReader, or null to use the default reader
     */
    default ObjectReader getObjectReader(ObjectReaderProvider provider, Type type) {
        return getObjectReader(type);
    }

    /**
     * Returns a custom ObjectReader for the specified type, if this module provides one.
     *
     * @param type the type to create a reader for
     * @return a custom ObjectReader, or null to use the default reader
     */
    default ObjectReader getObjectReader(Type type) {
        return null;
    }
}
