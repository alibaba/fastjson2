package com.alibaba.fastjson3.modules;

import com.alibaba.fastjson3.ObjectWriter;

import java.lang.reflect.Type;

/**
 * Module for extending serialization. Register custom {@link ObjectWriter} instances
 * for specific types.
 *
 * <pre>
 * ObjectMapper mapper = ObjectMapper.builder()
 *     .addWriterModule(new ObjectWriterModule() {
 *         &#64;Override
 *         public ObjectWriter&lt;?&gt; getObjectWriter(Type type, Class&lt;?&gt; rawType) {
 *             if (rawType == Point.class) return new PointWriter();
 *             return null;
 *         }
 *     })
 *     .build();
 * </pre>
 */
public interface ObjectWriterModule {
    /**
     * Called when the module is registered with an ObjectMapper.
     */
    default void init() {
    }

    /**
     * Provide an ObjectWriter for the given type, or null to delegate to the next module.
     *
     * @param type    the source type (may be parameterized)
     * @param rawType the raw class
     * @return an ObjectWriter, or null if this module doesn't handle the type
     */
    default ObjectWriter<?> getObjectWriter(Type type, Class<?> rawType) {
        return null;
    }
}
