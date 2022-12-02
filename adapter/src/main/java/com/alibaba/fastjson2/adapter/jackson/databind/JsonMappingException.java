package com.alibaba.fastjson2.adapter.jackson.databind;

import java.io.Closeable;

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

    public JsonMappingException(Closeable processor, String message, Throwable cause) {
        super(message, cause);
    }

    public JsonMappingException(Closeable processor, String message) {
        super(message);
    }
}
