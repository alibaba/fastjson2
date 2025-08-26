package com.alibaba.fastjson2;

/**
 * Exception thrown when JSON validation fails.
 *
 * @author wenshao
 * @since 2.0.59
 */
public class JSONValidException
        extends JSONException {
    /**
     * Constructs a new JSONValidException with the specified detail message.
     *
     * @param message the detail message
     */
    public JSONValidException(String message) {
        super(message);
    }
}
