package com.alibaba.fastjson2;

/**
 * Exception thrown when attempting to serialize an object that exceeds size limits
 * and the LargeObject feature is not enabled.
 *
 * @since 2.0.55
 */
public class JSONLargeObjectException
        extends JSONException {
    public JSONLargeObjectException(String message) {
        super(message);
    }
    public JSONLargeObjectException(String message, Throwable cause) {
        super(message, cause);
    }
}
