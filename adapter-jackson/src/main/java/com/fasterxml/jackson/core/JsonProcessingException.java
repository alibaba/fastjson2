package com.fasterxml.jackson.core;

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
