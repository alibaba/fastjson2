package com.alibaba.fastjson3.modules;

import com.alibaba.fastjson3.ObjectReader;

import java.lang.reflect.Type;

/**
 * Module for extending deserialization. Register custom {@link ObjectReader} instances
 * for specific types.
 *
 * <pre>
 * ObjectMapper mapper = ObjectMapper.builder()
 *     .addReaderModule(new ObjectReaderModule() {
 *         &#64;Override
 *         public ObjectReader&lt;?&gt; getObjectReader(Type type) {
 *             if (type == Point.class) return new PointReader();
 *             return null;
 *         }
 *     })
 *     .build();
 * </pre>
 */
public interface ObjectReaderModule {
    /**
     * Called when the module is registered with an ObjectMapper.
     */
    default void init() {
    }

    /**
     * Provide an ObjectReader for the given type, or null to delegate to the next module.
     *
     * @param type the target type
     * @return an ObjectReader, or null if this module doesn't handle the type
     */
    default ObjectReader<?> getObjectReader(Type type) {
        return null;
    }
}
