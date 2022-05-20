package com.alibaba.fastjson2;

public class JSONSchemaValidException extends JSONException {
    public JSONSchemaValidException(String message) {
        super(message);
    }

    public JSONSchemaValidException(String message, Throwable cause) {
        super(message, cause);
    }
}
