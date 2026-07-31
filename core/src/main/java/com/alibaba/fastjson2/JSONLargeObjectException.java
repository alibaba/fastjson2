package com.alibaba.fastjson2;

/**
 * Exception thrown when attempting to serialize an object that exceeds size limits
 * and the LargeObject feature is not enabled.
 *
 * @author wenshao
 * @since 2.0.58
 */
public class JSONLargeObjectException
        extends JSONException {
    /**
     * Constructs a new JSONLargeObjectException with the specified detail message.
     *
     * @param message the detail message
     */
    public JSONLargeObjectException(String message) {
        super(message);
    }

    /**
     * Constructs a new JSONLargeObjectException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause of the exception
     */
    public JSONLargeObjectException(String message, Throwable cause) {
        super(message, cause);
    }
}
