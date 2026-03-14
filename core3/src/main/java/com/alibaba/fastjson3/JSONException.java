package com.alibaba.fastjson3;

/**
 * Unchecked exception for all JSON processing errors.
 */
public class JSONException extends RuntimeException {
    public JSONException(String message) {
        super(message);
    }

    public JSONException(String message, Throwable cause) {
        super(message, cause);
    }
}
