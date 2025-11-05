package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ObjectWriterImplAtomicInteger provides serialization support for {@link java.util.concurrent.atomic.AtomicInteger}
 * values to JSON format. AtomicInteger values are serialized as their integer value.
 *
 * <p>This class provides support for:
 * <ul>
 *   <li>AtomicInteger serialization as integer value</li>
 *   <li>Type information writing when required</li>
 *   <li>Null AtomicInteger handling</li>
 *   <li>Optimized JSONB encoding</li>
 * </ul>
 *
 * <p><b>Usage Examples:</b></p>
 * <pre>{@code
 * // Serialize an AtomicInteger
 * AtomicInteger counter = new AtomicInteger(42);
 * String json = JSON.toJSONString(counter); // 42
 *
 * // With type information
 * String json = JSON.toJSONString(counter, JSONWriter.Feature.WriteClassName);
 * // {"@type":"AtomicInteger","value":42}
 *
 * // In an object
 * class Counter {
 *     AtomicInteger count = new AtomicInteger(10);
 * }
 * String json = JSON.toJSONString(new Counter());
 * // {"count":10}
 * }</pre>
 *
 * @since 2.0.0
 */
final class ObjectWriterImplAtomicInteger
        extends ObjectWriterPrimitiveImpl {
    static final ObjectWriterImplAtomicInteger INSTANCE = new ObjectWriterImplAtomicInteger(null);
    static final byte[] JSONB_TYPE_NAME_BYTES = JSONB.toBytes("AtomicInteger");

    final Class defineClass;

    public ObjectWriterImplAtomicInteger(Class defineClass) {
        this.defineClass = defineClass;
    }

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNumberNull();
            return;
        }

        AtomicInteger atomic = (AtomicInteger) object;
        if (jsonWriter.isWriteTypeInfo(atomic, fieldType)) {
            final long JSONB_TYPE_HASH = 7576651708426282938L; // Fnv.hashCode64("AtomicInteger");
            jsonWriter.writeTypeName(JSONB_TYPE_NAME_BYTES, JSONB_TYPE_HASH);
        }

        jsonWriter.writeInt32(atomic.intValue());
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNumberNull();
            return;
        }

        AtomicInteger atomic = (AtomicInteger) object;
        jsonWriter.writeInt32(atomic.intValue());
    }
}
