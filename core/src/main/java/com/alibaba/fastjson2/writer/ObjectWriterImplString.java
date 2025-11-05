package com.alibaba.fastjson2.writer;

import com.alibaba.fastjson2.JSONWriter;

import java.lang.reflect.Type;

/**
 * ObjectWriterImplString provides serialization support for String values to JSON format.
 * This is one of the most frequently used writers as strings are common in JSON serialization.
 *
 * <p>This class provides support for:
 * <ul>
 *   <li>String serialization with proper escaping of special characters</li>
 *   <li>Unicode character handling</li>
 *   <li>Null string handling</li>
 *   <li>Optimized JSONB encoding for strings</li>
 *   <li>UTF-8 and UTF-16 encoding support</li>
 * </ul>
 *
 * <p><b>Usage Examples:</b></p>
 * <pre>{@code
 * // Simple string serialization
 * String str = "Hello, World!";
 * String json = JSON.toJSONString(str); // "Hello, World!"
 *
 * // String with special characters
 * String str = "Line 1\nLine 2\tTabbed";
 * String json = JSON.toJSONString(str); // "Line 1\nLine 2\tTabbed"
 *
 * // Null string
 * String nullStr = null;
 * String json = JSON.toJSONString(nullStr); // null
 * }</pre>
 *
 * @since 2.0.0
 */
final class ObjectWriterImplString
        extends ObjectWriterPrimitiveImpl {
    static final ObjectWriterImplString INSTANCE = new ObjectWriterImplString();

    @Override
    public void writeJSONB(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        jsonWriter.writeString((String) object);
    }

    @Override
    public void write(JSONWriter jsonWriter, Object object, Object fieldName, Type fieldType, long features) {
        jsonWriter.writeString((String) object);
    }
}
