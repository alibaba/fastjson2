package com.alibaba.fastjson2.adapter.jackson.core;

public class JsonProcessingException
        extends JacksonException {
    public JsonProcessingException() {
    }

    public JsonProcessingException(String message) {
        super(message);
    }

    public JsonProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String getOriginalMessage() { return super.getMessage(); }
}
