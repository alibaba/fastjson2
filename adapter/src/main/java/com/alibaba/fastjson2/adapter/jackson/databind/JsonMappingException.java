package com.alibaba.fastjson2.adapter.jackson.databind;

public class JsonMappingException
        extends DatabindException {
    public JsonMappingException() {
    }

    public JsonMappingException(String message) {
        super(message);
    }

    public JsonMappingException(String message, Throwable cause) {
        super(message, cause);
    }
}
