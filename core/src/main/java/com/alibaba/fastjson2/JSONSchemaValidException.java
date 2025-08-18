package com.alibaba.fastjson2;

/**
 * Exception thrown when JSON schema validation fails.
 *
 * @author wenshao
 * @since 2.0.59
 */
public class JSONSchemaValidException
        extends JSONException {
    /**
     * Constructs a new JSONSchemaValidException with the specified detail message.
     *
     * @param message the detail message
     */
    public JSONSchemaValidException(String message) {
        super(message);
    }
}
