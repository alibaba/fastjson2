package com.alibaba.fastjson2;

public class JSONException
        extends RuntimeException {
    public JSONException(String message) {
        super(message);
    }

    public JSONException(String message, Throwable cause) {
        super(message, cause);
    }
}
