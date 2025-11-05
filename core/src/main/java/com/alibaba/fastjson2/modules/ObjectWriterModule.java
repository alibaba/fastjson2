package com.alibaba.fastjson2.modules;

import com.alibaba.fastjson2.writer.FieldWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterCreator;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Module interface for customizing object writing (serialization) behavior in fastjson2.
 * Implementations can provide custom {@link ObjectWriter} instances, field writers,
 * and annotation processors for controlling serialization behavior.
 *
 * <p>Modules are registered with {@link ObjectWriterProvider} and are invoked during
 * the serialization process to allow custom handling of specific types or annotations.
 *
 * @see ObjectWriterProvider
 * @see ObjectWriterAnnotationProcessor
 * @since 2.0.0
 */
public interface ObjectWriterModule {
    /**
     * Initializes this module with the given provider. This method is called when
     * the module is registered with an ObjectWriterProvider.
     *
     * @param provider the provider that this module is being registered with
     */
    default void init(ObjectWriterProvider provider) {
    }

    /**
     * Returns a custom ObjectWriter for the specified type, if this module provides one.
     *
     * @param objectType the type to create a writer for
     * @param objectClass the class to create a writer for
     * @return a custom ObjectWriter, or null to use the default writer
     */
    default ObjectWriter getObjectWriter(Type objectType, Class objectClass) {
        return null;
    }

    /**
     * Creates custom field writers for the specified object type. Implementations can
     * add custom FieldWriter instances to the provided list.
     *
     * @param creator the creator used to create field writers
     * @param objectType the type to create field writers for
     * @param fieldWriters the list to add field writers to
     * @return true if field writers were created, false otherwise
     */
    default boolean createFieldWriters(
            ObjectWriterCreator creator,
            Class objectType,
            List<FieldWriter> fieldWriters) {
        return false;
    }

    /**
     * Returns the annotation processor for this module, if any.
     *
     * @return the annotation processor, or null if not applicable
     */
    default ObjectWriterAnnotationProcessor getAnnotationProcessor() {
        return null;
    }

    /**
     * Returns the ObjectWriterProvider associated with this module, if any.
     *
     * @return the provider, or null if not applicable
     */
    default ObjectWriterProvider getProvider() {
        return null;
    }
}
