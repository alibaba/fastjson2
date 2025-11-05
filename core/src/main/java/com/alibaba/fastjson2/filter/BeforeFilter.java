package com.alibaba.fastjson2.filter;

import com.alibaba.fastjson2.JSONWriter;

/**
 * Abstract filter for writing additional properties before the object's own properties during serialization.
 * Useful for adding metadata or computed properties at the start of the JSON object.
 *
 * <p><b>Usage Examples:</b></p>
 * <pre>{@code
 * BeforeFilter timestampFilter = new BeforeFilter() {
 *     @Override
 *     public void writeBefore(Object object) {
 *         writeKeyValue("_timestamp", System.currentTimeMillis());
 *         writeKeyValue("_type", object.getClass().getSimpleName());
 *     }
 * };
 *
 * String json = JSON.toJSONString(user, timestampFilter);
 * // Output: {"_timestamp":1234567890,"_type":"User","id":1,"name":"Alice"}
 * }</pre>
 */
public abstract class BeforeFilter
        implements Filter {
    private static final ThreadLocal<JSONWriter> serializerLocal = new ThreadLocal<>();

    public void writeBefore(JSONWriter serializer, Object object) {
        JSONWriter last = serializerLocal.get();
        serializerLocal.set(serializer);
        writeBefore(object);
        serializerLocal.set(last);
    }

    /**
     * Writes a key-value pair to the JSON output.
     * Use this method within writeBefore() to add properties.
     *
     * @param key the property name
     * @param value the property value
     */
    protected final void writeKeyValue(String key, Object value) {
        JSONWriter serializer = serializerLocal.get();
        boolean ref = serializer.containsReference(value);
        serializer.writeName(key);
        serializer.writeColon();
        serializer.writeAny(value);
        if (!ref) {
            serializer.removeReference(value);
        }
    }

    /**
     * Called before serializing the object's properties.
     * Override this method to write additional properties.
     *
     * @param object the object being serialized
     */
    public abstract void writeBefore(Object object);
}
