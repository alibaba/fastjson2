package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;

import static com.alibaba.fastjson2.JSONWriter.Feature.WriteNonStringValueAsString;

/**
 * ObjectWriterImplInt32 provides serialization support for Integer (int32) values to JSON format.
 * This writer handles both primitive int and boxed Integer types.
 *
 * <p>This class provides support for:
 * <ul>
 *   <li>Integer serialization as JSON numbers</li>
 *   <li>Integer serialization as strings when WriteNonStringValueAsString is enabled</li>
 *   <li>Null handling for boxed Integer values</li>
 *   <li>Optimized JSONB encoding for integers</li>
 * </ul>
 *
 * <p><b>Usage Examples:</b></p>
 * <pre>{@code
 * // Standard integer serialization
 * Integer value = 42;
 * String json = JSON.toJSONString(value); // 42
 *
 * // Serialize as string
 * String json = JSON.toJSONString(value, JSONWriter.Feature.WriteNonStringValueAsString);
 * // "42"
 *
 * // Null handling
 * Integer nullValue = null;
 * String json = JSON.toJSONString(nullValue); // null
 * }</pre>
 *
 * @since 2.0.0
 */
final class ObjectWriterImplInt32
        extends ObjectWriterPrimitiveImpl {
    static final ObjectWriterImplInt32 INSTANCE = new ObjectWriterImplInt32();

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNumberNull();
            return;
        }

        int value = (Integer) object;

        if ((features & WriteNonStringValueAsString.mask) != 0) {
            jsonWriter.writeString(value);
            return;
        }
        jsonWriter.writeInt32(value);
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        if (object == null) {
            jsonWriter.writeNumberNull();
            return;
        }

        int value = (Integer) object;

        if ((features & WriteNonStringValueAsString.mask) != 0) {
            jsonWriter.writeString(value);
            return;
        }
        jsonWriter.writeInt32(value);
    }
}
