package com.alibaba.fastjson2.filter;

import com.alibaba.fastjson2.JSONWriter;

/**
 * Abstract filter for writing additional properties after the object's own properties during serialization.
 * Useful for adding computed properties or metadata at the end of the JSON object.
 *
 * <p><b>Usage Examples:</b></p>
 * <pre>{@code
 * AfterFilter computedFilter = new AfterFilter() {
 *     @Override
 *     public void writeAfter(Object object) {
 *         if (object instanceof User) {
 *             User user = (User) object;
 *             writeKeyValue("fullName", user.getFirstName() + " " + user.getLastName());
 *             writeKeyValue("isActive", user.getStatus() == Status.ACTIVE);
 *         }
 *     }
 * };
 *
 * String json = JSON.toJSONString(user, computedFilter);
 * // Output: {"id":1,"firstName":"Alice","lastName":"Smith","fullName":"Alice Smith","isActive":true}
 * }</pre>
 */
public abstract class AfterFilter
        implements Filter {
    private static final ThreadLocal<JSONWriter> writerLocal = new ThreadLocal<>();

    public void writeAfter(JSONWriter serializer, Object object) {
        JSONWriter last = writerLocal.get();
        writerLocal.set(serializer);
        writeAfter(object);
        writerLocal.set(last);
    }

    /**
     * Writes a key-value pair to the JSON output.
     * Use this method within writeAfter() to add properties.
     *
     * @param key the property name
     * @param value the property value
     */
    protected final void writeKeyValue(String key, Object value) {
        JSONWriter serializer = writerLocal.get();
        boolean ref = serializer.containsReference(value);
        serializer.writeName(key);
        serializer.writeColon();
        serializer.writeAny(value);
        if (!ref) {
            serializer.removeReference(value);
        }
    }

    /**
     * Called after serializing the object's properties.
     * Override this method to write additional properties.
     *
     * @param object the object being serialized
     */
    public abstract void writeAfter(Object object);
}
