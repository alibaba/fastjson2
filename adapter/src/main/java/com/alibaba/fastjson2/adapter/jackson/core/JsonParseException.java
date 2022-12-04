package com.alibaba.fastjson2.adapter.jackson.core;

public class JsonParseException
        extends JsonProcessingException {
    public JsonParseException() {
    }

    public JsonParseException(String message) {
        super(message);
    }

    public JsonParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public JsonParseException(JsonParser p, String msg) {
        super(msg);
    }
}
