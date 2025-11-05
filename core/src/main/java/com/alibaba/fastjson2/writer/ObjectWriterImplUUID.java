package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;
import java.util.UUID;

/**
 * ObjectWriterImplUUID provides serialization support for {@link java.util.UUID} values to JSON format.
 * UUIDs are serialized as strings in their standard format (e.g., "550e8400-e29b-41d4-a716-446655440000").
 *
 * <p>This class provides support for:
 * <ul>
 *   <li>UUID serialization as standard hyphenated string format</li>
 *   <li>Null UUID handling</li>
 *   <li>Optimized JSONB encoding for UUIDs</li>
 * </ul>
 *
 * <p><b>Usage Examples:</b></p>
 * <pre>{@code
 * // Serialize a UUID
 * UUID uuid = UUID.randomUUID();
 * String json = JSON.toJSONString(uuid);
 * // "550e8400-e29b-41d4-a716-446655440000"
 *
 * // Serialize null UUID
 * UUID nullUuid = null;
 * String json = JSON.toJSONString(nullUuid); // null
 *
 * // UUID in an object
 * class Entity {
 *     UUID id = UUID.randomUUID();
 * }
 * String json = JSON.toJSONString(new Entity());
 * // {"id":"550e8400-e29b-41d4-a716-446655440000"}
 * }</pre>
 *
 * @since 2.0.0
 */
final class ObjectWriterImplUUID
        extends ObjectWriterPrimitiveImpl {
    static final ObjectWriterImplUUID INSTANCE = new ObjectWriterImplUUID();

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        jsonWriter.writeUUID((UUID) object);
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNull();
            return;
        }

        jsonWriter.writeUUID((UUID) object);
    }
}
